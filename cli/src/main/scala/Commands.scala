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

  private val userOpt =
    Opts.option[String]("user", help = "Person to greet.").withDefault("world")

  private val quietOpt = Opts.flag("quiet", help = "Whether to be quiet.").orFalse

  private val sadOpt = Opts.flag("sad", help = "Whether it's sad user is leaving").orFalse

  case class SayHello(
      user: String,
      quiet: Boolean
  )

  case class SayGoodbye(
      user: String,
      sad: Boolean
  )

  case class NewProject(
      projectName: String,
      tiffPath: String,
      refreshToken: String
  )

  val helloOpts: Opts[SayHello] =
    Opts.subcommand("hello", "Say hello") {
      (userOpt, quietOpt).mapN(SayHello)
    }

  val goodbyeOpts: Opts[SayGoodbye] =
    Opts.subcommand("goodbye", "Say goodbye") {
      (userOpt, sadOpt).mapN(SayGoodbye)
    }

  val uploadTiffOpts: Opts[NewProject] =
    Opts.subcommand("new-project", "Upload an image to a new Raster Foundry project") {
      (projectNameOpt, localTiffPathOpt, refreshTokenOpt).mapN(NewProject)
    }
}
