package tech.picnic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.testng.annotations.Test;
import tech.picnic.TestWebConfigTest.TestApplication;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = TestApplication.class,
    properties = "security.basic.enabled=false")
public final class TestWebConfigTest extends AbstractTestNGSpringContextTests {
  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  public void testExceptionBecomesJson() {
    verifyInternalServerErrorResponse(get("get-error", MediaType.APPLICATION_JSON));
  }

  private RequestEntity<Void> get(String path, MediaType... acceptedTypes) {
    return getWithParams(path, ImmutableMap.of(), acceptedTypes);
  }

  private RequestEntity<Void> getWithParams(
      String path, ImmutableMap<String, String> params, MediaType... acceptedTypes) {
    return RequestEntity.get(getUri(path, params)).accept(acceptedTypes).build();
  }

  private URI getUri(String path, ImmutableMap<String, String> params) {
    return UriComponentsBuilder.newInstance()
        .scheme("http")
        .host("localhost")
        .port(port)
        .path(path)
        .build()
        .toUri();
  }

  private void verifyInternalServerErrorResponse(RequestEntity<?> requestEntity) {
    ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
    assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    // When using Jetty 9.4.21 the assertion below fails because the default error response uses
    // "url" instead of "path"
    assertTrue(response.getBody().contains("\"path\":\"/get-error\""));
  }

  /** Test application. */
  @SpringBootApplication
  @Import(TestController.class)
  public static class TestApplication {
    public static void main(String[] args) {
      SpringApplication.run(TestApplication.class, args);
    }
  }

  @RestController
  static final class TestController {
    @GetMapping("get-error")
    String getTestError() {
      throw new RuntimeException("foobar");
    }
  }
}
