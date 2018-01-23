package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerDefaultConfigurationTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class JerseyServerDefaultConfigurationITest extends AbstractServerDefaultConfigurationTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        JerseyHelper.initServletContext(context);
    }
}
