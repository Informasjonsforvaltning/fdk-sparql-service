# FDK SPARQL Service
A service that manages an embedded [Fuseki](https://jena.apache.org/documentation/fuseki2/fuseki-embedded.html#fuseki-basic) server instance, with a persistent [TDB2](https://jena.apache.org/documentation/tdb2) database.

A schedule checks for any updated resources every 15 minutes, the schedule updates the associated fuseki-graph when it finds that resources have been updated.

The service listens for kafka events, relevant events are `*_REASONED` and `*_REMOVED` where `*` is the associated resource-type. Any of these events will update a postgres database and tag the relevant resource-type as updated. The fuseki graph for the type tagged as updated will be generated from the data in postgres when the 15-minute schedule next checks what has been updated.

### Compact
The update will create a new graph and drop the old, but the old graph will still use disk space. A [compact](https://jena.apache.org/documentation/tdb2/tdb2_admin.html) process will therefore run after each update, to free the now unused disk space.

## Requirements
- maven
- java 17
- docker
- docker-compose

## Run tests
```
mvn verify
```

#### Running application using Docker Compose

- Run the application using the following command:
    - `docker-compose up -d --build`
