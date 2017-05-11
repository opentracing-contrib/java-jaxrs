package io.opentracing.contrib.jaxrs2.itest.resteasy;

import org.eclipse.jetty.servlet.ServletContextHandler;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerDefaultConfigurationTest;

/**
 * @author Pavol Loffay
 */
public class RestEasyServerDefaultConfigurationITest extends AbstractServerDefaultConfigurationTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }
}
