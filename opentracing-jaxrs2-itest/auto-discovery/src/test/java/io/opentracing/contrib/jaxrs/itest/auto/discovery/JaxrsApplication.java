package io.opentracing.contrib.jaxrs.itest.auto.discovery;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
@ApplicationPath("/")
public class JaxrsApplication extends Application {

    private static final MockTracer mockTracer = new MockTracer();

    public JaxrsApplication() {
        GlobalTracer.registerIfAbsent(mockTracer);
    }
}
