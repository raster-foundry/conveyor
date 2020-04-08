package com.rasterfoundry.conveyor.programs

import com.rasterfoundry.conveyor.{Commands, Http}
import com.rasterfoundry.datamodel.{Project, Upload, UploadStatus, Visibility}

import cats.effect.IO
import cats.implicits._
import com.rasterfoundry.datamodel.FileType
import com.rasterfoundry.datamodel.UploadType
import io.circe.syntax._
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
      jwt       <- http.getJWT(projectOpts.refreshToken)
      project   <- http.createProject(projectCreate, jwt)
      upload    <- http.createUpload(getUploadCreate(project), jwt)
      uploadUri <- http.getUploadDestination(upload, jwt)
      _         <- http.uploadTiff(projectOpts.tiffPath, uploadUri)
      _         <- http.completeUpload(upload, uploadUri, jwt)
    } yield ()).recoverWith({
      case err => IO { println(err.getMessage) }
    })
  }
}
