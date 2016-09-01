package org.ecloudmanager.node;

import org.ecloudmanager.node.model.*;

import java.util.List;
import java.util.Map;

public interface AsyncNodeBaseAPI {
    APIInfo getAPIInfo() throws Exception;

    List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception;

    List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception;

    LoggableFuture<NodeInfo> createNode(Credentials credentials, Map<String, String> parameters) throws Exception;

    NodeInfo getNode(Credentials credentials, String nodeId) throws Exception;

    LoggableFuture<NodeInfo> configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception;

    LoggableFuture<Void> deleteNode(Credentials credentials, String nodeId) throws Exception;

    FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception;

    LoggableFuture<FirewallInfo> updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception;
}
