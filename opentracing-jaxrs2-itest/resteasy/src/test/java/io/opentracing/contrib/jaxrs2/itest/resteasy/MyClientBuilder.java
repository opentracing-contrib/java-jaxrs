package io.opentracing.contrib.jaxrs2.itest.resteasy;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * @author Pavol Loffay
 */
public class MyClientBuilder extends ResteasyClientBuilder {

  public MyClientBuilder() {
  System.out.println("\n\n\n\nMyClientBuilder\n\n\n\n\n");
  }
}
