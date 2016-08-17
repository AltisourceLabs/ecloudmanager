package org.ecloudmanager.service;

import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.node.LocalNodeAPI;
import org.ecloudmanager.node.NodeAPI;
import org.ecloudmanager.node.aws.AWSNodeAPI;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.node.rest.RestNodeAPI;
import org.ecloudmanager.node.verizon.VerizonNodeAPI;
import org.ecloudmanager.service.aws.AWSMongoCredentialsProvider;
import org.ecloudmanager.service.template.VerizonConfigurationService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NodeAPIProvider {
    private static Map<String, NodeAPI> apis = new HashMap<>();
    private static Map<String, SecretKey> credentials = new HashMap<>();

    static {
        apis.put("AWS", new LocalNodeAPI(new AWSNodeAPI()));
        apis.put("VERIZON", new LocalNodeAPI(new VerizonNodeAPI()));
        apis.put("AWS-REMOTE", new RestNodeAPI("http://localhost:8080"));
    }

    @Inject
    private AWSMongoCredentialsProvider awsMongoCredentialsProvider;
    @Inject
    private VerizonConfigurationService verizonConfigurationService;

    public List<String> getAPIs() {
        return new ArrayList<>(apis.keySet());
    }

    @PostConstruct
    public void postInit() {
        credentials.put("AWS", new SecretKey(awsMongoCredentialsProvider.getCredentials().getAWSAccessKeyId(), awsMongoCredentialsProvider.getCredentials().getAWSSecretKey()));
        credentials.put("AWS-REMOTE", new SecretKey(awsMongoCredentialsProvider.getCredentials().getAWSAccessKeyId(), awsMongoCredentialsProvider.getCredentials().getAWSSecretKey()));
        credentials.put("VERIZON", new SecretKey(verizonConfigurationService.getCurrentConfiguration().getAccessKey(), verizonConfigurationService.getCurrentConfiguration().getPrivateKey()));
    }

    public NodeAPI getAPI(String id) {
        return apis.get(id);
    }

    public SecretKey getCredentials(String id) {
        return credentials.get(id);
    }

}
