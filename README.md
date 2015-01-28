# Docker Maven Plugin Shootout

 This is a sample project for exercising various Docker maven plugins. The focus is on the
 most active docker maven plugins, namely:

 * [wouterd/docker-maven-plugin](https://github.com/wouterd/docker-maven-plugin)
 * [alexec/docker-maven-plugin](https://github.com/alexec/docker-maven-plugin)
 * [spotify/docker-maven-plugin](https://github.com/spotify/docker-maven-plugin)
 * [rhuss/docker-maven-plugin](https://github.com/rhuss/docker-maven-plugin)

## A Micro-Service

This project contains of a very simple Micro-Service, which accesses a Postgres database. The purpose of
this micro service is to simply log every access in the database and return the list of all log entries. In
addition, a simple integration test checks this behaviour. The database schema is created with [Flyway](http://flywaydb.org/).

This setup consist of two images:

* The official PostgreSQL database `postgres:9`
* An Image holding the microservice. This image is created during the maven build and based on the official `java:8u40` image.

Both containers from this image are docker linked together during the integration test.

## Running the shoot-out

Each plugin is configured in an extra Maven profile: `wouterd`, `alexec`, `spotify` and `rhuss`. The test can be started
with

````bash
$ mvn -Prhuss clean install
````

 ## Results

 ### rhuss

 ### wouterd

 ### alexec

 ### spotify