package com.rasterfoundry.conveyor

import cats.implicits._

import com.monovore.decline._

object Commands {

  private val localTiffPathOpt =
    Opts.argument[String]("TIFF_ABSOLUTE_PATH")

  private val projectNameOpt =
    Opts.argument[String]("PROJECT_NAME")

  private val refreshTokenOpt =
    Opts.argument[String]("REFRESH_TOKEN")

  case class NewProject(
      projectName: String,
      tiffPath: String,
      refreshToken: String
  )

  val uploadTiffOpts: Opts[NewProject] =
    Opts.subcommand("new-project", "Upload an image to a new Raster Foundry project") {
      (projectNameOpt, localTiffPathOpt, refreshTokenOpt).mapN(NewProject)
    }
}
