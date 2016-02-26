# Docker Maven Plugin Shootout

 This is a sample project for exercising various Docker maven plugins. The focus is on the
 most active docker maven plugins, namely:

 * [wouterd/docker-maven-plugin](https://github.com/wouterd/docker-maven-plugin)
 * [alexec/docker-maven-plugin](https://github.com/alexec/docker-maven-plugin)
 * [spotify/docker-maven-plugin](https://github.com/spotify/docker-maven-plugin)
 * [fabric8io/docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin)

Of course, this shootout is biased. And since we know our plugin the best, the configuration for the
 `fabric8io/docker-maven-plugin` is probably tuned best. However, we happily will integrate any PR which improves the configuration for the other examples as well.

## A Micro-Service

This project contains of a very simple Micro-Service, which accesses a Postgres database. The purpose of
this micro service is to simply log every access in the database and return the list of all log entries. In
addition, a simple integration test checks this behaviour. The database schema is created with [Flyway](http://flywaydb.org/).

This setup consist of two images:

* The official PostgreSQL database `postgres:9`
* An Image holding the microservice. This image is created during the maven build and based on the official `java:8u40` image.

Both containers from this image are docker linked together during the integration test.

## Running the shoot-out

Each plugin is configured in an extra Maven profile: `wouterd`, `alexec`, `spotify` and `fabric8`. The test can be started
e.g. with

````bash
$ mvn -Pfabric8 clean install
````

## Results

The following sections summarizes some results while developing and running the examples. Of course, they can change over time when new releases happen.
Please feel also free to send pull requests for things which you consider to be wrong or corrected. And of course, the results
are somewhat biased ;-)

### fabric8io

* Version: 0.14.1

The fabric8 plugin uses a configuration section for all images to maintain. It has config section for 
each section, which is divided in a build and run part for building instructions and rumtime configuration, 
respectively. The service image dynamically adds files as described in the assembly descriptor 
`src/main/fabric8/docker-assembly.xml`

To build the images:

      mvn -Pfabric8 docker:build

To create containers and start them:

      mvn -Pfabric8 docker:build docker:start

Calling

      mvn -Pfabric8 install

will perform all the above plus more: Create images, start container, run unit tests,
stop containers, cleanup.

Some features:

* Log output of containers' standard out during integration test
* Progress bar when downloading images
* Full support for waiting on time, url or log output after container startup
* Flexible (dynamic) port mapping and assignment to variables
* Building images via plugin configuration or Dockerfiles
* Credentials can be stored encrypted in `~/.m2/settings.xml` in the `<servers>` section
* No detailed output when building images (only when debugging is switched on with `-X`)

### wouterd

* Version: 5.0.0

The wouterd plugin uses different configuration sections for building and running images. 
For building images every file (including the Dockerfile itself) must be referenced.

To build the images:

      mvn -Pwouterd package docker:build-images

To create containers and start them:

      mvn -Pwouterd docker:start-containers

Calling

      mvn -Pwouterd install

will perform all the above plus more: Create images, start container, run unit tests, stop containers, 
cleanup.

Some limitations:

* Port mapping variables are fixed and not explicitly mentioned in the configuration (one has to guess or look 
  at the Dockerfile to find out the variable names and ports exposed)
* No progress indicator during long lasting download. Build seems to hang.
* No support for encrypted password in `~/.m2/settings.xml`.

### alexec

* Version: 2.11.9

The alexec plugin uses external configuration below src/main/docker where each subdirectory
specifies an extra image.

To build the images:

      mvn -Palexec package docker:package

To start the images:

      mvn -Palexec docker:start

Run integration tests:

      mvn -Palexec install
      
The host exposed with ${docker.log-service.ipAddress} is not routable outside the host running
the Docker daemon so when running a setup with a VM like Boot2Docker or docker-machine then the
test would need to run within this VM. Hence the integration test code has a workaround to use
the ${DOCKER_HOST} in this case. See `LogServiceIt` for the workaround. 

Other limitations:

* Cannot influence name of linked containers. You application need to use the given name which
  is automatically calculated by the artifact name and the image directory name.
* Port mapping cannot be dynamically used. Only the ports specified in `conf.yml` are exported
  directly.
* No exposure of the external ip adress how a container can be reached (this should be the `DOCKER_HOST`   address).  Only the internal address is reachable.

### spotify

* Version: 0.4.1

Since this plugin can be only used to build an image, its quite limited. Also we need to use
the Dockerfile mode because we want to export a port (which is not possible with the simple
configuration)

Build the image with:

      mvn -Pspotify clean package docker:build

You can run the image but need to link the postgres image properly:

      docker run --name postgres_cnt -d postgres:8
      docker run -P --link postgres_cnt:db fabric8/shootout-docker-maven-spotify:0.0.1-SNAPSHOT

Finally you can run the integration test:

      mvn -Dlog.url=http://localhost:49162 integration-test

(replace 49162 with the dynamic port as shown by "docker ps" and "localhost" with the your docker host)
