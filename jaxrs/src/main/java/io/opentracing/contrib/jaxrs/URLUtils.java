package io.opentracing.contrib.jaxrs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author Pavol Loffay
 */
public class URLUtils {
    private URLUtils() {}

    public static String path(URI uri) {
        String path = uri.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    public static String url(URI uri) {
        String urlStr = null;
        try {
            URL url = uri.toURL();
            urlStr = url.toString();
        } catch (MalformedURLException e) {
        }

        return urlStr;
    }
}
