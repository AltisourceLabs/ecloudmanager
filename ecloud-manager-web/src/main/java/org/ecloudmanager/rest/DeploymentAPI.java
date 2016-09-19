package org.ecloudmanager.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/deployment")
public class DeploymentAPI {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        return "hi";
    }

}
