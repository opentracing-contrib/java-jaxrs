package io.opentracing.contrib.jaxrs.example.javaee;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.logging.Logger;

/**
 * This is a regular JAX-RS endpoint. Each call to the placeOrder method will be automatically traced.
 */
@Path("/order")
public class Endpoint {
    private static final Logger log = Logger.getLogger(Endpoint.class.getName());

    @POST
    @Path("/")
    public String placeOrder() {
        log.info("Request received to place an order");
        return "Order placed";
    }

}
