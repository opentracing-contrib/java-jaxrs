package io.opentracing.contrib.jaxrs.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.SpanWrapper;
import io.opentracing.contrib.jaxrs.URLUtils;
import io.opentracing.propagation.Format;

/**
 * @author Pavol Loffay
 */
public class SpanServerRequestFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(SpanServerRequestFilter.class.getName());

    public static final String SPAN_PROP_ID = "currentServerSpan";

    private Tracer tracer;
    private String operationName;
    private List<SpanDecorator> spanDecorators;

    public SpanServerRequestFilter(Tracer tracer, String operationName, List<SpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.operationName = operationName;
        this.spanDecorators = spanDecorators;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // return in case filter if registered twice
        if (requestContext.getProperty(SPAN_PROP_ID) != null) {
            return;
        }

        if (tracer != null) {
            SpanContext extractedSpanContext = tracer.extract(Format.Builtin.TEXT_MAP,
                    new ServerHeadersExtractTextMap(requestContext.getHeaders()));

            String operationName = this.operationName != null ? this.operationName :
                    URLUtils.path(requestContext.getUriInfo().getRequestUri());

            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName);

            if (extractedSpanContext != null) {
                spanBuilder.asChildOf(extractedSpanContext);
            }

            Span span = spanBuilder.start();

            if (spanDecorators != null) {
                for (SpanDecorator decorator: spanDecorators) {
                    decorator.decorateRequest(requestContext, span);
                }
            }

            if (log.isLoggable(Level.FINEST)) {
                log.finest("Creating server span: " + operationName);
            }

            requestContext.setProperty(SPAN_PROP_ID, new SpanWrapper(span));
        }
    }
}
