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

package org.ecloudmanager.deployment.vm.infrastructure;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.ecloudmanager.actions.VmActions;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.NodeAPISuggestions;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.APIInfo;
import org.ecloudmanager.node.model.NodeParameter;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfrastructureDeployerImpl extends InfrastructureDeployer {
    private final static String NODE_PARAMETER_NAME = "name";
    private final static String NODE_PARAMETER_CPU = "cpu";
    private final static String NODE_PARAMETER_MEMORY = "memory";
    private final static String NODE_PARAMETER_STORAGE = "storage";

    private List<String> COMMON_PARAMETERS = Arrays.asList(NODE_PARAMETER_NAME, NODE_PARAMETER_CPU, NODE_PARAMETER_MEMORY, NODE_PARAMETER_STORAGE);

    private NodeAPIConfigurationService nodeAPIProvider = CDI.current().select(NodeAPIConfigurationService.class).get();
    private SecretKey credentials;
    private String apiName;
    private VmActions vmActions = CDI.current().select(VmActions.class).get();
    private AsyncNodeAPI nodeAPI;
    private APIInfo apiInfo;

    public InfrastructureDeployerImpl(String apiName) {
        this.apiName = apiName;
        nodeAPI = nodeAPIProvider.getAPI(apiName);
        try {
            apiInfo = nodeAPI.getAPIInfo();
        } catch (Exception e) {
            throw new RuntimeException("Can't get node API info", e);
        }
        credentials = nodeAPIProvider.getCredentials(apiName);

    }

    public DeploymentObject getConfig(VMDeployment deployment) {
        ApplicationDeployment ad = (ApplicationDeployment) deployment.getTop();
        String apiName = ad.getInfrastructure();

        return deployment.createIfMissingAndGetConfig(apiInfo.getId());
    }

    public Map<String, String> getNodeParameters(VMDeployment deployment) {
        Map<String, String> result = new HashMap<>(getConfig(deployment).getConfigValues());
        result.put(NODE_PARAMETER_NAME, deployment.getConfigValue(VMDeployer.VM_NAME));
        result.put(NODE_PARAMETER_CPU, Integer.toString(deployment.getVirtualMachineTemplate().getProcessorCount()));
        result.put(NODE_PARAMETER_MEMORY, Integer.toString(deployment.getVirtualMachineTemplate().getMemory()));
        result.put(NODE_PARAMETER_STORAGE, Integer.toString(deployment.getVirtualMachineTemplate().getStorage()));
        return result;
    }


    @Override
    public void specifyConstraints(VMDeployment vmDeployment) {
        DeploymentConstraint constraint = getConfig(vmDeployment);
        try {
            List<NodeParameter> params = nodeAPI.getNodeParameters(credentials);
            params.forEach(p -> {
                if (!COMMON_PARAMETERS.contains(p.getName())) {
                    constraint.addField(ConstraintField.builder().name(p.getName()).description(p.getDescription())
                            .defaultValue(p.getDefaultValue()).required(p.getRequired())
                            .suggestionsProvider(p.getCanSuggest() ? new NodeAPISuggestions(p) : null).build());
                }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Action getCreateAction(VMDeployment vmDeployment) {
        return vmActions.getCreateVmAction(vmDeployment, nodeAPI, credentials, getNodeParameters(vmDeployment));
    }

    @Override
    public Action getDeleteAction(VMDeployment vmDeployment) {
        return vmActions.getDeleteVmAction(vmDeployment, nodeAPI, credentials);
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, VMDeployment before, VMDeployment after) {
        return vmActions.getUpdateVmAction(before, after, nodeAPI, credentials, getNodeParameters(after));
    }

    @Override
    public DeploymentObject getInfrastructureConfig(VMDeployment deployment) {
        return getConfig(deployment);
    }

    @Override
    public boolean isRecreateActionRequired(VMDeployment before, VMDeployment after) {
        Map<String, String> beforeParams = getNodeParameters(before);
        Map<String, String> afterParams = getNodeParameters(after);
        List<NodeParameter> params;
        try {
            params = nodeAPIProvider.getAPI(apiName).getNodeParameters(credentials);
        } catch (Exception e) {
            throw new RuntimeException("Can't get node parameters", e);
        }
        Map<String, MapDifference.ValueDifference<String>> diff = Maps.difference(beforeParams, afterParams).entriesDiffering();

        return diff.entrySet().stream().anyMatch(entry -> params.stream().anyMatch(parameter -> parameter.getName().equals(entry.getKey()) && !parameter.getConfigure()));
//          AWSInstanceType beforeInstanceType = AWSInstanceType.get(getAwsInstanceType(before));
//        AWSInstanceType afterInstanceType = AWSInstanceType.get(getAwsInstanceType(after));
//        if (beforeInstanceType == null || afterInstanceType == null || !afterInstanceType.isCompatible(beforeInstanceType)) {
//            return true;
//        }
//
//        if (before.getVirtualMachineTemplate().getStorage() != after.getVirtualMachineTemplate().getStorage()) {
//            if (
//                !(beforeInstanceType.isEbs() && afterInstanceType.isEbs()) ||
//                before.getVirtualMachineTemplate().getStorage() > after.getVirtualMachineTemplate().getStorage()
//            ) {
//                return true;
//            }
//        }
//
//        return !getAwsRegion(after).equals(getAwsRegion(before)) ||
//               !getAwsSubnet(after).equals(getAwsSubnet(before)) ||
//               !getAwsAmi(after).equals(getAwsAmi(before)) ||
//               !getAwsKeypair(after).equals(getAwsKeypair(before));
    }

}