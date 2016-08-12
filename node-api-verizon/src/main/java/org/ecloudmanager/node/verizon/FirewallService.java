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

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.model.CreateFirewallAclType;
import org.ecloudmanager.tmrk.cloudapi.model.FirewallAclType;
import org.ecloudmanager.tmrk.cloudapi.model.ObjectFactory;
import org.ecloudmanager.tmrk.cloudapi.service.network.FirewallRuleService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

public class FirewallService {

    private Logger log;

    private FirewallRuleService firewallRuleService;

    public FirewallService(CloudServicesRegistry registry) {
        firewallRuleService = registry.getFirewallRuleService();
    }
    public void createFirewallRule(String environmentId, CreateFirewallAclType firewallRule) throws Exception {

        ObjectFactory objectFactory = new ObjectFactory();
        try {
            FirewallAclType rule = firewallRuleService.createFirewallRuleForEnvironment(TmrkUtils.getIdFromHref
                (environmentId), objectFactory.createCreateFirewallAcl(firewallRule));
            log.info("Firewall rule created: " + rule.getName());
        } catch (CloudapiException ex) {
            log.error("Exception during firewall rule creation: " + ex.getMessage());
            throw new Exception(ex);
        }
    }
}
