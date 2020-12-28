FROM openjdk:8-slim-buster

RUN mkdir -p /opt/jars

COPY ./cli/target/scala-2.12/cli-assembly-0.1.0-SNAPSHOT.jar /opt/jars

ENTRYPOINT ["java", "-Xmx3g", "-cp", "/opt/jars/cli-assembly-0.1.0-SNAPSHOT.jar", "com.rasterfoundry.conveyor.Conveyor", "new-project"]