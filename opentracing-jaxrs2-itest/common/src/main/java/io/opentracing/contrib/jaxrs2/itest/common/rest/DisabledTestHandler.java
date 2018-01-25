package io.opentracing.contrib.jaxrs2.itest.common.rest;

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.opentracing.Traced;
import org.junit.Assert;

/**
 * @author Pavol Loffay
 */
@Path("/tracedFalse")
@Traced(value = false, operationName = "tracedFalse")
public class DisabledTestHandler {

  private final Tracer tracer;

  public DisabledTestHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  @GET
  @Path("/foo")
  public Response disabled() {
    assertNoActiveSpan();
    return Response.status(Response.Status.OK).build();
  }

  @Traced
  @GET
  @Path("/enabled")
  public Response helloMethod() {
    assertActiveSpan();
    return Response.status(Response.Status.OK).build();
  }

  private void assertNoActiveSpan() {
    if (!(tracer instanceof NoopTracer)) {
      Assert.assertNull(tracer.scopeManager().active());
    }
  }

  private void assertActiveSpan() {
    if (!(tracer instanceof NoopTracer)) {
      Assert.assertNotNull(tracer.scopeManager().active());
    }
  }
}
