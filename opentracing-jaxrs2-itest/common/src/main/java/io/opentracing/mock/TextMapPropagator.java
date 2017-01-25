package io.opentracing.mock;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import io.opentracing.contrib.jaxrs2.client.ClientHeadersInjectTextMap;
import io.opentracing.contrib.jaxrs2.server.ServerHeadersExtractTextMap;
import io.opentracing.propagation.Format;

public class TextMapPropagator implements MockTracer.Propagator {

    private static final String SPAN_ID = "spanid";
    private static final String TRACE_ID = "traceid";

    @Override
    public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
        if (format.equals(Format.Builtin.HTTP_HEADERS)) {
            ClientHeadersInjectTextMap injectAdapter = (ClientHeadersInjectTextMap) carrier;
            injectAdapter.put(SPAN_ID, String.valueOf(ctx.spanId()));
            injectAdapter.put(TRACE_ID, String.valueOf(ctx.traceId()));
        } else {
            throw new IllegalArgumentException("Unknown format");
        }
    }

    @Override
    public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
        Long traceId = null;
        Long spanId = null;

        if (format.equals(Format.Builtin.HTTP_HEADERS)) {
            ServerHeadersExtractTextMap extractAdapter = (ServerHeadersExtractTextMap) carrier;
            Iterator<Map.Entry<String, String>> iterator = extractAdapter.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (TRACE_ID.equals(entry.getKey())) {
                    traceId = Long.valueOf(entry.getValue());
                } else if (SPAN_ID.equals(entry.getKey())) {
                    spanId = Long.valueOf(entry.getValue());
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown format");
        }

        if (traceId != null && spanId != null) {
            return new MockSpan.MockContext(traceId, spanId, Collections.<String, String>emptyMap());
        }

        return null;
    }
}

