package io.opentracing.contrib.jaxrs2.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author Pavol Loffay
 */
public class URIUtils {
    private URIUtils() {}

    /**
     * Returns path of given URI. If the first character of path is '/' then it is removed.
     *
     * @param uri
     * @return path or null
     */
    public static String path(URI uri) {
        String path = uri.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * Returns string representation of supplied URL.
     *
     * @param uri
     * @return string URL or null
     */
    public static String url(URI uri) {
        String urlStr = null;
        try {
            URL url = uri.toURL();
            urlStr = url.toString();
        } catch (MalformedURLException e) {
            // ignoring returning null
        }

        return urlStr;
    }
}
