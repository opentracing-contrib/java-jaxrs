package io.opentracing.contrib.jaxrs.example.swarm;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Pavol Loffay
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Swarm swarm = new Swarm(args);

        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addPackage(Main.class.getPackage());
        deployment.addAllDependencies();

        swarm.start().deploy(deployment);
    }
}
