package org.ecloudmanager.deployment.core;

import org.ecloudmanager.node.model.NodeParameter;
import org.ecloudmanager.service.NodeAPIConfigurationService;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeAPISuggestions implements ConstraintFieldSuggestionsProvider {
    private NodeParameter parameter;
    private String apiName;

    public NodeAPISuggestions() {
    }

    public NodeAPISuggestions(String apiName, NodeParameter parameter) {
        this.apiName = apiName;
        this.parameter = parameter;
    }

    @Override
    public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
        Map<String, String> params = ((DeploymentObject) deploymentConstraint).getConfigValues();
        try {
            NodeAPIConfigurationService nodeAPIProvider = CDI.current().select(NodeAPIConfigurationService.class).get();
            return nodeAPIProvider.getAPI(apiName).getNodeParameterValues(nodeAPIProvider.getCredentials(apiName), parameter.getName(), params).stream()
                    .map(p -> new ConstraintFieldSuggestion(p.getDescription(), p.getValue())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
