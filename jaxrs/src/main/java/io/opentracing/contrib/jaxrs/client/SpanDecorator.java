package io.opentracing.contrib.jaxrs.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface SpanDecorator {

    void decorateRequest(ClientRequestContext containerRequestContext, Span span);

    void decorateResponse(ClientResponseContext containerRequestContext, Span span);

    SpanDecorator STANDARD_TAGS = new SpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext requestContext, Span span) {
            span.setTag("http.method", requestContext.getMethod())
                .setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

            Optional<URL> url = toURL(requestContext.getUri());
            url.ifPresent(url1 -> span.setTag(Tags.HTTP_URL.getKey(), url1.toString()));
        }

        @Override
        public void decorateResponse(ClientResponseContext responseContext, Span span) {
            span.setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus());
        }

        private Optional<URL> toURL(URI uri) {
            URL url = null;

            try {
                url = uri.toURL();
            } catch (MalformedURLException e) {}

            return Optional.ofNullable(url);
        }
    };


}
