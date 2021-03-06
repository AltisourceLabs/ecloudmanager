/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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

import org.ecloudmanager.deployment.core.ConstraintFieldSuggestion;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestionsProvider;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.domain.template.SshConfiguration;
import org.ecloudmanager.service.template.SshConfigurationService;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.stream.Collectors;

import static org.ecloudmanager.deployment.core.ConstraintFieldSuggestion.suggestionsList;

public class SshConfigurationSuggestionsProvider implements ConstraintFieldSuggestionsProvider {
    @Override
    public List<ConstraintFieldSuggestion> getSuggestions(DeploymentObject deploymentConstraint) {
        SshConfigurationService sshConfigurationService = CDI.current().select(SshConfigurationService.class).get();
        List<String> sshConfigNames = sshConfigurationService.getAllForCurrentUser().stream()
                .map(SshConfiguration::getName)
                .collect(Collectors.toList());
        return suggestionsList(sshConfigNames);
    }
}
