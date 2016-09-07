package org.ecloudmanager.node.rest;

import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.rest.client.NodeApi;
import org.ecloudmanager.node.rest.client.SshApi;
import org.ecloudmanager.node.rest.client.TasksApi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestNodeAPI implements org.ecloudmanager.node.AsyncNodeAPI {
    private NodeApi nodeApi;
    private SshApi sshApi;
    private TasksApi tasksApi;

    public RestNodeAPI(String basePath) {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);
        nodeApi = new NodeApi(client);
        sshApi = new SshApi(client);
        tasksApi = new TasksApi(client);
    }


    @Override
    public APIInfo getAPIInfo() throws ApiException {
        return nodeApi.getInfo();
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
        List<String> values = names.stream().map(parameters::get).collect(Collectors.toList());
        return nodeApi.getNodeParameterValues(sk.getName(), sk.getSecret(), parameter, names, values);
    }

    @Override
    public LoggableFuture<NodeInfo> createNode(Credentials credentials, Map<String, String> parameters) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        try {
            String taskId = nodeApi.createNode(sk.getName(), sk.getSecret(), new Node().parameters(parameters));
            return new RestLoggableFuture<>(sk, taskId, tasksApi, NodeInfo.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'createNode'", e);
        }
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.getNode(sk.getName(), sk.getSecret(), nodeId);
    }

    @Override
    public LoggableFuture<NodeInfo> configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        try {
            String taskId = nodeApi.configureNode(sk.getName(), sk.getSecret(), nodeId, new Node().parameters(parameters));
            return new RestLoggableFuture<>(sk, taskId, tasksApi, NodeInfo.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'configureNode'", e);
        }
    }

    @Override
    public LoggableFuture<Void> deleteNode(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        try {
            String taskId = nodeApi.deleteNode(sk.getName(), sk.getSecret(), nodeId);
            return new RestLoggableFuture<>(sk, taskId, tasksApi, Void.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'deleteNode'", e);
        }
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        return nodeApi.getNodeFirewallRules(sk.getName(), sk.getSecret(), nodeId);
    }

    @Override
    public LoggableFuture<FirewallInfo> updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        SecretKey sk = (SecretKey) credentials;
        try {
            String taskId = nodeApi.updateNodeFirewallRules(sk.getName(), sk.getSecret(), nodeId, firewallUpdate);
            return new RestLoggableFuture<>(sk, taskId, tasksApi, FirewallInfo.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'deleteNode'", e);
        }
    }

    @Override
    public LoggableFuture<Void> uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream is, String path) {
        SecretKey sk = (SecretKey) credentials;
        Path filePath;
        try {
            filePath = Files.createTempFile("ssh-upload", ".tmp");
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return LoggableFuture.failedFuture("Failed to write InputStream to temporary file", e);
        }
        try {
            String taskId = sshApi.uploadFile(sk.getName(), sk.getSecret(), sshCredentials.getUsername(), sshCredentials.getPrivateKey(), path, nodeId, sshCredentials.getPrivateKeyPassphrase()
                    , sshCredentials.getJumpHost1(), sshCredentials.getJumpHost1Username(), sshCredentials.getJumpHost1PrivateKey(), sshCredentials.getJumpHost1PrivateKeyPassphrase()
                    , sshCredentials.getJumpHost2(), sshCredentials.getJumpHost2Username(), sshCredentials.getJumpHost2PrivateKey(), sshCredentials.getJumpHost2PrivateKeyPassphrase(),
                    filePath.toFile());
            return new RestLoggableFuture<>(sk, taskId, tasksApi, Void.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'uploadFile'", e);
        }
    }

    @Override
    public LoggableFuture<Integer> executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command) {
        SecretKey sk = (SecretKey) credentials;
        try {
            String taskId = sshApi.executeScript(sk.getName(), sk.getSecret(), nodeId, command);
            return new RestLoggableFuture<>(sk, taskId, tasksApi, Integer.class);
        } catch (ApiException e) {
            return LoggableFuture.failedFuture("Failed to invoke 'executeScript'", e);
        }
    }
}
