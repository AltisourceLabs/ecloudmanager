package org.ecloudmanager.node;

import org.ecloudmanager.node.model.Command;
import org.ecloudmanager.node.model.CommandOutput;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.SSHCredentials;

import java.io.InputStream;

public interface SshAPI {
    void uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream file, String path) throws Exception;

    CommandOutput executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command) throws Exception;
}
