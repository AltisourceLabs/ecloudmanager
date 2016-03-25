/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.service.provisioning;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.execution.SynchronousPoller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.ConnectException;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Singleton
public class SshSessionFactory {
    @Inject
    SynchronousPoller synchronousPoller;

    public Session createSession(String username, String host, String privateKey, String passphrase) throws
            JSchException {
        Callable<Session> tryConnect = () -> {
            try {
                JSch jsch = new JSch();
                jsch.addIdentity(username, privateKey.getBytes(), null, passphrase.getBytes());
                Session session = jsch.getSession(username, host);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                return session;
            } catch (JSchException e) {
                if (e.getCause() instanceof ConnectException) {
                    return null;
                } else {
                    throw new ActionException(e);
                }
            }
        };
        Predicate<Session> sessionConnected = session -> session != null && session.isConnected();
        return synchronousPoller.poll(tryConnect, sessionConnected, 1, 600, 30, "wait for SSH to become available");
    }

    public Session createForwardingSession(Session parentSession, String username, String host, String privateKey,
                                           String passphrase) throws JSchException {
        Callable<Session> tryConnect = () -> {
            try {
                if (!parentSession.isConnected()) {
                    throw new IllegalArgumentException("Parent session must be connected.");
                }
                int assinged_port = parentSession.setPortForwardingL(0, host, 22);
                JSch jsch = new JSch();
                jsch.addIdentity(username, privateKey.getBytes(), null, passphrase.getBytes());
                Session session = jsch.getSession(username, "127.0.0.1", assinged_port);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                return session;
            } catch (JSchException e) {
                if (e.getCause() instanceof ConnectException) {
                    return null;
                } else {
                    throw new ActionException(e);
                }
            }
        };
        Predicate<Session> sessionConnected = session -> session != null && session.isConnected();
        return synchronousPoller.poll(tryConnect, sessionConnected, 1, 600, 30, "wait for SSH to become available");
    }
}
