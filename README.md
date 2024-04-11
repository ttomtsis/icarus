# icarus

This project is part of my undergraduate thesis in University of the Aegean, supervised by Dr. K. Kritikos.

**Concept**: 

Icarus is a FaaS testing utility, able to evaluate the Functional and Performance characteristics of AWS Lambda and Google Cloud Function serverless functions.
The main goal of the project is to provide an easy to use, extendable, open source serverless benchmarking tool that supports several FaaS providers and abstracts the complexity of managing the lifecycle of individual functions. Icarus relies on well known tools to execute Functional and Performance tests and utilizes Terraform and the Terraform CDK to manage the lifecycle of the functions. 

**Associated DockerHub repository:** 
https://hub.docker.com/r/ttomtsis/icarus

# Features
* Execution of Performance Tests using JMeter
* Execution of Functional Tests using RestAssured
* Function lifecycle management using Terraform and the Terraform CDK
* Querying of Function Metrics from every provider
* Automated generation of reports containing test artefacts and test results using Eclipse BiRT
* User management
* Auth0 integration

# Technology stack
* Java 21
* Spring Boot 3.1.5
* PostgreSQL 15.5
* Terraform 1.6.5
* JMeter 5.6.2
* Eclipse BiRT 4.8.0

# Getting started

## Installation
To get started with this project, you can install Icarus on your host system or use a container.

### Install on host system
To install Icarus in your host system you will need the following dependencies:
* Java 21+ installed
* A PostgreSQL v15.5+ database  
* NodeJS 20+ installed and properly configured ( required by Terraform CDK )
* Terraform installed and available on the PATH
* JMeter installed

If you need help installing the dependencies refer to the appendix

After installing the required dependencies the rest of the process is straightforward:
* Download the latest jar from the releases page of the project: https://github.com/ttomtsis/icarus/releases
* Place it in the directory of your choosing
* Enable java preview features by setting the JAVA_TOOL_OPTIONS environment variable equal to --enable-preview `$env:JAVA_TOOL_OPTIONS=--enable-preview` ( for powershell )
* Configure Icarus environment variables ( refer to the Configuration section )
* Run Icarus using Java `java -jar icarus-0.0.1.jar`

You can also build the jar yourself:
* Clone the repository using git  `git clone https://github.com/ttomtsis/icarus`
* Use the maven wrapper, or maven to produce a jar of the application  `mvn clean package`
* The jar will be located in the 'target' directory  `cd ./target`

### Install as a container
This is the recommended way of installing Icarus as it is far easier and less error prone
To install Icarus as a container you will need Docker installed and properly configured ( refer to the Appendix for Docker help with the installation )

* Simply pull the latest Icarus image from the associated DockerHub repository: `docker pull ttomtsis/icarus:latest`
* Configure Icarus environment variables located in the 'icarus_backend.env' file ( refer to the Configuration section )
* Run Icarus using Docker `docker run ttomtsis/icarus:latest`

You can also use docker compose to automatically deploy both Icarus and a PostgreSQL database `docker compose up`

## Configuration
Icarus is configured by using environment variables. If you have installed Icarus using docker, you can modify the .env file and use it when running the container

### General Configuration variables
* **FUNCTION_SOURCES_DIRECTORY** - Icarus temporarily places the source code of the functions in a directory, before persisting it permanently to the database or before executing a Test. This environment variable specifies the location of that temporary directory.

### Auth0 Configuration variables
The variables below configure Auth0's integration.
The value of the below variables can be found in the Auth0 console
* **AUTH0_AUDIENCES** - The required audiences that valid JWT tokens must possess
* **AUTH0_DOMAIN** - Auth0's domain
* **AUTH0_PROVIDER_JWKS** - URL where Auth0's Key set is available
* **AUTH0-PROVIDER_URI** - Auth0's provider URI
* **AUTH0_SYNCHRONIZE_DATABASE** - Icarus can automatically delete resources if a user deleted his/her account in Auth0. This process is called 'database synchronization' and by default is set to FALSE
* **AUTH0_MANAGEMENT_API_ID** - ID of Auth0's management API ( Required to synchronize the database )
* **AUTH0_CLIENT_ID** - Auth0 Client's ID ( Required to synchronize the database )
* **AUTH0_CLIENT_SECRET** - Auth0 Client's Secret ( Required to synchronize the database )

### HTTP Basic configuration variables
* **ENABLE_HTTP_BASIC** - By default HTTP Basic is disabled, however users may choose to enable it. HTTP Basic works alongside OAuth2 authentication and enabling this setting will not interfere with Auth0
* **ENABLE_TEST_USER** - If HTTP Basic is enabled, Icarus can create a demo account ( meant for debugging and testing purposes )
* **TEST_USER_EMAIL** - Email of the demo account
* **TEST_USER_USERNAME** - Username of the demo account
* **TEST_USER_PASSWORD** - Password of the demo account

### Database Configuration variables
* **DATASOURCE_URL** - The URL where the PostgreSQL database is exposed
* **DB_USERNAME** - The username that Icarus will use to connect to the database
* **DB_PASSWORD** - The password that Icarus will use to connect to the database

### JMeter Configuration variables
* **JMETER_HOME** - Installation directory of JMeter
* **JMETER_LOGS_DIR** - Directory where JMeter will store logs
* **JMETER_PROPERTIES** - Location of JMeter's configuration file

### Terraform configuration variables
* **STACK_OUTPUT_DIRECTORY** - When deploying a function, Terraform will create stacks and .tfstate files. This variable specifies the directory where those files will be placed
* **USE_LOCAL_PROVIDERS** - Instead of downloading the provider plugins every time a user executes a test, Icarus permits the usage of local providers. This setting increases performance, however by default it is set to FALSE
* **LOCAL_PROVIDERS_DIRECTORY** - Directory of the local provider binaries

### Eclipse BiRT configuration variables
Icarus comes bundled with two ready to use BiRT templates, however a user may choose to replace them with his/her own. In that case the user must configure the following variables:
* **TEST_CASE_RESULT_REPORT_DIRECTORY** - Directory of the template for functional tests
*  **METRIC_RESULT_REPORT_DIRECTORY** - Directory of the template for performance tests
  
# Appendix
## Icarus Dependencies installation
* Java 21 - https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html#GUID-8677A77F-231A-40F7-98B9-1FD0B48C346A
* PostgreSQL 15.5 - https://www.postgresql.org/download/
* NodeJS 20 - https://nodejs.org/en/learn/getting-started/how-to-install-nodejs
* Terraform - https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli
* JMeter - https://jmeter.apache.org/usermanual/get-started.html#install
* Docker - https://docs.docker.com/engine/install/

  
