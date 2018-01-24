package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractWildcardOperationNameTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class JerseyWildcardOperationNameITest extends AbstractWildcardOperationNameTest {

    @Override
    public void initServletContext(ServletContextHandler context) {
        JerseyHelper.initServletContext(context);
    }
}
