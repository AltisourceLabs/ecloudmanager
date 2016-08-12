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

package org.ecloudmanager.node.verizon;

import org.ecloudmanager.tmrk.cloudapi.model.CreateFirewallAclType;
import org.ecloudmanager.tmrk.cloudapi.model.FirewallAclType;
import org.ecloudmanager.tmrk.cloudapi.model.FirewallAclsType;
import org.ecloudmanager.tmrk.cloudapi.model.ObjectFactory;
import org.ecloudmanager.tmrk.cloudapi.service.network.FirewallRuleService;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class FirewallAclService {
    private FirewallRuleService firewallRulesService;

    public FirewallAclService(CloudServicesRegistry registry) {
        firewallRulesService = registry.getFirewallRuleService();
    }
    public List<FirewallAclType> getFirewallServiceByEnvironmentId(String environmentId) {
        FirewallAclsType firewallAclsType = firewallRulesService.getFirewallRulesByEnvironmentId(environmentId);

        List<FirewallAclType> list = firewallAclsType.getFirewallAcl();

        return list;
    }

    public FirewallAclType createFirewallRule(String environmentId, CreateFirewallAclType createFirewallAclType) {
        ObjectFactory obj = new ObjectFactory();
        JAXBElement<CreateFirewallAclType> createFirewallAclTypeJaxb = obj.createCreateFirewallAcl
            (createFirewallAclType);
        FirewallAclType firewallAclsType = firewallRulesService.createFirewallRuleForEnvironment(environmentId,
            createFirewallAclTypeJaxb);
        return firewallAclsType;
    }
}
