package io.opentracing.contrib.jaxrs.example.swarm;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Pavol Loffay
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Instantiate the container
        Swarm swarm = new Swarm(args);

        // Create one or more deployments
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);

        // Add resource to deployment
        deployment.addClass(HelloHandler.class);

        swarm.fraction(new UndertowFraction().httpPort(3000));

        swarm.socketBinding("ds", new SocketBinding("ds").port(3000));
        swarm.start();
        swarm.deploy(deployment);
    }
}
