package com.rasterfoundry

import cats.{ApplicativeError, Monad}
import cats.implicits._
import io.circe.Error

import com.softwaremill.sttp.{DeserializationError, Response}

package object conveyor {

  implicit class SttpResponse[F[_]: Monad: ApplicativeError[?[_], Throwable], T](
      r: F[Response[Either[DeserializationError[Error], T]]]
  ) {

    def decode: F[T] = {
      r.flatMap { response =>
        response.body match {
          case Left(e) => {
            println(s"Response: ${response.code}, ${response.statusText}")
            ApplicativeError[F, Throwable].raiseError(new Exception(s"Error retrieving response from API: $e"))
          }
          case Right(value) =>
            value match {
              case Left(e)      => ApplicativeError[F, Throwable].raiseError(e.error)
              case Right(value) => ApplicativeError[F, Throwable].pure(value)
            }
        }
      }
    }
  }
}
