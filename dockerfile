FROM container-registry.oracle.com/os/oraclelinux:8-slim

LABEL authors="ttomtsis, icsd15201@icsd.aegean.gr"


WORKDIR /app
COPY ./target /app


# Required by Terraform CDK
RUN microdnf module enable nodejs:20
RUN microdnf install nodejs

# Required by JMeter and Icarus
RUN microdnf install java-21-openjdk

# Terraform is required to manage the serverless infrastructure
RUN microdnf install unzip
RUN curl -O https://releases.hashicorp.com/terraform/1.6.3/terraform_1.6.3_linux_amd64.zip
RUN unzip terraform_1.6.3_linux_amd64.zip -d /usr/local/bin/


EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=--enable-preview

ENTRYPOINT ["java", "-jar", "icarus-0.0.1-SNAPSHOT.jar"]
