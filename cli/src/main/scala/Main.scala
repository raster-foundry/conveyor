package com.rasterfoundry.conveyor

import com.rasterfoundry.conveyor.programs.NewProjectProgram

import cats.effect._
import cats.implicits._

import com.monovore.decline._
import com.monovore.decline.effect._

import com.softwaremill.sttp
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend

object HelloWorld
    extends CommandIOApp(
      name = "conveyor",
      header = "Say hello or goodbye"
    ) {

  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO]()

  override def main: Opts[IO[ExitCode]] =
    (Commands.helloOpts orElse Commands.goodbyeOpts orElse Commands.uploadTiffOpts)
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
        case np @ Commands.NewProject(_, _, token) =>
          val http: Http[IO] = new LiveHttp[IO](sttp.emptyRequest, token)
          new NewProjectProgram(http).run(np)
      })
      .map(_.as(ExitCode.Success))
}
