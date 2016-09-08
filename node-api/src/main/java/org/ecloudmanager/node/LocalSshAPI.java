package org.ecloudmanager.node;

import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.node.model.Command;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.NodeInfo;
import org.ecloudmanager.node.model.SSHCredentials;
import org.ecloudmanager.node.util.SynchronousPoller;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class LocalSshAPI {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(LocalSshAPI.class);
    private NodeBaseAPI nodeBaseAPI;
    private SynchronousPoller synchronousPoller = new SynchronousPoller();
    public LocalSshAPI(NodeBaseAPI nodeBaseAPI) {
        this.nodeBaseAPI = nodeBaseAPI;

    }

    public void uploadFile(Credentials credentials, SSHCredentials sshCredentials, String nodeId, InputStream file, String path) throws Exception {
        NodeInfo info = nodeBaseAPI.getNode(credentials, nodeId);
        // TODO check status
        String ip = info.getIp();
        Stack<Session> sessionsChain = createSshSessionChain(ip, sshCredentials);
        Session session = sessionsChain.peek();
        ChannelSftp transferChannel = (ChannelSftp) session.openChannel("sftp");
        transferChannel.connect();
        transferChannel.put(file, path);
        transferChannel.disconnect();
        log.info("Uploaded file to node " + nodeId + ":" + path);
    }

    public int executeScript(Credentials credentials, SSHCredentials sshCredentials, String nodeId, Command command) throws Exception {
        NodeInfo info = nodeBaseAPI.getNode(credentials, nodeId);
        String ip = info.getIp();
        Stack<Session> sessionsChain = createSshSessionChain(ip, command.getCredentials());
        Session session = sessionsChain.peek();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPty(true);
        command.getCommand().add("exit;");
        String cmd = String.join(";", command.getCommand());

        log.info("Issuing the following command to " + nodeId + ": " + cmd);

        InputStream input = channel.getInputStream();
        OutputStream ops = channel.getOutputStream();
        PrintStream ps = new PrintStream(ops, true);

        channel.connect();

        ps.println(cmd);

        ps.close();
        byte[] tmp = new byte[1024];
        while (true) {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, 1024);
                if (i < 0) break;
                String s = new String(tmp, 0, i);
                log.info(s);
            }
            if (channel.isClosed()) {
                if (input.available() > 0) continue;
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Exception command output consuming loop", e);
            }
        }

        int exitStatus = channel.getExitStatus();
        log.info("Command finished with exit status: " + exitStatus);

        channel.disconnect();
        while (!sessionsChain.isEmpty()) {
            sessionsChain.pop().disconnect();
        }
        return exitStatus;
    }

    private Session createSession(String username, String host, String privateKey, String passphrase) throws
            JSchException {
        Callable<Session> tryConnect = () -> {
            try {
                JSch jsch = new JSch();
                jsch.addIdentity(username, privateKey.getBytes(), null, passphrase == null ? null : passphrase.getBytes());
                Session session = jsch.getSession(username, host);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                return session;
            } catch (JSchException e) {
                if (e.getCause() instanceof ConnectException) {
                    return null;
                } else {
                    throw e;
                }
            }
        };
        Predicate<Session> sessionConnected = session -> session != null && session.isConnected();
        return synchronousPoller.poll(tryConnect, sessionConnected, 1, 600, 30, "wait for SSH to become available");
    }

    private Session createForwardingSession(Session parentSession, String username, String host, String privateKey,
                                            String passphrase) throws JSchException {
        Callable<Session> tryConnect = () -> {
            try {
                if (!parentSession.isConnected()) {
                    throw new IllegalArgumentException("Parent session must be connected.");
                }
                int assinged_port = parentSession.setPortForwardingL(0, host, 22);
                JSch jsch = new JSch();
                jsch.addIdentity(username, privateKey.getBytes(), null, passphrase == null ? null : passphrase.getBytes());
                Session session = jsch.getSession(username, "127.0.0.1", assinged_port);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                return session;
            } catch (JSchException e) {
                if (e.getCause() instanceof ConnectException) {
                    return null;
                } else {
                    throw e;
                }
            }
        };
        Predicate<Session> sessionConnected = session -> session != null && session.isConnected();
        return synchronousPoller.poll(tryConnect, sessionConnected, 1, 600, 30, "wait for SSH to become available");
    }


    private Stack<Session> createSshSessionChain(String ip, SSHCredentials cfg) throws JSchException {
        Stack<Session> sessionsChain = new Stack<>();

        Session jumpSession1 = null;
        if (!StringUtils.isEmpty(cfg.getJumpHost1())) {
            jumpSession1 = createSession(cfg.getJumpHost1Username(), cfg.getJumpHost1(), cfg.getJumpHost1PrivateKey(), cfg.getJumpHost1PrivateKeyPassphrase());
            sessionsChain.push(jumpSession1);
        }

        Session jumpSession2 = null;
        if (jumpSession1 != null && !StringUtils.isEmpty(cfg.getJumpHost2())) {
            jumpSession2 = createForwardingSession(jumpSession1, cfg.getJumpHost2Username(), cfg.getJumpHost2(), cfg.getJumpHost2PrivateKey(), cfg.getJumpHost2PrivateKeyPassphrase());
            sessionsChain.push(jumpSession2);
        }

        Session nodeSession;
        if (jumpSession2 != null) {
            nodeSession = createForwardingSession(jumpSession2, cfg.getUsername(),
                    ip, cfg.getPrivateKey(), cfg.getPrivateKeyPassphrase());
        } else if (jumpSession1 != null) {
            nodeSession = createForwardingSession(jumpSession1, cfg.getUsername(),
                    ip, cfg.getPrivateKey(), cfg.getPrivateKeyPassphrase());
        } else {
            nodeSession = createSession(cfg.getUsername(), ip, cfg.getPrivateKey(), cfg.getPrivateKeyPassphrase());
        }

        sessionsChain.push(nodeSession);
        return sessionsChain;
    }


}
