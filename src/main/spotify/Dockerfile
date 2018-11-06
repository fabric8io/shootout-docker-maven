# Dockerfile used by spotify/docker-maven-plugin
FROM java:8

MAINTAINER rhuss@redhat.com

EXPOSE 8080

# No variable replacement yet, so the full artefact name has to be repeated
ADD shootout-docker-maven-0.0.1-SNAPSHOT.jar /opt/

CMD java -Djava.security.egd=file:/dev/./urandom -jar /opt/shootout-docker-maven-0.0.1-SNAPSHOT.jar
