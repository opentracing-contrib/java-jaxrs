package io.opentracing.contrib.jaxrs2.server;

import static io.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import io.opentracing.contrib.jaxrs2.serialization.TracingInterceptor;
import java.util.List;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.InterceptorContext;

@Priority(Priorities.ENTITY_CODER)
public class ServerTracingInterceptor extends TracingInterceptor {
    /**
     * Apache CFX does not seem to publish the PROPERTY_NAME into the Interceptor context.
     * Use the current HttpServletRequest to lookup the current span wrapper.
     */
    @Context
    private HttpServletRequest servletReq;

    public ServerTracingInterceptor(Tracer tracer, List<InterceptorSpanDecorator> spanDecorators) {
        super(tracer, spanDecorators);
    }

    @Override
    protected SpanWrapper findSpan(InterceptorContext context) {
        SpanWrapper spanWrapper = CastUtils.cast(context.getProperty(PROPERTY_NAME), SpanWrapper.class);
        if(spanWrapper == null && servletReq != null) {
            spanWrapper = CastUtils
                .cast(servletReq.getAttribute(PROPERTY_NAME), SpanWrapper.class);
        }
        return spanWrapper;
    }
}
