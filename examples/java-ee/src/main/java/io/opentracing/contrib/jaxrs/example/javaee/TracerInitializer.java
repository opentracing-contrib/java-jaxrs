package io.opentracing.contrib.jaxrs.example.javaee;


import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.logging.Logger;

/**
 * This is not part of the "business" of this example, but can be used as an example of how to register a Tracer
 * with the GlobalTracer. On some platforms, like Wildfly Swarm, this is done automatically for you.
 *
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
public class TracerInitializer {
    private static final Logger log = Logger.getLogger(TracerInitializer.class.getName());

    @PostConstruct
    public void init() {
        if (GlobalTracer.isRegistered()) {
            log.info("A Tracer is already registered at the GlobalTracer. Skipping resolution via TraceResolver.");
            return;
        }

        Tracer tracer = TracerResolver.resolveTracer();
        if (null == tracer) {
            log.info("Could not get a valid OpenTracing Tracer from the classpath. Skipping.");
            return;
        }

        log.info(String.format("Registering %s as the OpenTracing Tracer", tracer.getClass().getName()));
        GlobalTracer.register(tracer);
    }
}