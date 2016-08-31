package org.ecloudmanager.node;

import org.ecloudmanager.node.model.Command;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.SSHCredentials;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

public class LocalAsyncSshAPI implements AsyncSshAPI {


    private ExecutorService executor;
    private LocalSshAPI localSsh;

    public LocalAsyncSshAPI(NodeBaseAPI nodeBaseAPI, ExecutorService executor) {
        localSsh = new LocalSshAPI(nodeBaseAPI);
        this.executor = executor;
    }


    @Override
    public LocalLoggableFuture<Integer> executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command) {
        return LoggableFuture.submit(() -> localSsh.executeScript(credentials, sshCredentials, nodeId, command), executor);
    }

    @Override
    public LocalLoggableFuture<Void> uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream file, String path) {
        return LoggableFuture.submit(() -> {
            localSsh.uploadFile(credentials, sshCredentials, nodeId, file, path);
            return null;
        }, executor);
    }


}
