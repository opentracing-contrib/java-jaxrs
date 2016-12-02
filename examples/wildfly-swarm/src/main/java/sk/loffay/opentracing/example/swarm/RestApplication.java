package sk.loffay.opentracing.example.swarm;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.hawkular.apm.client.opentracing.APMTracer;

import io.opentracing.Tracer;

import sk.loffay.opentracing.jax.rs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
@ApplicationPath("/wildfly-swarm")
public class RestApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        Tracer tracer = new APMTracer();

        objects.add(ServerTracingDynamicFeature.Builder
                .traceAll(tracer)
                .build());

        objects.add(new HelloHandler(tracer));
        return objects;
    }
}
