FROM container-registry.oracle.com/os/oraclelinux:8-slim

LABEL authors="ttomtsis, icsd15201@icsd.aegean.gr"


WORKDIR /app
COPY ./target /app


# Required for Terraform CDK
RUN microdnf install nodejs

# Required for JMeter as well as the jar
RUN microdnf install java-21-openjdk


EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=--enable-preview

ENTRYPOINT ["java", "-jar", "icarus-0.0.1-SNAPSHOT.jar"]
