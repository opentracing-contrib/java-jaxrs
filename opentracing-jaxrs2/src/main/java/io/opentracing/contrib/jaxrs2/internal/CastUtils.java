package io.opentracing.contrib.jaxrs2.internal;

import java.util.logging.Logger;

/**
 * @author Pavol Loffay
 */
public class CastUtils {
    private static final Logger log = Logger.getLogger(CastUtils.class.getName());

    private CastUtils() {}

    /**
     * Casts given object to the given class.
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return casted object, or null if there is any error
     */
    public static <T> T cast(Object object, Class<T> clazz) {
        if (object == null || clazz == null) {
            return null;
        }

        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            log.severe("Cannot cast to " + clazz.getName());
            return null;
        }
    }
}
