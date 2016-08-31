package org.ecloudmanager.node;

import org.ecloudmanager.node.model.Command;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.SSHCredentials;

import java.io.InputStream;

public interface AsyncSshAPI {
    LoggableFuture<Integer> executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command);

    LoggableFuture<Void> uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream file, String path);
}
