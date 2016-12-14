package io.opentracing.contrib.jaxrs.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.SpanWrapper;
import io.opentracing.contrib.jaxrs.URLUtils;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

/**
 * @author Pavol Loffay
 */
public class SpanServerRequestFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SpanServerRequestFilter.class);

    public static final String SPAN_PROP_ID = "currentServerSpan";

    private Tracer tracer;
    private Optional<String> operationName;
    private Optional<List<SpanDecorator>> spanDecorators;

    public SpanServerRequestFilter(Tracer tracer, String operationName, List<SpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.operationName = Optional.ofNullable(operationName);
        this.spanDecorators = Optional.ofNullable(spanDecorators);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // return in case filter if registered twice
        if (requestContext.getProperty(SPAN_PROP_ID) != null) {
            return;
        }

        if (tracer != null) {
            SpanContext extractedSpanContext = tracer.extract(Format.Builtin.TEXT_MAP,
                    new TextMapExtractAdapter(toMap(requestContext.getHeaders())));

            Tracer.SpanBuilder spanBuilder =
                    tracer.buildSpan(operationName.orElse(
                            URLUtils.path(requestContext.getUriInfo().getRequestUri()).get()));

            if (extractedSpanContext != null) {
                spanBuilder.asChildOf(extractedSpanContext);
            }

            Span span = spanBuilder.start();

            spanDecorators.ifPresent(decorators ->
                    decorators.forEach(decorator -> decorator.decorateRequest(requestContext, span)));

            log.trace("Creating server span: {}", span);

            requestContext.setProperty(SPAN_PROP_ID, new SpanWrapper(span));
        }
    }

    private static Map<String, String> toMap(MultivaluedMap<String, String> multivaluedMap) {
        return multivaluedMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 0 && entry.getValue().get(0) != null)
                .collect(Collectors.toMap(key -> key.getKey(), value -> value.getValue().get(0).toString()));
    }
}
