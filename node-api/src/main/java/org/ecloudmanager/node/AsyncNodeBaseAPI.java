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

    ExecutionDetails configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception;

    ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception;

    FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception;

    ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception;
}
