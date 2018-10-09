package io.opentracing.contrib.jaxrs2.itest.resteasy;

import static org.awaitility.Awaitility.await;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerTest;
import io.opentracing.mock.MockSpan;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class RestEasyITest extends AbstractServerTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }

    /**
     * TODO resteasy does not call onComplete callback nor propagate exception to filter.
     */
    @Test
    @Override
    public void testAsyncError() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/asyncError"))
            .request()
            .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockSpans);
    }
}
