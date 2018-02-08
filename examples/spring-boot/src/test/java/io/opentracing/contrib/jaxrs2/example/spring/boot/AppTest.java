package io.opentracing.contrib.jaxrs2.example.spring.boot;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Pavol Loffay
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AppTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private LoggingTracer loggingTracer;

  @Test
  public void contextLoads() {
    ResponseEntity<String> response = restTemplate.getForEntity("/hello/1", String.class);
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, loggingTracer.finishedSpans().size());
  }
}
