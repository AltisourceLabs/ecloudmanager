package org.ecloudmanager.deployment.core;

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.node.model.NodeParameter;
import org.ecloudmanager.service.NodeAPIConfigurationService;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeAPISuggestions implements ConstraintFieldSuggestionsProvider {
    private NodeParameter parameter;

    public NodeAPISuggestions() {
    }

    public NodeAPISuggestions(NodeParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public List<ConstraintFieldSuggestion> getSuggestions(DeploymentObject deploymentConstraint) {
        Map<String, String> params = deploymentConstraint.getConfigValues();
        String apiName = ApplicationDeployment.class.cast(deploymentConstraint.getTop()).getInfrastructure();
        try {
            NodeAPIConfigurationService nodeAPIProvider = CDI.current().select(NodeAPIConfigurationService.class).get();
            return nodeAPIProvider.getAPI(apiName).getNodeParameterValues(nodeAPIProvider.getCredentials(apiName), parameter.getName(), params).stream()
                    .map(p -> new ConstraintFieldSuggestion(p.getDescription(), p.getValue())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
