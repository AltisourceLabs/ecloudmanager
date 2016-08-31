package org.ecloudmanager.node;

import org.ecloudmanager.node.model.*;

import java.util.List;
import java.util.Map;

public interface NodeBaseAPI {
    /**
     * @return API Info
     */
    APIInfo getAPIInfo() throws Exception;

    List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception;

    /**
     * @return Possible values for parameter
     */
    List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception;

    /**
     * @return Id of node created
     */
    NodeInfo createNode(Credentials credentials, Map<String, String> parameters) throws Exception;

    /**
     * @return Information for node with id 'nodeId'
     */
    NodeInfo getNode(Credentials credentials, String nodeId) throws Exception;

    /**
     * @return Updates node with parameter values specified in 'node'
     */
    ExecutionDetails configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception;

    /**
     * Delete node
     */
    ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception;

    /**
     * @return FirewallInfo for node with 'nodeId'
     */
    FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception;

    /**
     * Updates Firewall rules for specified node
     */
    ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception;
}
