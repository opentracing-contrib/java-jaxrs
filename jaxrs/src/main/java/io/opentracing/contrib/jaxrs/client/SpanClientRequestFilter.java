package io.opentracing.contrib.jaxrs.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;

/**
 * @author Pavol Loffay
 */
public class SpanClientRequestFilter implements ClientRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SpanClientRequestFilter.class);

    public static final String SPAN_PROP_ID = "currentClientSpan";

    private Tracer tracer;
    private Optional<List<SpanDecorator>> spanDecorators;

    public SpanClientRequestFilter(Tracer tracer, List<SpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.spanDecorators = Optional.ofNullable(spanDecorators);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Boolean tracingDisabled = (Boolean) requestContext.getProperty(TracingProperties.TRACING_DISABLED);
        if (tracingDisabled != null && tracingDisabled) {
            log.trace("Client tracing disabled");
            return;
        }

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(requestContext.getUri().getPath());

        Span parentSpan = (Span)requestContext.getProperty(TracingProperties.PARENT_SPAN);
        if (parentSpan != null) {
            spanBuilder.asChildOf(parentSpan);
            log.trace("Client will be childOf: {}", parentSpan);
        }

        Span span = spanBuilder.start();
        spanDecorators.ifPresent(decorators ->
                decorators.forEach(decorator -> decorator.decorateRequest(requestContext, span)));
        log.trace("Starting client span: {}", span);

        injectHeadersToRequest(requestContext, span);
        requestContext.setProperty(SPAN_PROP_ID, span);
    }

    private void injectHeadersToRequest(ClientRequestContext clientRequestContext, Span span) {
        Map<String, String> spanContext = new HashMap<>();
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMapInjectAdapter(spanContext));

        spanContext.forEach((key, value) ->
                clientRequestContext.getHeaders().add(key, value));
    }
}
