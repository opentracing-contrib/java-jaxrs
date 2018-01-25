package io.opentracing.contrib.jaxrs2.server;

import static io.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

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

    protected ServerTracingFilter(
        Tracer tracer,
        String operationName,
        List<ServerSpanDecorator> spanDecorators,
        OperationNameProvider operationNameProvider,
        Pattern skipPattern) {
        this.tracer = tracer;
        this.operationName = operationName;
        this.spanDecorators = new ArrayList<>(spanDecorators);
        this.operationNameProvider = operationNameProvider;
        this.skipPattern = skipPattern;
    }

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // return in case filter if registered twice
        if (requestContext.getProperty(PROPERTY_NAME) != null || matchesSkipPattern(requestContext)) {
            return;
        }

        if (tracer != null) {
            SpanContext extractedSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                    new ServerHeadersExtractTextMap(requestContext.getHeaders()));

            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationNameProvider.operationName(requestContext))
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

            if (extractedSpanContext != null) {
                spanBuilder.asChildOf(extractedSpanContext);
            }

            Span span = spanBuilder.startActive(false).span();

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

            requestContext.setProperty(PROPERTY_NAME, new SpanWrapper(span));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        SpanWrapper spanWrapper = CastUtils.cast(requestContext.getProperty(PROPERTY_NAME), SpanWrapper.class);
        if (spanWrapper == null) {
            return;
        }

        if (spanDecorators != null) {
            for (ServerSpanDecorator decorator: spanDecorators) {
                decorator.decorateResponse(responseContext, spanWrapper.get());
            }
        }
    }

    private boolean matchesSkipPattern(ContainerRequestContext requestContext) {
        // skip URLs matching skip pattern
        // e.g. pattern is defined as '/health|/status' then URL 'http://localhost:5000/context/health' won't be traced
        if (skipPattern != null) {
            String path = requestContext.getUriInfo().getPath();
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }
            return skipPattern.matcher(path).matches();
        }
        return false;
    }
}
