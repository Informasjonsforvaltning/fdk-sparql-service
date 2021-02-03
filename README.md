# FDK SPARQL Service
A service that manages Fuseki server instance.

## Installation and Usage

- Required tools to run this project:
    - IntelliJ, Maven and Java SDK 15 to run locally on a host machine
    - Docker and Docker Compose to run locally in a container

#### Running application locally on a host machine

- Install dependencies by running `mvn clean install`
- Do the following to run the application:
    - Set optional `develop` profile
    - Run or debug `Application` class using IntelliJ

#### Running application in a Docker container

- Build a Docker container using the following command:
    - `docker build -t fdk-sparql-service .`
- Run the container using the following comand:
    - `docker run -d -p 8080:8080 -e LOG_LEVEL fdk-sparql-service`

#### Running application using Docker Compose

- Run the application using the following command:
    - `docker-compose up -d`

## Environment Variables

- `LOG_LEVEL` - log level
    - `TRACE`
    - `DEBUG`
    - `INFO`
    - `WARN`
    - `ERROR`
