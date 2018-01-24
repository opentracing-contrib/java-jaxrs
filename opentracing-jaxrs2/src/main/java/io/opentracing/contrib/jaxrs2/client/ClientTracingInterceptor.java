package io.opentracing.contrib.jaxrs2.client;

import static io.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import io.opentracing.contrib.jaxrs2.serialization.TracingInterceptor;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.InterceptorContext;

@Priority(Priorities.ENTITY_CODER)
public class ClientTracingInterceptor extends TracingInterceptor {

    public ClientTracingInterceptor(Tracer tracer, List<InterceptorSpanDecorator> spanDecorators) {
        super(tracer, spanDecorators);
    }

    @Override
    protected SpanWrapper findSpan(InterceptorContext context) {
        return CastUtils.cast(context.getProperty(PROPERTY_NAME), SpanWrapper.class);
    }
}
