# Dockerfile used by alexec/docker-maven-plugin
FROM java:8

MAINTAINER rhuss@redhat.com

EXPOSE 8080

# Note that there is no variable replacement available for this plugin
ADD ${project.build.finalName}.jar /opt/shootout-docker-maven-alexec.jar

# Workaround: Sleep 2 seconds in order to allow the DB image to startup
CMD env && sleep 2 && java -Djava.security.egd=file:/dev/./urandom -jar /opt/shootout-docker-maven-alexec.jar
