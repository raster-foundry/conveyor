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
      header = "Upload a tif to a Raster Foundry project"
    ) {

  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO]()

  override def main: Opts[IO[ExitCode]] =
    Commands.uploadTiffOpts
      .map({
        case np @ Commands.NewProject(_, _, token) =>
          val http: Http[IO] = new LiveHttp[IO](sttp.emptyRequest, token)
          new NewProjectProgram(http).run(np)
      })
      .map(_.as(ExitCode.Success))
}
