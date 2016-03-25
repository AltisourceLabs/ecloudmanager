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

package org.ecloudmanager.ws.rest.verizon.deployment;

import org.ecloudmanager.service.verizon.FirewallService;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/firewall")
public class FirewallRuleCreation {

    @Inject
    private CloudCachedEntityService cacheService;

    @Inject
    private FirewallService firewallService;

    public FirewallRuleCreation() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rule")
    public Response createFirewallRule(CreateFirewallRule rule) {

        Response.ResponseBuilder builder;

        EnvironmentType environmentType = cacheService.getByHrefOrName(EnvironmentType.class, rule.getEnvironmentName
            ());
        try {
            firewallService.createFirewallRule(environmentType.getHref(), createRule(rule));
            // Create an "ok" response
            builder = Response.status(Response.Status.CREATED);

        } catch (Exception ex) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", ex.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }
        return builder.build();

    }

    private CreateFirewallAclType createRule(CreateFirewallRule rule) {
        CreateFirewallAclType firewallRule = new CreateFirewallAclType();

        ObjectFactory objectFactory = new ObjectFactory();

        //source
        firewallRule.setPermission(AclPermissionTypeEnum.fromValue(rule.getPermission()));
        firewallRule.setProtocol(ProtocolTypeEnum.fromValue(rule.getProtocol()));

        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.fromValue(rule.getSourceType()));
        fwaclet.setExternalIpAddress(objectFactory.createFirewallAclEndpointTypeExternalIpAddress(rule
            .getSourceTypeValue()));
        firewallRule.setSource(fwaclet);

        //destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.fromValue(rule.getDestinationType()));
        firewallRule.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));

        PortRangeType portRange = new PortRangeType();
        boolean port = false;

        if (rule.getPortStartRange() != null) {
            portRange.setStart(rule.getPortStartRange());
            port = true;
        }

        if (rule.getPortEndRange() != null) {
            portRange.setEnd(rule.getPortEndRange());
            port = true;
        }
        if (port) {
            firewallRule.setPortRange(objectFactory.createFirewallAclTypePortRange(portRange));
        }

        return firewallRule;
    }
}
