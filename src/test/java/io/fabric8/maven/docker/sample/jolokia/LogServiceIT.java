package io.fabric8.maven.docker.sample.jolokia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * @author roland
 * @since 15.05.14
 */
public class LogServiceIT {

    @Test
    public void testLog() {

        String baseUrl = extractUrl();
        long nonce = (int) (Math.random() * 10000);
        System.out.println("Checking URL: " + baseUrl);

        RestAssured.baseURI = baseUrl;
        RestAssured.defaultParser = Parser.TEXT;
        String answer = get("/" + nonce).asString();
        System.out.println("Response: " + answer);

        given()
                .param("mimeType", "application/json")
                .get("/" + nonce)
        .then().assertThat()
                .header("content-type", containsString("text/plain"))
                .body(containsString(nonce + ""));
    }

    private String extractUrl() {
        String ret = System.getProperty("log.url");
        if (ret == null) {
            // This fallback is used for alexec/docker-maven-plugin since there is no
            // way to get the Docker host adress (only ips on the internal docke network)
            // So lets try to fetch it on our own.
            String port = System.getProperty("log.port");
            if (port == null) {
                throw new IllegalArgumentException("Cannot extract service url as system property");
            }
            String host = System.getenv("DOCKER_HOST");
            if (host == null) {
                throw new IllegalArgumentException("Cannot extract docker host address");
            }
            Matcher matcher = Pattern.compile("^tcp://([\\d.]+):\\d+$").matcher(host);
            if (matcher.matches()) {
                return "http://" + matcher.group(1) + ":" + port;
            } else {
                throw new IllegalArgumentException(host + " doesn't match as DOCKER_HOST");
            }
        } else {
            return ret;
        }
    }

}
