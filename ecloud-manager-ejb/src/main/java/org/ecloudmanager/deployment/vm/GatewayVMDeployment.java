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

package org.ecloudmanager.deployment.vm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class GatewayVMDeployment extends VMDeployment {
    public static final String HAPROXY_STATS = "haproxyStats";
    public static final String ETCD_NODE = "etcd_node";
    public static final String ETCD_PATH = "etcd_path";
    private static List<Recipe> gatewayRunlist = new ArrayList<>();
    private static Endpoint haproxyStats;
    static {
        Recipe selinux = new Recipe("selinux");
        selinux.setVersion("= 0.9.0");
        ChefAttribute selinuxAttribute = new ChefAttribute("selinux",
                "{" +
                "\"booleans\": {\"haproxy_connect_any\": 1 }" +
                "}");
        selinuxAttribute.setEnvironmentAttribute(false);
        selinux.addChefAttribute(selinuxAttribute);

        Recipe haproxy = new Recipe("haproxy");
        haproxy.setVersion("= 1.6.6");
        ChefAttribute attribute = new ChefAttribute("haproxy",
                "{" +
                "\"members\":[], " +
                "\"admin\": {\"address_bind\": \"0.0.0.0\", \"port\": 22002, \"options\": {\"stats\": \"uri /\"} }" +
                "}");
        attribute.setEnvironmentAttribute(false);
        haproxy.addChefAttribute(attribute);
        haproxyStats = new Endpoint(HAPROXY_STATS);
        haproxyStats.setPort(22002);
        haproxyStats.setConstant(true);
        haproxy.addEndpoint(haproxyStats);

        Recipe confd = new Recipe("rf_infra-confd");
        confd.setVersion("= 0.1.0");
        String reloadCommand = "/usr/local/bin/optimize_maps.sh; haproxy -f /etc/haproxy/haproxy.cfg -f /var/lib/haproxy/haproxy-confd.cfg -p /var/run/haproxy.pid -D -sf $(cat /var/run/haproxy.pid)";
        ChefAttribute confdAttrs = new ChefAttribute("rf_infra-confd", "{\"etcd_nodes\":[\"{{" + ETCD_NODE + "}}\"], \"etcd_path\":\"{{" + ETCD_PATH + "}}\", \"haproxy_reload_cmd\":\"" + reloadCommand + "\"}");
        confdAttrs.setEnvironmentAttribute(false);
        confd.addChefAttribute(confdAttrs);

        Recipe haproxyGeo = new Recipe("ecloudmanager-haproxy-geo");
        haproxyGeo.setVersion("= 0.1.0");

        gatewayRunlist.add(selinux);
        gatewayRunlist.add(haproxy);
        gatewayRunlist.add(confd);
        gatewayRunlist.add(haproxyGeo);
    }

    GatewayVMDeployment() {}

    public GatewayVMDeployment(VirtualMachineTemplate virtualMachineTemplate) {
        setVirtualMachineTemplate(virtualMachineTemplate);
    }

    @Override
    public Stream<Deployable> getRequired() {
        return Stream.empty();
    }

    @Override
    public List<Pair<DeploymentObject, Endpoint>> getLinkedRequiredEndpoints() {
        return Collections.emptyList();
    }

    @Override
    public List<Recipe> getRunlist() {
        List<Recipe> runlist = new ArrayList<>();
        //List<Recipe> runlist = new ArrayList<>(getInfrastructure().getRunlistHolder().getRunlist());
        runlist.addAll(gatewayRunlist);
        return runlist;
    }

    public List<Endpoint> getEndpoints() {
        return Lists.newArrayList(haproxyStats);
    }
}
