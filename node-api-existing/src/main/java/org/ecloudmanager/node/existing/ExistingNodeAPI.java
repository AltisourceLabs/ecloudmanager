package org.ecloudmanager.node.existing;

import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExistingNodeAPI implements NodeBaseAPI {
    static Logger log = LoggerFactory.getLogger(ExistingNodeAPI.class);

    private static APIInfo API_INFO = new APIInfo().id("Existing Node").description("Provisioning to existing node").firewalled(false).configureOnCreate(false);

    @Override
    public APIInfo getAPIInfo() {
        return API_INFO;
    }


    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception {
        return Arrays.stream(Parameter.values()).map(Parameter::getNodeParameter).collect(Collectors.toList());
    }


    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) {
        return Collections.emptyList();
    }

    @Override
    public String createNode(Credentials credentials, Map<String, String> parameters) {
        String ip = parameters.get(Parameter.ip.name());
        log.info("Node ip address: " + ip);
        return ip;
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) {
        return new NodeInfo().id(nodeId).ip(nodeId);
    }

    @Override
    public NodeInfo configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        log.info("Using existing node, nothing to configure");
        return getNode(credentials, nodeId);
    }

    @Override
    public void deleteNode(Credentials credentials, String nodeId) {
        log.info("Using existing node, nothing to delete");
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) {
        return new FirewallInfo();
    }

    @Override
    public FirewallInfo updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) {
        log.info("Using existing node, nothing to configure");
        return new FirewallInfo();
    }


    enum Parameter {
        ip("Node IP address", true, false, true, null, false, false),;

        private NodeParameter nodeParameter;

        Parameter(String description, boolean create, boolean configure, boolean required, String defaultValue, boolean canSuggest, boolean strictSuggest, Parameter... args) {
            List<String> argsList = Arrays.stream(args).map(Enum::name).collect(Collectors.toList());
            nodeParameter = new NodeParameter().name(name()).description(description).create(create).configure(configure)
                    .required(required).defaultValue(defaultValue)
                    .canSuggest(canSuggest).strictSuggest(strictSuggest).args(argsList);
        }

        public NodeParameter getNodeParameter() {
            return nodeParameter;
        }

    }


}
