package com.rasterfoundry.conveyor

import com.rasterfoundry.datamodel.{Project, Upload}

import sttp.model.Uri

trait Http[F[_]] {

  def getJWT(refreshToken: String): F[String]

  def createProject(projectName: Project.Create, jwt: String): F[Project]

  def createUpload(uploadCreate: Upload.Create, jwt: String): F[Upload]

  def getUploadDestination(upload: Upload, jwt: String): F[Uri]

  def uploadTiff(localPath: String, uri: Uri): F[Unit]

  def completeUpload(upload: Upload, uploadLocation: Uri, jwt: String): F[Upload]
}
