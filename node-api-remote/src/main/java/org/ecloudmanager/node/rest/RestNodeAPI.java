package org.ecloudmanager.node.rest;

import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.rest.client.NodeApi;
import org.ecloudmanager.node.rest.client.SshApi;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestNodeAPI implements org.ecloudmanager.node.NodeAPI {
    private NodeApi nodeApi;
    private SshApi sshApi;

    public RestNodeAPI(String basePath) {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);
        nodeApi = new NodeApi(client);
        sshApi = new SshApi(client);
    }


    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.getNodeParameters(sk.getName(), sk.getSecret());
    }

    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        List<String> names = new ArrayList<>(parameters.keySet());
        List<String> values = names.stream().map(k -> parameters.get(k)).collect(Collectors.toList());
        return nodeApi.getNodeParameterValues(sk.getName(), sk.getSecret(), parameter, names, values);
    }

    @Override
    public CreateNodeResponse createNode(Credentials credentials, Map<String, String> parameters) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.createNode(sk.getName(), sk.getSecret(), new Node().parameters(parameters));
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.getNode(sk.getName(), sk.getSecret(), nodeId);
    }

    @Override
    public ExecutionDetails configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.configureNode(sk.getName(), sk.getSecret(), nodeId, new Node().parameters(parameters));
    }

    @Override
    public ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.deleteNode(sk.getName(), sk.getSecret(), nodeId);
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.getNodeFirewallRules(sk.getName(), sk.getSecret(), nodeId);
    }

    @Override
    public ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.updateNodeFirewallRules(sk.getName(), sk.getSecret(), nodeId, firewallUpdate);
    }

    @Override
    public void uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream is, String path) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        File f = File.createTempFile("ssh-upload", ".tmp");
        Files.copy(is, Paths.get(f.toURI()));
        //sshApi.uploadFile(sk.getName(), sk.getSecret(), sshCredentials.getUsername(), sshCredentials.getPrivateKey(), path, nodeId, f);

    }

    @Override
    public CommandOutput executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return sshApi.executeScript(sk.getName(), sk.getSecret(), nodeId, command);
    }
}
