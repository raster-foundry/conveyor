// give the user a nice default project!
ThisBuild / organization := "com.rasterfoundry"
ThisBuild / scalaVersion := "2.12.11"

val AWSSdkVersion        = "1.11.535"
val DeclineVersion       = "1.0.0"
val RasterFoundryVersion = "1.40.3"
val SttpVersion          = "1.5.19"

val cliDependencies = List(
  "com.monovore"          %% "decline"                        % DeclineVersion,
  "com.monovore"          %% "decline-effect"                 % DeclineVersion,
  "com.rasterfoundry"     %% "datamodel"                      % RasterFoundryVersion,
  "com.amazonaws"         % "aws-java-sdk-core"               % AWSSdkVersion,
  "com.amazonaws"         % "aws-java-sdk-s3"                 % AWSSdkVersion,
  "com.softwaremill.sttp" %% "async-http-client-backend-cats" % SttpVersion,
  "com.softwaremill.sttp" %% "circe"                          % SttpVersion,
  "com.softwaremill.sttp" %% "core"                           % SttpVersion
)

lazy val cli = (project in file("./cli"))
  .settings(
    libraryDependencies ++= cliDependencies,
    externalResolvers ++= Seq(
      DefaultMavenRepository,
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("azavea", "maven"),
      Resolver.bintrayRepo("azavea", "geotrellis"),
      "locationtech-releases" at "https://repo.locationtech.org/content/groups/releases",
      "locationtech-snapshots" at "https://repo.locationtech.org/content/groups/snapshots",
      Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(
        Resolver.ivyStylePatterns
      )
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4"),
    assemblyMergeStrategy in assembly := {
      case "reference.conf"                       => MergeStrategy.concat
      case "application.conf"                     => MergeStrategy.concat
      case n if n.startsWith("META-INF/services") => MergeStrategy.concat
      case n if n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA") =>
        MergeStrategy.discard
      case "META-INF/MANIFEST.MF" => MergeStrategy.discard
      case _                      => MergeStrategy.first
    }
  )
