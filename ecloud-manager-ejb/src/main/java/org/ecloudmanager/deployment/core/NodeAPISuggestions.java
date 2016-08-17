package org.ecloudmanager.deployment.core;

import org.ecloudmanager.node.model.NodeParameter;
import org.ecloudmanager.service.NodeAPIProvider;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeAPISuggestions implements ConstraintFieldSuggestionsProvider {
    private NodeParameter parameter;
    private String apiId;

    public NodeAPISuggestions() {
    }

    public NodeAPISuggestions(String apiId, NodeParameter parameter) {
        this.apiId = apiId;
        this.parameter = parameter;
    }

    @Override
    public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
        Map<String, String> params = ((DeploymentObject) deploymentConstraint).getConfigValues();
        try {
            NodeAPIProvider nodeAPIProvider = CDI.current().select(NodeAPIProvider.class).get();
            return nodeAPIProvider.getAPI(apiId).getNodeParameterValues(nodeAPIProvider.getCredentials(apiId), parameter.getName(), params).stream()
                    .map(p -> new ConstraintFieldSuggestion(p.getDescription(), p.getValue())).collect(Collectors.toList());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
