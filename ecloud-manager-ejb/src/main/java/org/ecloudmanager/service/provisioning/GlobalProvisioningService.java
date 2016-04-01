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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.ecloudmanager.domain.template.SshConfiguration;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.repository.SshConfigurationRepository;
import org.ecloudmanager.service.chef.ChefGenerationService;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.execution.SynchronousPoller;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.*;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.Callable;

@Service
public class GlobalProvisioningService {

    @Inject
    private SshSessionFactory sshSessionFactory;

    @Inject
    private SshConfigurationRepository sshConfigurationRepository;

    @Inject
    private Event<SshLog> logEvent;

    @Inject
    private ChefGenerationService chefGenerationService;

    @Inject
    private Logger log;

    public void provisionVm(VMDeployment deployment, boolean firstRun) {
        try {
            provisionVmImpl(deployment, firstRun);
        } catch (Exception e) {
            log.log(Level.ERROR, "Failed to provision VM " + deployment.getConfigValue(VMDeployer.VM_NAME), e);
            throw new RuntimeException(e);
        }
    }

    private void provisionVmImpl(VMDeployment deployment, boolean firstRun) throws Exception {
        if (deployment == null) {
            log.error("Provisioning without deployment object is not supported, exiting...");
            return;
        }

        String nodeName = deployment.getConfigValue(VMDeployer.VM_NAME);
        log.info("Started provisioning of vm: " + nodeName);

        DeploymentObject applicationDeployment = deployment.getTop();
        DeploymentObject config = ChefEnvironmentDeployer.getServerConfig(deployment.getChefEnvironment());

        String ipAddress = InfrastructureDeployer.getIP(deployment);
        String sshConfigurationName = InfrastructureDeployer.getSshConfiguration(deployment);
        Stack<Session> sessionsChain = createSshSessionChain(sshConfigurationName, ipAddress);

        Session session = sessionsChain.peek();
        // Chef config files transfer
        ChannelSftp transferChannel = (ChannelSftp) session.openChannel("sftp");
        transferChannel.connect();

        if (firstRun) {
            InputStream clientRbTemplateStream = this.getClass().getResourceAsStream("/client.rb.mustache");
            InputStream clientRbStream = generateClientRb(clientRbTemplateStream, deployment.getChefEnvironment(),
                nodeName);
            transferChannel.put(clientRbStream, "client.rb");

            transferChannel.put(new ByteArrayInputStream(config.getConfigValue(ChefEnvironmentDeployer
                .CHEF_VALIDATION_CLIENT_SECRET).getBytes()), "chef-validator.pem");
        }

        InputStream nodeConfigJsonStream = generateNodeJson(deployment, "\"recipe[hostnames]\"");
        transferChannel.put(nodeConfigJsonStream, "node-config.json");

        InputStream nodeJsonStream = generateNodeJson(deployment);
        transferChannel.put(nodeJsonStream, "node.json");

        transferChannel.disconnect();

        // Chef prerequisites task
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPty(true);

        StringBuilder command = new StringBuilder();

        //commands
        if (firstRun) {
            command.append("sudo yum install -y wget;");
            command.append("wget https://opscode-omnibus-packages.s3.amazonaws.com/el/6/x86_64/chef-12.0.3-1.x86_64.rpm;");
            command.append("sudo rpm -ivh chef-12.0.3-1.x86_64.rpm;");
            command.append("sudo mkdir /etc/chef;");
            command.append("sudo chmod 777 /etc/chef;");
            command.append("mv -f client.rb /etc/chef;");
            command.append("mv -f chef-validator.pem /etc/chef;");
        }
        command.append("mv -f node-config.json /etc/chef;");
        command.append("mv -f node.json /etc/chef;");

        command.append("sudo chef-client -j /etc/chef/node-config.json;");
        command.append("sudo chef-client -j /etc/chef/node.json;");

        command.append("exit;");
        log.info("Issuing the following command to " + nodeName + ": " + command);

        InputStream input = channel.getInputStream();
        OutputStream ops = channel.getOutputStream();
        PrintStream ps = new PrintStream(ops, true);

        channel.connect();

        ps.println(command.toString());
        ps.close();

        byte[] tmp = new byte[1024];
        while (true) {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, 1024);
                if (i < 0) break;
                String s = new String(tmp, 0, i);
                log.info(s);
                // TODO - output to ui
                //logEvent.fire(new SshLog(s));
            }
            if (channel.isClosed()) {
                if (input.available() > 0) continue;
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.log(Level.ERROR, "Exception in provisioning output consuming loop", e);
            }
        }

        int exitStatus = channel.getExitStatus();
        log.info("Provisioning process finished with exit status: " + exitStatus);

        channel.disconnect();
        while (!sessionsChain.isEmpty()) {
            sessionsChain.pop().disconnect();
        }
        if (exitStatus != 0) {
            throw new RuntimeException("Error provisioning VM " + nodeName + ", exit status: " + exitStatus);
        }

        log.info("Finished provisioning of vm: " + nodeName);
    }

    private Stack<Session> createSshSessionChain(String sshConfigurationName, String ipAddress) throws JSchException {
        Stack<Session> sessionsChain = new Stack<>();

        SshConfiguration sshConfiguration = sshConfigurationRepository.find(sshConfigurationName);
        if (sshConfiguration == null) {
            throw new RuntimeException("SSH configuration not found: " + sshConfigurationName);
        }

        Session jumpSession1 = null;
        if (!StringUtils.isEmpty(sshConfiguration.getJumpHost1())) {
            jumpSession1 = sshSessionFactory.createSession(sshConfiguration.getJumpHost1Username(), sshConfiguration
                    .getJumpHost1(), sshConfiguration.getJumpHost1PrivateKey(), sshConfiguration
                    .getJumpHost1PrivateKeyPassphrase());
            sessionsChain.push(jumpSession1);
        }

        Session jumpSession2 = null;
        if (jumpSession1 != null && !StringUtils.isEmpty(sshConfiguration.getJumpHost2())) {
            jumpSession2 = sshSessionFactory.createForwardingSession(jumpSession1, sshConfiguration
                            .getJumpHost2Username(), sshConfiguration.getJumpHost2(), sshConfiguration.getJumpHost2PrivateKey(),
                    sshConfiguration
                            .getJumpHost2PrivateKeyPassphrase());
            sessionsChain.push(jumpSession2);
        }

        Session vmSession;
        if (jumpSession2 != null) {
            vmSession = sshSessionFactory.createForwardingSession(jumpSession2, sshConfiguration.getUsername(),
                    ipAddress, sshConfiguration.getPrivateKey(), sshConfiguration.getPrivateKeyPassphrase());
        } else if (jumpSession1 != null) {
            vmSession = sshSessionFactory.createForwardingSession(jumpSession1, sshConfiguration.getUsername(),
                    ipAddress, sshConfiguration.getPrivateKey(), sshConfiguration.getPrivateKeyPassphrase());
        } else {
            vmSession = sshSessionFactory.createSession(sshConfiguration.getUsername(), ipAddress, sshConfiguration
                    .getPrivateKey(), sshConfiguration.getPrivateKeyPassphrase());
        }

        sessionsChain.push(vmSession);
        return sessionsChain;
    }

    private InputStream generateClientRb(InputStream clientRbTemplate, ChefEnvironment env, String
            nodeName) {
        MustacheFactory mf = new UnescapedMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(clientRbTemplate), "client.rb");

        DeploymentObject config = ChefEnvironmentDeployer.getServerConfig(env);
        HashMap<String, String> scopes = new HashMap<>();
        scopes.put("node_name", nodeName);
        scopes.put("chef_server_url", config.getConfigValue(ChefEnvironmentDeployer.CHEF_SERVER_ADDRESS));
        scopes.put("environment", env.getTop().getName());
        scopes.put("validation_client_name", config.getConfigValue(ChefEnvironmentDeployer
                .CHEF_VALIDATION_CLIENT_NAME));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, scopes);
        writer.flush();
        return new ByteArrayInputStream(writer.toString().getBytes());
    }

    private InputStream generateNodeJson(VMDeployment deployment) {
        Writer sw = chefGenerationService.generateChefNodeEnv(new StringWriter(), deployment);
        return getInputStreamFromWriter(sw);
    }

    private InputStream generateNodeJson(VMDeployment deployment, String runlist) {
        Writer sw = chefGenerationService.generateChefNodeEnv(new StringWriter(), deployment, runlist);
        return getInputStreamFromWriter(sw);
    }

    private InputStream getInputStreamFromWriter(Writer writer) {
        String jsonString;
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Object o = mapper.readValue(writer.toString(), Object.class);
            jsonString = mapper.writeValueAsString(o);
        } catch (IOException e) {
            jsonString = e.toString();
        }

        return new ByteArrayInputStream(jsonString.getBytes());
    }

    public boolean needUpdateProvisioning(VMDeployment before, VMDeployment after) {
        String oldNodeJson = chefGenerationService.generateChefNodeEnv(new StringWriter(), before).toString();
        String newNodeJson = chefGenerationService.generateChefNodeEnv(new StringWriter(), after).toString();
        return !oldNodeJson.equals(newNodeJson);
    }
}
