package com.rasterfoundry.conveyor

import cats.implicits._

import com.monovore.decline._

object Commands {

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

  val helloOpts: Opts[SayHello] =
    Opts.subcommand("hello", "Say hello") {
      (userOpt, quietOpt).mapN(SayHello)
    }

  val goodbyeOpts: Opts[SayGoodbye] =
    Opts.subcommand("goodbye", "Say goodbye") {
      (userOpt, sadOpt).mapN(SayGoodbye)
    }

}
