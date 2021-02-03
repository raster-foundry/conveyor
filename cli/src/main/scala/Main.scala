package com.rasterfoundry.conveyor

import com.rasterfoundry.conveyor.programs.NewProjectProgram

import cats.effect._
import com.softwaremill.sttp
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.TimeUnit
import com.monovore.decline.effect.CommandIOApp

object Conveyor extends IOApp.WithContext {

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] =
    Resource
      .make(
        SyncIO(
          Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("conveyor-io-%d").build()
          )
        )
      )(
        pool =>
          SyncIO {
            pool.shutdown()
            val _ = pool.awaitTermination(10, TimeUnit.SECONDS)
          }
      )
      .map(ExecutionContext.fromExecutor _)

  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO]()

  def run(args: List[String]): IO[ExitCode] = {
    val uploadProgram = Commands.uploadTiffOpts map {
      case np @ Commands.NewProject(_, _, token, _) =>
        val http: Http[IO] = new LiveHttp[IO](sttp.emptyRequest, token)
        new NewProjectProgram(http).run(np).as(ExitCode.Success)
    }
    CommandIOApp
      .run("conveyor", "Upload a tif to a Raster Foundry project")(
        uploadProgram,
        args
      )
  }

}
