package com.rasterfoundry.conveyor

import com.rasterfoundry.datamodel.{Project, Upload, UploadStatus}

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import com.softwaremill.sttp.{Empty, RequestT, SttpBackend, Uri}
import com.softwaremill.sttp.circe._

import java.nio.file.{Files, Paths}

trait Http[F[_]] {

  def getJWT(refreshToken: String): F[TokenResponse]

  def createProject(projectCreate: Project.Create): F[Project]

  def createUpload(uploadCreate: Upload.Create): F[Upload]

  def getUploadDestination(upload: Upload): F[Upload.PutUrl]

  def uploadTiff(localPath: String, uri: Uri): F[Unit]

  def completeUpload(upload: Upload, uploadLocation: Uri): F[Option[Upload]]
}

class LiveHttp[F[_]: Async](client: RequestT[Empty, String, Nothing], refreshToken: String)(
    implicit backend: SttpBackend[F, Nothing]
) extends Http[F] {

  private def uriFor(apiPath: String): Uri =
    Uri(java.net.URI.create(s"https://app.rasterfoundry.com/api/$apiPath"))

  private def uriToS3Protocol(uri: Uri): Option[String] = {
    val bucketE = uri.host.split(".").headOption
    val key     = uri.path
    bucketE map { bucket =>
      s"s3://$bucket$key"
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

  def getUploadDestination(upload: Upload): F[Upload.PutUrl] =
    for {
      authed <- authedClient
      putUrl <- authed
        .get(uriFor(s"uploads/${upload.id}"))
        .response(asJson[Upload.PutUrl])
        .send()
        .decode
    } yield putUrl

  def uploadTiff(localPath: String, uri: Uri): F[Unit] = {
    val byteArray = Files.readAllBytes(Paths.get(localPath))
    client.put(uri).body(byteArray).send().void
  }

  def completeUpload(
      upload: Upload,
      uploadLocation: Uri
  ): F[Option[Upload]] = {
    val newFilesO = uriToS3Protocol(uploadLocation) map { List(_) }

    val newUploadO = newFilesO map { newFiles =>
      upload.copy(
        uploadStatus = UploadStatus.Uploaded,
        files = newFiles
      )
    }

    newUploadO traverse { (update: Upload) =>
      for {
        authed <- authedClient
        result <- authed
          .put(uriFor(s"uploads/${upload.id}"))
          .body(update.asJson)
          .response(asJson[Upload])
          .send()
          .decode
      } yield result
    }
  }
}
