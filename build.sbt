// give the user a nice default project!
ThisBuild / organization := "com.rasterfoundry"
ThisBuild / scalaVersion := "2.12.11"

val DeclineVersion       = "1.0.0"
val RasterFoundryVersion = "1.40.3"
val SttpVersion          = "2.0.7"

val cliDependencies = List(
  "com.monovore"                 %% "decline"                        % DeclineVersion,
  "com.monovore"                 %% "decline-effect"                 % DeclineVersion,
  "com.rasterfoundry"            %% "datamodel"                      % RasterFoundryVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % SttpVersion,
  "com.softwaremill.sttp.client" %% "circe"                          % SttpVersion,
  "com.softwaremill.sttp.client" %% "core"                           % SttpVersion
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
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
  )
