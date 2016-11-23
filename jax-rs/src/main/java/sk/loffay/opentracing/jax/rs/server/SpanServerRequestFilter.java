package sk.loffay.opentracing.jax.rs.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
@Provider
public class SpanServerRequestFilter implements ContainerRequestFilter {

    public static ThreadLocal<Span> threadLocalSpan = new ThreadLocal<>();

    @Inject
    private Tracer tracer;

    public SpanServerRequestFilter() {}

    public SpanServerRequestFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (tracer != null) {
            SpanContext extractedSpanContext = tracer.extract(Format.Builtin.TEXT_MAP,
                    new TextMapExtractAdapter(toMap(requestContext.getHeaders())));

            Span currentSpan = tracer.buildSpan(requestContext.getUriInfo().getPath().toString())
                    .asChildOf(extractedSpanContext)
                    .withTag(Tags.HTTP_URL.getKey(), requestContext.getUriInfo().getAbsolutePath().toString())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                    .start();

            threadLocalSpan.set(currentSpan);
        }
    }

    private static Map<String, String> toMap(MultivaluedMap<String, String> multivaluedMap) {
        HashMap<String, String> map = new HashMap<>(multivaluedMap.size());

        multivaluedMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 0)
                .forEach(entry ->  map.put(entry.getKey(), entry.getValue().get(0).toString()));

        return map;
    }
}
