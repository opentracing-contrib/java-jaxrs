package io.opentracing.contrib.jaxrs2.itest.jersey;

import org.eclipse.jetty.servlet.ServletContextHandler;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;

/**
 * @author Pavol Loffay
 */
public class JerseyClientITest extends AbstractClientTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        JerseyHelper.initServletContext(context);
    }
}
