package org.ecloudmanager.agent.controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.ecloudmanager.node.LocalNodeAPI;
import org.ecloudmanager.node.aws.AWSNodeAPI;
import org.ecloudmanager.node.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NodeController {
    static Logger log = LoggerFactory.getLogger(NodeController.class);
    // FIXME should be configurable
    private LocalNodeAPI api = new LocalNodeAPI(new AWSNodeAPI());

    public ResponseContext getNodeParameters(RequestContext request, String accessKey, String secretKey) {
        try {
            List<NodeParameter> response = api.getNodeParameters(new SecretKey(accessKey, secretKey));
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNodeParameterValues(RequestContext request, String accessKey, String secretKey, String parameter, List<String> names, List<String> values) {
        try {
            Map<String, String> parameters = new HashMap<>();
            if (names != null && values != null) {
                for (int i = 0; i < Math.min(names.size(), values.size()); i++) {
                    parameters.put(names.get(i), values.get(i));
                }
            }
            List<ParameterValue> response = api.getNodeParameterValues(new SecretKey(accessKey, secretKey), parameter, parameters);
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext createNode(RequestContext request, String accessKey, String secretKey, Node node) {
        try {
            CreateNodeResponse response = api.createNode(new SecretKey(accessKey, secretKey), node.getParameters());
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext configureNode(RequestContext request, String accessKey, String secretKey, String nodeId, Node node) {
        try {
            ExecutionDetails response = api.configureNode(new SecretKey(accessKey, secretKey), nodeId, node.getParameters());
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext deleteNode(RequestContext request, String accessKey, String secretKey, String nodeId) {
        try {
            api.deleteNode(new SecretKey(accessKey, secretKey), nodeId);
            return new ResponseContext().status(Status.OK);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNode(RequestContext request, String accessKey, String secretKey, String nodeId) {
        try {
            NodeInfo response = api.getNode(new SecretKey(accessKey, secretKey), nodeId);
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNodeFirewallRules(RequestContext request, String accessKey, String secretKey, String nodeId) {
        try {
            FirewallInfo rules = api.getNodeFirewallRules(new SecretKey(accessKey, secretKey), nodeId);
            return new ResponseContext().status(Status.OK).entity(rules);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext updateNodeFirewallRules(RequestContext request, String accessKey, String secretKey, String nodeId, FirewallUpdate firewallUpdate) {
        try {
            api.updateNodeFirewallRules(new SecretKey(accessKey, secretKey), nodeId, firewallUpdate);
            return new ResponseContext().status(Status.OK);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }
}
