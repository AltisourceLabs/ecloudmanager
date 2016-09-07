package org.ecloudmanager.agent.controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ecloudmanager.node.LocalAsyncNodeAPI;
import org.ecloudmanager.node.LocalLoggableFuture;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;


public class DefaultController {
    private final static Map<String, Pair<Credentials, LocalLoggableFuture<?>>> tasks = new ConcurrentHashMap<>();


    private static Logger log = LoggerFactory.getLogger(DefaultController.class);
    private LocalAsyncNodeAPI api = new LocalAsyncNodeAPI(getNodeBaseAPI(), Executors.newCachedThreadPool());

    private static NodeBaseAPI getNodeBaseAPI() {
        Map<String, APIInfo> apis = NodeUtil.getAvailableAPIs();
        if (apis.isEmpty()) {
            log.error("NodeBaseAPI implementation not found");
            throw new RuntimeException("NodeBaseAPI implementation not found");
        }
        log.info("NodeBaseAPI implementations found:");
        apis.entrySet().forEach(entry -> log.info(entry.getKey() + " id: " + entry.getValue().getId() + " description: " + entry.getValue().getDescription()));
        if (apis.size() > 1) {
            throw new RuntimeException("More than one NodeBaseAPI implementation found");
        }
        String className = apis.keySet().toArray(new String[1])[0];
        try {
            return (NodeBaseAPI) Class.forName(className).newInstance();

        } catch (Exception e) {
            log.error("Can't instantiate " + className, e);
            throw new RuntimeException(e);
        }
    }


