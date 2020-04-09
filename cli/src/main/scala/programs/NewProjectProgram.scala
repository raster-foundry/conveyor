package com.rasterfoundry.conveyor.programs

import com.rasterfoundry.conveyor.{Commands, Http}
import com.rasterfoundry.datamodel.{Project, Upload, UploadStatus, Visibility}

import cats.effect.IO
import cats.implicits._
import com.rasterfoundry.datamodel.FileType
import com.rasterfoundry.datamodel.UploadType
import io.circe.syntax._
import sttp.model.Uri

import java.util.UUID

class NewProjectProgram(http: Http[IO]) {

  private def getUploadCreate(project: Project): Upload.Create =
    Upload.Create(
      UploadStatus.Created,
      FileType.Geotiff,
      UploadType.S3,
      Nil,
      UUID.randomUUID,
      ().asJson,
      None,
      Visibility.Private,
      Some(project.id),
      None,
      None,
      Some(false),
      None,
      false
    )

  def run(projectOpts: Commands.NewProject): IO[Unit] = {
    val projectCreate = Project.Create(
      projectOpts.projectName,
      "",
      Visibility.Private,
      Visibility.Private,
      false,
      0,
      None,
      Nil,
      false,
      None
    )
    (for {
      project <- http.createProject(projectCreate)
      upload  <- http.createUpload(getUploadCreate(project))
      putUrl  <- http.getUploadDestination(upload)
      result <- IO.fromEither(
        Uri.parse(putUrl.signedUrl).leftMap(s => new Exception(s))
      ) flatMap { uploadUri =>
        http.uploadTiff(projectOpts.tiffPath, uploadUri) *> http.completeUpload(upload, uploadUri)
      }
    } yield {
      println(s"Program result was: ${result map { _.uploadStatus }}")
    }).recoverWith({
      case err => IO { println(err.getMessage) }
    })
  }
}
