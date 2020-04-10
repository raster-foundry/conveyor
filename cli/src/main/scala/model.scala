package com.rasterfoundry.conveyor

import com.amazonaws.auth.AWSSessionCredentials

import io.circe.Decoder
import io.circe.generic.semiauto._
import com.amazonaws.auth.AWSSessionCredentials

final case class TokenResponse(idToken: String)

object TokenResponse {
  implicit val decTokenResponse: Decoder[TokenResponse] = Decoder.forProduct1("id_token")(TokenResponse.apply _)
}

// ripped directly from `Credentials.scala` in RF API subproject
final case class Credentials(AccessKeyId: String, Expiration: String, SecretAccessKey: String, SessionToken: String)
    extends AWSSessionCredentials {

  def getAWSAccessKeyId(): String = this.AccessKeyId

  def getAWSSecretKey(): String = this.SecretAccessKey

  def getSessionToken(): String = this.SessionToken
}

object Credentials {
  implicit val decCredentials: Decoder[Credentials] = deriveDecoder
}

// ripped directly from `Credentials.scala` in RF API subprojec
final case class CredentialsWithBucketPath(credentials: Credentials, bucketPath: String)

object CredentialsWithBucketPath {
  implicit val decCredentialsWithBucketPath: Decoder[CredentialsWithBucketPath] = deriveDecoder
}
