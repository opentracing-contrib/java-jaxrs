package io.opentracing.contrib.jaxrs2.itest.common.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Pavol Loffay
 */
@Path("/services")
public interface ServicesInterface {

  @GET
  @Path("method/{id}")
  void method(@PathParam("id") String id);
}
