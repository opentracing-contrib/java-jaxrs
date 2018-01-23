package io.opentracing.contrib.jaxrs2.client;

import io.opentracing.propagation.TextMap;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Helper class used to add carrier data to HTTP headers.
 *
 * @author Pavol Loffay
 */
public class ClientHeadersInjectTextMap implements TextMap {

    private final MultivaluedMap<String, Object> headers;

    public ClientHeadersInjectTextMap(MultivaluedMap<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException(
                ClientHeadersInjectTextMap.class.getName() +" should only be used with Tracer.inject()");
    }

    @Override
    public void put(String key, String value) {
        headers.add(key, value);
    }

}
