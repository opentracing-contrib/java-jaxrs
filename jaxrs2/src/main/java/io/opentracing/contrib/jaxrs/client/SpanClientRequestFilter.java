package io.opentracing.contrib.jaxrs.client;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.SpanWrapper;
import io.opentracing.contrib.jaxrs.URLUtils;
import io.opentracing.propagation.Format;

/**
 * @author Pavol Loffay
 */
public class SpanClientRequestFilter implements ClientRequestFilter {

    private static final Logger log = Logger.getLogger(SpanClientRequestFilter.class.getName());

    public static final String SPAN_PROP_ID = "currentClientSpan";

    private Tracer tracer;
    private List<SpanDecorator> spanDecorators;

    public SpanClientRequestFilter(Tracer tracer, List<SpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.spanDecorators = spanDecorators;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Boolean tracingDisabled = (Boolean) requestContext.getProperty(TracingProperties.TRACING_DISABLED);
        if (tracingDisabled != null && tracingDisabled) {
            log.finest("Client tracing disabled");
            return;
        }

        // in case filter is registered twice
        if (requestContext.getProperty(SPAN_PROP_ID) != null) {
            return;
        }

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(URLUtils.path(requestContext.getUri()));

        Span parentSpan = (Span)requestContext.getProperty(TracingProperties.CHILD_OF);
        if (parentSpan != null) {
            spanBuilder.asChildOf(parentSpan);
        }

        Span span = spanBuilder.start();

        if (spanDecorators != null) {
            for (SpanDecorator decorator: spanDecorators) {
                decorator.decorateRequest(requestContext, span);
            }
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest("Starting client span");
        }

        injectHeadersToRequest(requestContext, span);
        requestContext.setProperty(SPAN_PROP_ID, new SpanWrapper(span));
    }

    private void injectHeadersToRequest(ClientRequestContext clientRequestContext, Span span) {
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new ClientHeadersInjectTextMap(clientRequestContext.getHeaders()));
    }
}
