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
e.g. with

````bash
$ mvn -Prhuss clean install
````

## Results

The following sections summarizes some results while developing and running the examples. Of course, they can change over time when new releases happen.
Please feel also free to send pull requests for things which you consider to be wrong or corrected. And of course, the results
are somewhat biased ;-)

### rhuss

* Version: 0.11.3
* Log output of containers' standard out during integration test
* Progress bar when downloading images
* Full support for waiting on time, url or log output after container startup
* Flexible (dynamic) port mapping and assignment to variables
* Building images via plugin configuration or Dockerfiles
* Credentials can be stored encrypted in `~/.m2/settings.xml` in the `<servers>` section
* No detailed output when building images (only when debugging is switched on with `-X`)

### wouterd

* Version: 3.0
* No progress indicator during long lasting download. Build seems to hang.
* No variable substitution in Dockerfile so the artifact must be specified with version number
  (and updated for each new version).
* Port mapping variables are fixed and not explicitly mentioned in the configuration (one has to guess or look at
  the Dockerfile to find out the variable names and ports exposed).
* Credentials must be given as properties or within the plugin configuration. No support for encrypted password and usage
  of `~/.m2/settings.xml`.

### alexec

* Version: 2.4.0
* The integration test doesn't work on other systems than Linux since there is no possibility
  for dynamic port mapping. It always maps the exposed port (8080) to the same port on the
  docker host. For Boot2Docker this doesn't work easily because of the extra layer of a Linux VM.
  You can start the created images manually, however and run the integration tests directly. See the
  the section about the *spotify* plugin for an example.
* Quite noisy on standard out (including the whole HTTP communication).
* Cannot influence name of linked containers. You application need to use the given name which
  is automatically calculated by the artifact name and the image directory name.
* Port mapping cannot be dynamically used. Only the ports specified in `conf.yml` are exported
  directly.
* No waiting on log output of the DB possible.

### spotify

* Version: 0.2.3
* This plugin can be only used to build an image, so its quite limited for this example.
* A Dockerfile is needed because we want to export a port (this is not possible with the simple configuration mode).

The image can be build with

````
mvn -Pspotify clean package docker:build
````

In order to test is, the images must be started manually:

````
docker run -name postgres_cnt -d postgres
docker run -P -link postgres_cnt:db jolokia/docker-maven-demo-spotify:0.0.1-SNAPSHOT
`````

Then you can run the integration test:

````
mvn -Dlog.url=http://localhost:49162 integration-test
````

(replace 49162 with the dynamic port as shown by `docker ps` or `docker port`)
