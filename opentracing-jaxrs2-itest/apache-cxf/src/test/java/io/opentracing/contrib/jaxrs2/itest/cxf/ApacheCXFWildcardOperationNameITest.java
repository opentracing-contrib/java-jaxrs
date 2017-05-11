package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractWildcardOperationNameTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFWildcardOperationNameITest extends AbstractWildcardOperationNameTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        ApacheCXFHelper.initServletContext(context);
    }

}
