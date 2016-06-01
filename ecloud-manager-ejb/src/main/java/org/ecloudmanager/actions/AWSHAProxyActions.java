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

package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.service.aws.AWSVmService;
import org.ecloudmanager.service.execution.Action;

import javax.inject.Inject;

@Service
public class AWSHAProxyActions {
    @Inject
    private AWSVmService vmService;

    public Action getCreatePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        return Action.single("Create Firewall Rules for Public Endpoints", () -> {
            vmService.createFirewallRules(producedServiceDeployment);
        }, producedServiceDeployment);
    }

    public Action getDeletePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        return Action.single("Delete Firewall Rules for Public Endpoints", () -> {
            vmService.deleteFirewallRules(producedServiceDeployment);
        }, producedServiceDeployment);
    }
}
