package org.ecloudmanager.agent.controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.ecloudmanager.agent.model.VM;
import org.ecloudmanager.agent.model.VMInfo;

import javax.ws.rs.core.Response.Status;

public class VmController {
    public ResponseContext createVM(RequestContext request, VM vm) {
        return new ResponseContext().status(Status.ACCEPTED).entity("777");
    }

    ResponseContext getVM(io.swagger.inflector.models.RequestContext request, String vmId) {
        VMInfo info = new VMInfo().status("running");
        return new ResponseContext().status(Status.OK).entity(info);
    }

}
