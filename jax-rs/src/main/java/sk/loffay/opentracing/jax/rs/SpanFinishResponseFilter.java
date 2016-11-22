package sk.loffay.opentracing.jax.rs;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import io.opentracing.Span;

@Provider
public class SpanFinishResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        Span span = SpanExtractRequestFilter.threadLocalSpan.get();
        if (span != null) {
            span.finish();
        }
    }
}
