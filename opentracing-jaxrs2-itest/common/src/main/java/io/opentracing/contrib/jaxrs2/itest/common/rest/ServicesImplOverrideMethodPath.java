package io.opentracing.contrib.jaxrs2.itest.common.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Pavol Loffay
 */
@Path("/foo/")
public class ServicesImplOverrideMethodPath implements ServicesInterface {


  @GET // when the path is overridden it requires to add @GET
  @Path("/override/{id}")
  @Override
  public void method(@PathParam("id") String id) {
  }
}
