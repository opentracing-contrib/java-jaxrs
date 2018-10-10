package io.opentracing.contrib.jaxrs2.itest.resteasy;

import static org.awaitility.Awaitility.await;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerTest;
import io.opentracing.mock.MockSpan;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.junit.Assert;
import org.junit.Ignore;
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
    @Ignore
    @Override
    public void testAsyncError() {
    }

    /**
     * A substitution for {@link #testAsyncError()}. It test that span is reported.
     */
    @Test
    public void testAsyncErrorTestSpanReported() {
        try {
            // disable retry otherwise there can be 2 spans
            CloseableHttpClient build = HttpClientBuilder.create().disableAutomaticRetries().build();
            Client client = new ResteasyClientBuilder().httpEngine(new ApacheHttpClient4Engine(build)).build();
            Response response = client.target(url("/asyncError"))
                .request()
                .get();
            response.close();
            response.readEntity(String.class);
            client.close();
        } catch (Exception ex) {
            // client throws an exception if async request fails
        }
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockSpans);
    }
}
