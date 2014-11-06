package org.jolokia.docker.maven.sample.jolokia;

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
            throw new IllegalArgumentException("Cannot extract service url as system property");
        }
        return ret;
    }

}
