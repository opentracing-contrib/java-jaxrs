package sk.loffay.opentracing.jax.rs.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public class SpanClientResponseFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Span span = SpanClientRequestFilter.threadLocalSpan.get();
        if (span != null) {
            span.setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus())
                .finish();
        }

    }
}
