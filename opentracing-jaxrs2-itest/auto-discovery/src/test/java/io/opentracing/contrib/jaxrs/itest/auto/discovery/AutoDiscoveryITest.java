package io.opentracing.contrib.jaxrs.itest.auto.discovery;

import io.opentracing.mock.MockSpan;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Pavol Loffay
 */
@RunAsClient
@RunWith(Arquillian.class)
public class AutoDiscoveryITest {


    @ArquillianResource
    protected URL deploymentURL;

    @Deployment
    public static WebArchive createDeployment() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
            .importRuntimeDependencies().resolve().withTransitivity().asFile();

        return ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClasses(JaxrsApplication.class, HelloWorldEndpoint.class)
            .addAsLibraries(files)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeClass
    public static void initResteasyClient() {
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }

    @Test
    public void testRequestIsTraced() throws URISyntaxException {
        Client client = ClientBuilder.newClient();

        String url = deploymentURL.toString() + "hello";
        Response response = client.target(url)
            .request()
            .get();

        Assert.assertEquals(MockSpan.class.getName(), response.readEntity(String.class));
        response.close();
        client.close();
    }
}
