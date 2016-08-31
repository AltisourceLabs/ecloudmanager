package org.ecloudmanager.node;

import org.ecloudmanager.node.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalAsyncNodeAPI extends LocalAsyncSshAPI implements AsyncNodeAPI {
    private NodeBaseAPI nodeBaseAPI;
    private ExecutorService executor;
    public LocalAsyncNodeAPI(NodeBaseAPI nodeBaseAPI, ExecutorService executorService) {
        super(nodeBaseAPI, executorService);
        this.nodeBaseAPI = nodeBaseAPI;
        this.executor = executorService;
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
    public LocalLoggableFuture<NodeInfo> createNode(Credentials credentials, Map<String, String> parameters) {
        return LoggableFuture.submit(() -> nodeBaseAPI.createNode(credentials, parameters), executor);
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        return nodeBaseAPI.getNode(credentials, nodeId);
    }

    @Override
    public LocalLoggableFuture<NodeInfo> configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) {
        return LoggableFuture.submit(() -> nodeBaseAPI.configureNode(credentials, nodeId, parameters), executor);
    }

    @Override
    public LocalLoggableFuture<Void> deleteNode(Credentials credentials, String nodeId) {
        return LoggableFuture.submit(Executors.callable(() -> {
                    try {
                        nodeBaseAPI.deleteNode(credentials, nodeId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                , null), executor);

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
