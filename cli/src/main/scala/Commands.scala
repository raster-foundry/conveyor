package com.rasterfoundry.conveyor

import cats.implicits._

import com.monovore.decline._

import java.util.UUID

object Commands {

  private val localTiffPathOpt =
    Opts.argument[String]("TIFF_ABSOLUTE_PATH")

  private val projectNameOpt =
    Opts.argument[String]("PROJECT_NAME")

  private val refreshTokenOpt =
    Opts.argument[String]("REFRESH_TOKEN")
  
  private val datasourceOpt =
    Opts.option[UUID]("datasource", "Which datasource to associate with this imagery").withDefault(
      UUID.fromString("e4d1b0a0-99ee-493d-8548-53df8e20d2aa")
    )

  case class NewProject(
      projectName: String,
      tiffPath: String,
      refreshToken: String,
      datasourceId: UUID
  )

  val uploadTiffOpts: Opts[NewProject] =
    Opts.subcommand("new-project", "Upload an image to a new Raster Foundry project") {
      (projectNameOpt, localTiffPathOpt, refreshTokenOpt, datasourceOpt).mapN(NewProject)
    }
}
