package io.opentracing.contrib.jaxrs.itest.auto.discovery;

import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;

/**
 * @author Pavol Loffay
 */
@ApplicationPath("/")
public class JaxrsApplication extends Application {

    public static final MockTracer mockTracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);

    public JaxrsApplication() throws NamingException {
        GlobalTracer.register(mockTracer);
    }
}
