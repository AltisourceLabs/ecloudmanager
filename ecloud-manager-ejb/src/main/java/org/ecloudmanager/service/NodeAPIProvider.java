package org.ecloudmanager.service;

import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.node.LocalNodeAPI;
import org.ecloudmanager.node.NodeAPI;
import org.ecloudmanager.node.aws.AWSNodeAPI;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.node.rest.RestNodeAPI;
import org.ecloudmanager.node.verizon.VerizonNodeAPI;
import org.ecloudmanager.service.aws.AWSMongoCredentialsProvider;
import org.ecloudmanager.service.template.AWSConfigurationService;
import org.ecloudmanager.service.template.VerizonConfigurationService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Service
public class NodeAPIProvider {
    private static Map<String, NodeAPI> apis = new HashMap<>();
    private static Map<String, SecretKey> credentials = new HashMap<>();
    private static Map<String, List<Recipe>> runlists = new HashMap<>();
    static {
        apis.put("AWS", new LocalNodeAPI(new AWSNodeAPI()));
        apis.put("VERIZON", new LocalNodeAPI(new VerizonNodeAPI()));
        apis.put("AWS-REMOTE", new RestNodeAPI("http://localhost:8080"));
    }

    @Inject
    private AWSMongoCredentialsProvider awsMongoCredentialsProvider;
    @Inject
    private AWSConfigurationService awsConfigurationService;
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
        runlists.put("AWS", awsConfigurationService.getCurrentConfiguration().getRunlist());
        runlists.put("AWS-REMOTE", awsConfigurationService.getCurrentConfiguration().getRunlist());
        runlists.put("VERIZON", verizonConfigurationService.getCurrentConfiguration().getRunlist());
    }

    public NodeAPI getAPI(String id) {
        return apis.get(id);
    }

    public SecretKey getCredentials(String id) {
        return credentials.get(id);
    }

    public List<Recipe> getRunlist(String id) {
        List<Recipe> runlist = runlists.get(id);
        return runlist != null ? runlist : Collections.emptyList();
    }

}
