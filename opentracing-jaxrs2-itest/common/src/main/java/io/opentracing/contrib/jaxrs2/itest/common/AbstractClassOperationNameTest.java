package io.opentracing.contrib.jaxrs2.itest.common;

import static org.awaitility.Awaitility.await;

import io.opentracing.contrib.jaxrs2.itest.common.rest.TestHandler;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.ClassNameOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractClassOperationNameTest extends AbstractJettyTest {
  @Override
  protected void initTracing(ServletContextHandler context) {
    client.register(new Builder(mockTracer).build());

    ServerTracingDynamicFeature serverTracingBuilder =
        new ServerTracingDynamicFeature.Builder(mockTracer)
            .withOperationNameProvider(ClassNameOperationName.newBuilder())
            .build();
    context.addFilter(new FilterHolder(new SpanFinishingFilter(mockTracer)),
        "/*", EnumSet.of(DispatcherType.REQUEST));

    context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
    context.setAttribute(CLIENT_ATTRIBUTE, client);
    context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
  }

  @Test
  public void testHelloEndpoint() {
    Client client = ClientBuilder.newClient();
    Response response = client.target(url("/hello/1"))
        .request()
        .get();
    response.close();
    await().until(finishedSpansSizeEquals(1));

    List<MockSpan> mockSpans = mockTracer.finishedSpans();
    Assert.assertEquals(1, mockSpans.size());
    assertOnErrors(mockTracer.finishedSpans());
    Assert.assertEquals(String.format("GET:%s.helloMethod", TestHandler.class.getName()), mockSpans.get(0).operationName());
  }
}
