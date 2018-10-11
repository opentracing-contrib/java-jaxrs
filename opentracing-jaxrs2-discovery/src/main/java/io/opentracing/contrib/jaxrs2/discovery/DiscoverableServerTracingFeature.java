package io.opentracing.contrib.jaxrs2.discovery;

import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.util.GlobalTracer;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Pavol Loffay
 */
@Provider
public class DiscoverableServerTracingFeature implements DynamicFeature {

  private final ServerTracingDynamicFeature tracingFeature;

  public DiscoverableServerTracingFeature() {
    this.tracingFeature = new ServerTracingDynamicFeature.Builder(GlobalTracer.get())
        .withTraceSerialization(false)
        .build();
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    tracingFeature.configure(resourceInfo, context);
  }
}
