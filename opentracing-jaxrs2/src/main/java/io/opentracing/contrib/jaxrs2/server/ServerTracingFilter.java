package io.opentracing.contrib.jaxrs2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
@Priority(Priorities.HEADER_DECORATOR)
public class ServerTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger log = Logger.getLogger(ServerTracingFilter.class.getName());

    protected static final String SPAN_PROP_ID = ServerTracingFilter.class.getName() + ".activeServerSpan";

    private Tracer tracer;
    private String operationName;
    private List<ServerSpanDecorator> spanDecorators;

    protected ServerTracingFilter(Tracer tracer, String operationName,
                               List<ServerSpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.operationName = operationName;
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // return in case filter if registered twice
        if (requestContext.getProperty(SPAN_PROP_ID) != null) {
            return;
        }

        if (tracer != null) {
            SpanContext extractedSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                    new ServerHeadersExtractTextMap(requestContext.getHeaders()));

            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(requestContext.getMethod())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

            if (extractedSpanContext != null) {
                spanBuilder.asChildOf(extractedSpanContext);
            }

            Span span = spanBuilder.start();

            if (spanDecorators != null) {
                for (ServerSpanDecorator decorator: spanDecorators) {
                    decorator.decorateRequest(requestContext, span);
                }
            }

            // override operation name set by @Traced
            if (this.operationName != null) {
                span.setOperationName(operationName);
            }

            if (log.isLoggable(Level.FINEST)) {
                log.finest("Creating server span: " + operationName);
            }

            requestContext.setProperty(SPAN_PROP_ID, new SpanWrapper(span));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        SpanWrapper spanWrapper = CastUtils
                .cast(requestContext.getProperty(ServerTracingFilter.SPAN_PROP_ID), SpanWrapper.class);

        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing server span");

            if (spanDecorators != null) {
                for (ServerSpanDecorator decorator: spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.get());
                }
            }

            spanWrapper.finish();
        }
    }
}
