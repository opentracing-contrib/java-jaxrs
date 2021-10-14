package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static io.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

/**
 * @author Pavol Loffay
 */
@Priority(Priorities.HEADER_DECORATOR)
public class ServerTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger log = Logger.getLogger(ServerTracingFilter.class.getName());

    private Tracer tracer;
    private List<ServerSpanDecorator> spanDecorators;
    private String operationName;
    private OperationNameProvider operationNameProvider;
    private Pattern skipPattern;
    private final boolean joinExistingActiveSpan;

    protected ServerTracingFilter(
        Tracer tracer,
        String operationName,
        List<ServerSpanDecorator> spanDecorators,
        OperationNameProvider operationNameProvider,
        Pattern skipPattern,
        boolean joinExistingActiveSpan) {
        this.tracer = tracer;
        this.operationName = operationName;
        this.spanDecorators = new ArrayList<>(spanDecorators);
        this.operationNameProvider = operationNameProvider;
        this.skipPattern = skipPattern;
        this.joinExistingActiveSpan = joinExistingActiveSpan;
    }

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // return in case filter if registered twice
        if (requestContext.getProperty(PROPERTY_NAME) != null || matchesSkipPattern(requestContext)) {
            return;
        }

        if (tracer != null) {

            SpanContext parentSpanContext = parentSpanContext(requestContext);
            Span span = tracer.buildSpan(operationNameProvider.operationName(requestContext))
                    .ignoreActiveSpan()
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                    .asChildOf(parentSpanContext)
                    .start();

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

            requestContext.setProperty(PROPERTY_NAME, new SpanWrapper(span, tracer.activateSpan(span)));
        }
    }

    /**
     * Returns a parent for a span created by this filter (jax-rs span).
     * The context from the active span takes precedence over context in the request,
     * but only if joinExistingActiveSpan has been set.
     * The current active span should be child-of extracted context and for example
     * created at a lower level e.g. jersey filter.
     */
    private SpanContext parentSpanContext(ContainerRequestContext requestContext) {
        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null && this.joinExistingActiveSpan) {
            return activeSpan.context();
        } else {
            return tracer.extract(
                    Format.Builtin.HTTP_HEADERS,
                    new ServerHeadersExtractTextMap(requestContext.getHeaders())
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        SpanWrapper spanWrapper = CastUtils.cast(requestContext.getProperty(PROPERTY_NAME), SpanWrapper.class);
        if (spanWrapper == null) {
            return;
        }

        if (spanDecorators != null) {
            for (ServerSpanDecorator decorator: spanDecorators) {
                decorator.decorateResponse(requestContext, responseContext, spanWrapper.get());
            }
        }
    }

    private boolean matchesSkipPattern(ContainerRequestContext requestContext) {
        // skip URLs matching skip pattern
        // e.g. pattern is defined as '/health|/status' then URL 'http://localhost:5000/context/health' won't be traced
        String path = requestContext.getUriInfo().getPath();
        if (skipPattern != null && path != null) {
            if (path.length() > 0 && path.charAt(0) != '/') {
                path = "/" + path;
            }
            return skipPattern.matcher(path).matches();
        }
        return false;
    }
}
