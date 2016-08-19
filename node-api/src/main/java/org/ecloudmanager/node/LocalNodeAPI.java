package org.ecloudmanager.node;

import org.ecloudmanager.node.model.*;

import java.util.List;
import java.util.Map;

public class LocalNodeAPI extends LocalSshAPI implements NodeAPI {
    private NodeBaseAPI nodeBaseAPI;

    public LocalNodeAPI(NodeBaseAPI nodeBaseAPI) {
        super(nodeBaseAPI);
        this.nodeBaseAPI = nodeBaseAPI;
    }

    @Override
    public APIInfo getAPIInfo() throws Exception {
        return nodeBaseAPI.getAPIInfo();
    }

    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception {
        return nodeBaseAPI.getNodeParameters(credentials);
    }

    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception {
        return nodeBaseAPI.getNodeParameterValues(credentials, parameter, parameters);
    }

    @Override
    public CreateNodeResponse createNode(Credentials credentials, Map<String, String> parameters) throws Exception {
        return nodeBaseAPI.createNode(credentials, parameters);
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        return nodeBaseAPI.getNode(credentials, nodeId);
    }

    @Override
    public ExecutionDetails configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        return nodeBaseAPI.configureNode(credentials, nodeId, parameters);
    }

    @Override
    public ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception {
        return nodeBaseAPI.deleteNode(credentials, nodeId);
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception {
        return nodeBaseAPI.getNodeFirewallRules(credentials, nodeId);
    }

    @Override
    public ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        return nodeBaseAPI.updateNodeFirewallRules(credentials, nodeId, firewallUpdate);
    }
}