    public ResponseContext getInfo(RequestContext request) {
        try {
            return new ResponseContext().status(Status.OK).entity(api.getAPIInfo());
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNodeParameters(RequestContext request, String accessKey, String secretKey) {
        try {
            List<NodeParameter> response = api.getNodeParameters(new SecretKey(accessKey, secretKey));
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNodeParameterValues(RequestContext request, String accessKey, String secretKey, String parameter, List<String> names, List<String> values) {
        try {
            Map<String, String> parameters = new HashMap<>();
            if (names != null && values != null) {
                for (int i = 0; i < Math.min(names.size(), values.size()); i++) {
                    parameters.put(names.get(i), values.get(i));
                }
            }
            List<ParameterValue> response = api.getNodeParameterValues(new SecretKey(accessKey, secretKey), parameter, parameters);
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext createNode(RequestContext request, String accessKey, String secretKey, Node node) {
        LocalLoggableFuture<NodeInfo> f = api.createNode(new SecretKey(accessKey, secretKey), node.getParameters());
        tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
        return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());
    }

    public ResponseContext configureNode(RequestContext request, String accessKey, String secretKey, String nodeId, Node node) {
        LocalLoggableFuture<NodeInfo> f = api.configureNode(new SecretKey(accessKey, secretKey), nodeId, node.getParameters());
        tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
        return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());
    }

    public ResponseContext deleteNode(RequestContext request, String accessKey, String secretKey, String nodeId) {
        LocalLoggableFuture<Void> f = api.deleteNode(new SecretKey(accessKey, secretKey), nodeId);
        tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
        return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());
    }

    public ResponseContext getNode(RequestContext request, String accessKey, String secretKey, String nodeId) {
        try {
            NodeInfo response = api.getNode(new SecretKey(accessKey, secretKey), nodeId);
            return new ResponseContext().status(Status.OK).entity(response);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext getNodeFirewallRules(RequestContext request, String accessKey, String secretKey, String nodeId) {
        try {
            FirewallInfo rules = api.getNodeFirewallRules(new SecretKey(accessKey, secretKey), nodeId);
            return new ResponseContext().status(Status.OK).entity(rules);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext updateNodeFirewallRules(RequestContext request, String accessKey, String secretKey, String nodeId, FirewallUpdate firewallUpdate) {
        try {
            LocalLoggableFuture<FirewallInfo> f = api.updateNodeFirewallRules(new SecretKey(accessKey, secretKey), nodeId, firewallUpdate);
            tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
            return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
    }

    public ResponseContext executeScript(RequestContext request, String accessKey, String secretKey, String nodeId, Command command) {
        LocalLoggableFuture<Integer> f = api.executeScript(new SecretKey(accessKey, secretKey), command.getCredentials(), nodeId, command);
        tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
        return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());

    }

    public ResponseContext uploadFile(RequestContext request, String accessKey, String secretKey, String username, String privateKey, String privateKeyPassphrase, String jumpHost1, String jumpHost1Username, String jumpHost1PrivateKey, String jumpHost1PrivateKeyPassphrase, String jumpHost2, String jumpHost2Username, String jumpHost2PrivateKey, String jumpHost2PrivateKeyPassphrase, String path, String nodeId, File file) {
        SSHCredentials sshCredentials = new SSHCredentials().username(username).privateKey(privateKey).privateKeyPassphrase(privateKeyPassphrase).jumpHost1(jumpHost1).jumpHost1Username(jumpHost1Username).jumpHost1PrivateKey(jumpHost1PrivateKey).jumpHost1PrivateKeyPassphrase(jumpHost1PrivateKeyPassphrase).jumpHost2(jumpHost2).jumpHost2Username(jumpHost2Username).jumpHost2PrivateKey(jumpHost2PrivateKey).jumpHost1PrivateKeyPassphrase(jumpHost2PrivateKeyPassphrase);
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return new ResponseContext()
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage());
        }
        LocalLoggableFuture<Void> f = api.uploadFile(new SecretKey(accessKey, secretKey), sshCredentials, nodeId, is, path);
        tasks.put(f.getId(), Pair.of(new SecretKey(accessKey, secretKey), f));
        return new ResponseContext().status(Status.ACCEPTED).entity(f.getId());

    }

    public ResponseContext getTask(RequestContext request, String accessKey, String secretKey, String taskId) {
        Pair<Credentials, LocalLoggableFuture<?>> cf = tasks.get(taskId);
        if (cf == null || !cf.getLeft().equals(new SecretKey(accessKey, secretKey))) {
            return new ResponseContext().status(Status.NOT_FOUND);
        }
        LocalLoggableFuture f = cf.getRight();
        TaskInfo response = new TaskInfo();
        if (!f.isDone()) {
            return new ResponseContext().status(Status.OK).entity(response.done(false));
        }
        try {
            return new ResponseContext().status(Status.OK).entity(response.done(true).value(f.get()));
        } catch (Exception e) {
            LoggingEvent loggingEvent = new LoggingEvent().level("ERROR").logger(DefaultController.class.getName())
                    .message("Task " + taskId + " failed").timeStamp(System.currentTimeMillis())
                    .throwable(ExceptionUtils.getStackTrace(e));
            return new ResponseContext().status(Status.OK).entity(response.done(true).exception(loggingEvent));
        }
    }

    public ResponseContext deleteTask(RequestContext request, String accessKey, String secretKey, String taskId) {
        Pair<Credentials, LocalLoggableFuture<?>> cf = tasks.get(taskId);
        if (cf == null || !cf.getLeft().equals(new SecretKey(accessKey, secretKey))) {
            return new ResponseContext().status(Status.NOT_FOUND);
        }
        tasks.remove(taskId);
        cf.getValue().deleteLogs();
        return new ResponseContext().status(Status.OK);
    }

    public ResponseContext pollLog(RequestContext request, String accessKey, String secretKey, String taskId) {
        Pair<Credentials, LocalLoggableFuture<?>> cf = tasks.get(taskId);
        if (cf == null || !cf.getLeft().equals(new SecretKey(accessKey, secretKey))) {
            return new ResponseContext().status(Status.NOT_FOUND);
        }

        List<LoggingEvent> result = cf.getRight().pollLogs();
        log.info(result.size() + " log events polled ");
        return new ResponseContext().status(Status.OK).entity(result);
    }

}
