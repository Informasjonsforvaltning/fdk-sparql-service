# FDK SPARQL Service

This application provides an API for the SPARQL page on [data.norge.no](https://data.norge.no/sparql) and to other
backend applications.

The application manages an
embedded [Fuseki](https://jena.apache.org/documentation/fuseki2/fuseki-embedded.html#fuseki-basic) server instance, with
a persistent [TDB2](https://jena.apache.org/documentation/tdb2) database. A schedule checks for any updated resources
every 15 minutes, the schedule updates the associated fuseki-graph when it finds that resources have been updated.

The service listens for kafka events, relevant events are `*_REASONED` and `*_REMOVED` where `*` is the associated
resource-type. Any of these events will update a postgres database and tag the relevant resource-type as updated. The
fuseki graph for the type tagged as updated will be generated from the data in postgres when the 15-minute schedule next
checks what has been updated.

The update will create a new graph and drop the old, but the old graph will still use disk space.
A [compact](https://jena.apache.org/documentation/tdb2/tdb2_admin.html) process will therefore run after each update, to
free the now unused disk space.

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki. For more
specific context on this application, see the **Portal** subsystem section.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

Ensure you have the following installed:

- Java 17
- Maven
- Docker

### Running locally

#### Clone the repository

```sh
git clone https://github.com/Informasjonsforvaltning/fdk-sparql-service.git
cd fdk-sparql-service
```

#### Generate sources

Kafka messages are serialized using Avro. Avro schemas are located in ```kafka/schemas```. To generate sources from Avro
schema, run the following command:

```
mvn generate-sources    
```

#### Start PostgreSQL database, Kafka cluster and setup topics/schemas

Topics and schemas are set up automatically when starting the Kafka cluster. Docker compose uses the scripts
```create-topics.sh``` and ```create-schemas.sh``` to set up topics and schemas.

```
docker-compose up -d
```

If you have problems starting kafka, check if all health checks are ok. Make sure number at the end (after 'grep')
matches desired topics.

#### Start application

```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Produce messages

Check if schema id is correct in the script. This should be 1 if there is only one schema in your registry.

```
sh ./kafka/produce-messages.sh
```

### Running tests

```sh
mvn verify
```
