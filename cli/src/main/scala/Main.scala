package com.rasterfoundry.conveyor

import cats.effect._
import cats.implicits._

import com.monovore.decline._
import com.monovore.decline.effect._

object HelloWorld
    extends CommandIOApp(
      name = "conveyor",
      header = "Say hello or goodbye"
    ) {

  override def main: Opts[IO[ExitCode]] =
    (Commands.helloOpts orElse Commands.goodbyeOpts)
      .map({
        case Commands.SayHello(user, quiet) =>
          IO {
            println(s"Hello, $user!")
          } <* (if (!quiet) {
                  IO { println("So happy to see you!") }
                } else IO.unit)
        case Commands.SayGoodbye(user, sad) =>
          IO {
            println(s"Goodbye, $user" ++ (if (sad) {
                                             " :("
                                           } else {
                                             ""
                                           }))
          }
      })
      .map(_.as(ExitCode.Success))
}
