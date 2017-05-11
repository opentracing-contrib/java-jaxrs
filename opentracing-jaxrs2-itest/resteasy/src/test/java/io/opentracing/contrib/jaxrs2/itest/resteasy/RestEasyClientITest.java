package io.opentracing.contrib.jaxrs2.itest.resteasy;

import org.eclipse.jetty.servlet.ServletContextHandler;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;

/**
 * @author Pavol Loffay
 */
public class RestEasyClientITest extends AbstractClientTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }
}
