package io.opentracing.contrib.jaxrs.itest.auto.discovery;

import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import io.opentracing.mock.MockTracer;

/**
 * @author Pavol Loffay
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.WAR)
public class SwarmAutoDiscoveryITest {

    private static MockTracer mockTracer = JaxrsApplication.mockTracer;

    @Test
    public void testRequestIsTraced() throws InterruptedException, NamingException {
        Client client = ClientBuilder.newClient();

        client.target("http://localhost:8080/hello")
                .request()
                .get();

        client.close();

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
    }
}
