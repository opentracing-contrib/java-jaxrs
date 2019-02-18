package io.opentracing.contrib.jaxrs2.itest.common.rest;

import javax.ws.rs.Path;

/**
 * @author Pavol Loffay
 */
@Path("override")
public class ServicesImplOverrideClassPath implements ServicesInterface {

  @Override
  public void method(String id) {
  }
}
