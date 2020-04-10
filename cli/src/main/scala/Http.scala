package com.rasterfoundry.conveyor

import com.rasterfoundry.datamodel.{Project, Upload, UploadStatus}

import cats.effect.{Async, Sync}
import cats.implicits._
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import io.circe.syntax._
import com.softwaremill.sttp.{Empty, RequestT, SttpBackend, Uri}
import com.softwaremill.sttp.circe._

import java.io.File
import java.net.URI

trait Http[F[_]] {

  def getJWT(refreshToken: String): F[TokenResponse]

  def createProject(projectCreate: Project.Create): F[Project]

  def createUpload(uploadCreate: Upload.Create): F[Upload]

  def getUploadCredentials(upload: Upload): F[CredentialsWithBucketPath]

  def uploadTiff(localPath: String, credentials: CredentialsWithBucketPath): F[Unit]

  def completeUpload(upload: Upload, uploadLocation: String): F[Unit]
}

class LiveHttp[F[_]: Async: Sync](client: RequestT[Empty, String, Nothing], refreshToken: String)(
    implicit backend: SttpBackend[F, Nothing]
) extends Http[F] {

  private def uriFor(apiPath: String): Uri =
    Uri(java.net.URI.create(s"https://app.rasterfoundry.com/api/$apiPath"))

  private def uriToS3Protocol(s3Url: String): Option[(String, String)] = {
    val s3Uri = Uri(URI.create(s3Url))
    val bucketO = s3Uri.host.split('.').headOption
    val key     = s3Uri.path.mkString("/")
    bucketO map { bucket =>
      (bucket, key)
    }
  }

  private val memoizedJWTFetch: F[F[TokenResponse]] = Async.memoize(getJWT(refreshToken))

  private def authedClient: F[RequestT[Empty, String, Nothing]] =
    for {
      jwtFetch <- memoizedJWTFetch
      jwt      <- jwtFetch
    } yield client.auth.bearer(jwt.idToken)

  def getJWT(refreshToken: String): F[TokenResponse] = {
    client
      .post(uriFor("tokens"))
      .body(Map("refresh_token" -> refreshToken).asJson)
      .response(asJson[TokenResponse])
      .send()
      .decode
  }

  def createProject(
      projectCreate: Project.Create
  ): F[Project] =
    for {
      authed <- authedClient
      project <- authed
        .post(uriFor("projects"))
        .body(projectCreate.asJson)
        .response(asJson[Project])
        .send()
        .decode
    } yield project

  def createUpload(
      uploadCreate: Upload.Create
  ): F[Upload] =
    for {
      authed <- authedClient
      upload <- authed
        .post(uriFor("uploads"))
        .body(uploadCreate.asJson)
        .response(asJson[Upload])
        .send()
        .decode
    } yield upload

  def getUploadCredentials(upload: Upload): F[CredentialsWithBucketPath] =
    for {
      authed <- authedClient
      credentials <- authed
        .get(uriFor(s"uploads/${upload.id}/credentials"))
        .response(asJson[CredentialsWithBucketPath])
        .send()
        .map { r =>
          println(r)
          r
        }
        .decode
    } yield {
      println("Got credentials!")
      credentials
    }

  def uploadTiff(filePath: String, credentials: CredentialsWithBucketPath): F[Unit] = {
    val clientRegion        = Regions.US_EAST_1
    val Some((bucket, key)) = uriToS3Protocol(credentials.bucketPath)
    Sync[F].delay {
      println("Creating client")
      val s3Client = AmazonS3ClientBuilder
        .standard()
        .withRegion(clientRegion)
        .withCredentials(new AWSStaticCredentialsProvider(credentials.credentials))
        .build()
      println("Creating transfer manager")
      val tm = TransferManagerBuilder
        .standard()
        .withS3Client(s3Client)
        .build()
      println("Creating upload")
      val upload = tm.upload(bucket, key, new File(filePath))
      upload.waitForCompletion()
      println("Upload to AWS complete")
    }
  }

  def completeUpload(
      upload: Upload,
      uploadLocation: String
  ): F[Unit] = {
    val newFilesO = uriToS3Protocol(uploadLocation) map {
      case (bucket, key) => List(s"s3://$bucket/$key")
    }
    println(s"New files: $newFilesO")
    val newUploadO = newFilesO map { newFiles =>
      upload.copy(
        uploadStatus = UploadStatus.Uploaded,
        files = newFiles
      )
    }

    (newUploadO traverse { (update: Upload) =>
      for {
        authed <- authedClient
        result <- authed
          .put(uriFor(s"uploads/${upload.id}"))
          .body(update.asJson)
          .response(asJson[Upload])
          .send()
          .decode
      } yield result
    }).void
  }
}
