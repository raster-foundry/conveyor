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

  private def getUploadCreate(project: Project, datasourceId: UUID): Upload.Create =
    Upload.Create(
      UploadStatus.Created,
      FileType.Geotiff,
      UploadType.Local,
      List("placeholder"),
      datasourceId,
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
      project     <- http.createProject(projectCreate)
      upload      <- http.createUpload(getUploadCreate(project, projectOpts.datasourceId))
      credentials <- http.getUploadCredentials(upload)
      _           <- http.uploadTiff(projectOpts.tiffPath, credentials)
      _           <- http.completeUpload(upload, credentials.bucketPath)
    } yield ()).recoverWith({
      case err => IO { println(err.getMessage) }
    })
  }
}
