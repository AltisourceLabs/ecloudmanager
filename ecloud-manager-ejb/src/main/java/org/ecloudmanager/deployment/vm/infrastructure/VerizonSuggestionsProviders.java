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

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.core.Config;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestion;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestionsProvider;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.service.verizon.VmFieldsCompletionService;

import javax.enterprise.inject.spi.CDI;
import java.util.Collections;
import java.util.List;

import static org.ecloudmanager.deployment.core.ConstraintFieldSuggestion.*;

public class VerizonSuggestionsProviders {
    public static class EnvironmentSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
            VmFieldsCompletionService vmFieldsCompletionService = CDI.current().select(VmFieldsCompletionService
                .class).get();
            return suggestionsList(vmFieldsCompletionService.getEnvironments());
        }
    }

    public static ConstraintFieldSuggestionsProvider createEnvironmentSuggestionsProvider() {
        return new EnvironmentSuggestionsProvider();
    }

    public static class CatalogSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
            VmFieldsCompletionService vmFieldsCompletionService = CDI.current().select(VmFieldsCompletionService
                .class).get();
            String environment = VerizonInfrastructureDeployer.getEnvironment((Config) deploymentConstraint);
            return suggestionsList(vmFieldsCompletionService.getCatalogs(environment));
        }
    }

    public static ConstraintFieldSuggestionsProvider createCatalogSuggestionsProvider() {
        return new CatalogSuggestionsProvider();
    }

    public static class NetworkSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
            VmFieldsCompletionService vmFieldsCompletionService = CDI.current().select(VmFieldsCompletionService
                .class).get();
            String environment = VerizonInfrastructureDeployer.getEnvironment((Config) deploymentConstraint);
            if (StringUtils.isEmpty(environment)) {
                return Collections.emptyList();
            }
            return suggestionsList(vmFieldsCompletionService.getNetworks(environment));
        }
    }

    public static ConstraintFieldSuggestionsProvider createNetworkSuggestionsProvider() {
        return new NetworkSuggestionsProvider();
    }

    public static class RowSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
            VmFieldsCompletionService vmFieldsCompletionService = CDI.current().select(VmFieldsCompletionService
                .class).get();
            String environment = VerizonInfrastructureDeployer.getEnvironment((Config) deploymentConstraint);
            if (StringUtils.isEmpty(environment)) {
                return Collections.emptyList();
            }
            return suggestionsList(vmFieldsCompletionService.getRows(environment));
        }
    }

    public static ConstraintFieldSuggestionsProvider createRowSuggestionsProvider() {
        return new RowSuggestionsProvider();
    }

    public static class GroupSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
        @Override
        public List<ConstraintFieldSuggestion> getSuggestions(DeploymentConstraint deploymentConstraint) {
            VmFieldsCompletionService vmFieldsCompletionService = CDI.current().select(VmFieldsCompletionService
                .class).get();
            String environment = VerizonInfrastructureDeployer.getEnvironment((Config) deploymentConstraint);
            if (StringUtils.isEmpty(environment)) {
                return Collections.emptyList();
            }
            String row = VerizonInfrastructureDeployer.getRow((Config) deploymentConstraint);
            if (StringUtils.isEmpty(row)) {
                return Collections.emptyList();
            }
            return suggestionsList(vmFieldsCompletionService.getGroups(environment, row));
        }
    }

    public static ConstraintFieldSuggestionsProvider createGroupSuggestionsProvider() {
        return new GroupSuggestionsProvider();
    }

}
