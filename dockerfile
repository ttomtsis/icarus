# Report generation is currently broken for the dockerfile
FROM container-registry.oracle.com/os/oraclelinux:8-slim

LABEL authors="ttomtsis, icsd15201@icsd.aegean.gr"


WORKDIR /app
COPY ./target /app


# Required for Terraform CDK
RUN microdnf module enable nodejs:20
RUN microdnf install nodejs

# Required for JMeter as well as the jar
RUN microdnf install java-21-openjdk

# Developer release is required to install Terraform.
# The Terraform binary is required to manage the serverless infrastructure
RUN microdnf install oraclelinux-developer-release-el8
RUN microdnf install terraform


EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=--enable-preview

ENTRYPOINT ["java", "-jar", "icarus-0.0.1-SNAPSHOT.jar"]
