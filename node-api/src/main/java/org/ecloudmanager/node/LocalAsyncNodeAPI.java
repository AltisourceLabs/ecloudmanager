package org.ecloudmanager.node;

import org.ecloudmanager.node.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.ecloudmanager.node.LoggableFuture.submit;
public class LocalAsyncNodeAPI extends LocalAsyncSshAPI implements AsyncNodeAPI {
    private NodeBaseAPI nodeBaseAPI;
    private ExecutorService executor;
    public LocalAsyncNodeAPI(NodeBaseAPI nodeBaseAPI, ExecutorService executorService) {
        super(nodeBaseAPI, executorService);
        this.nodeBaseAPI = nodeBaseAPI;
        this.executor = executorService;
    }

    @Override
    public APIInfo getAPIInfo() {
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
        return submit(() -> nodeBaseAPI.createNode(credentials, parameters), executor);
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        return nodeBaseAPI.getNode(credentials, nodeId);
    }

    @Override
    public LocalLoggableFuture<NodeInfo> configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) {
        return submit(() -> nodeBaseAPI.configureNode(credentials, nodeId, parameters), executor);
    }

    @Override
    public LocalLoggableFuture<Void> deleteNode(Credentials credentials, String nodeId) {
        return submit(Executors.callable(() -> {
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
    public LocalLoggableFuture<FirewallInfo> updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        return submit(() -> nodeBaseAPI.updateNodeFirewallRules(credentials, nodeId, firewallUpdate), executor);
    }
}
