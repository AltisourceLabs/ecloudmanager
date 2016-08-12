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

package org.ecloudmanager.deployment.ps;

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.core.AbstractDeployer;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.gateway.GatewayDeployer;
import org.ecloudmanager.deployment.gateway.GatewayDeployment;
import org.ecloudmanager.deployment.gateway.GatewaySuggestionsProvider;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.ps.cg.HAProxyBackendConfig;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureHAProxyDeployer;
import org.ecloudmanager.repository.deployment.GatewayRepository;
import org.ecloudmanager.service.deployment.geolite.AclOperator;
import org.ecloudmanager.service.deployment.geolite.GeolocationExpr;
import org.ecloudmanager.service.deployment.geolite.GeolocationRecordType;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.provisioning.HAProxyConfigurator;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;

public class HAProxyDeployer extends AbstractDeployer<ProducedServiceDeployment> {
    public static final String PORT = "port";
    public static final String BIND_IP = "bind_ip";
    public static final String HAPROXY_MONITORING = "ha_proxy_monitoring";
    public static final String GATEWAY = "gateway";

    private HAProxyConfigurator haProxyConfigurator;

    private static GatewayDeployment getGatewayDeployment(ProducedServiceDeployment deployment) {
        String gateway = deployment.getConfigValue(GATEWAY);
        GatewayRepository gatewayRepository = CDI.current().select(GatewayRepository.class).get();
        return gatewayRepository.find(gateway);
    }

    public static String getHaproxyIp(ProducedServiceDeployment deployment) {
        GatewayDeployment gatewayDeployment = getGatewayDeployment(deployment);
        return gatewayDeployment == null ?
                null :
                InfrastructureDeployer.getIP(gatewayDeployment.children(GatewayVMDeployment.class).get(0));
    }

    public static GatewayVMDeployment getGatewayVmDeployment(ProducedServiceDeployment deployment) {
        GatewayDeployment gatewayDeployment = getGatewayDeployment(deployment);
        return gatewayDeployment == null ?
                null :
                gatewayDeployment.children(GatewayVMDeployment.class).get(0);
    }

    public static List<String> generateHAProxyFrontendConfig(String frontendName, HAProxyFrontendConfig frontendConfig) {
        List<String> config = new ArrayList<>();

        config.add("mode " + frontendConfig.getMode());

        if (!StringUtils.isEmpty(frontendConfig.getDefaultBackend())) {
            config.add("default_backend " + frontendConfig.getDefaultBackend());
        }

        generateGeoRules(frontendName, frontendConfig, config);
        generateABTestingRules(frontendName, frontendConfig, config);

        config.addAll(frontendConfig.getConfig());
        return config;
    }

    private static void generateGeoRules(String frontendName, HAProxyFrontendConfig frontendConfig, List<String> config) {
        if (frontendConfig.getGeolocationRules().size() == 0) {
            return;
        }

        boolean http = frontendConfig.getMode() == HAProxyMode.HTTP;
        boolean hasCities = frontendConfig.getGeolocationRules().stream()
                .flatMap(geolocationRule -> geolocationRule.getLocations().stream())
                .map(GeolocationExpr::getRecord)
                .anyMatch(geolocationRecord -> geolocationRecord.getType() == GeolocationRecordType.CITY);

        if (http && frontendConfig.getUseXff()) {
            config.add("acl h_xff_exists req.hdr(X-Forwarded-For) -m found");
            config.add("acl h_forwarded_exists req.hdr(Forwarded) -m found");
            config.add("http-request set-header X-Country %[req.hdr_ip(X-Forwarded-For,-1),map_ip(/etc/haproxy/geolite/countrymap.txt)] if h_xff_exists");
            config.add("http-request set-header X-Country %[req.hdr_ip(Forwarded,-1),map_ip(/etc/haproxy/geolite/countrymap.txt)] if h_forwarded_exists");
            config.add("http-request set-header X-Country %[src,map_ip(/etc/haproxy/geolite/countrymap.txt)] if !h_xff_exists !h_forwarded_exists");

            if (hasCities) {
                config.add("http-request set-header X-City %[req.hdr_ip(X-Forwarded-For,-1),map_ip(/etc/haproxy/geolite/citymap.txt)] if h_xff_exists");
                config.add("http-request set-header X-City %[req.hdr_ip(Forwarded,-1),map_ip(/etc/haproxy/geolite/citymap.txt)] if h_forwarded_exists");
                config.add("http-request set-header X-City %[src,map_ip(/etc/haproxy/geolite/citymap.txt)] if !h_xff_exists !h_forwarded_exists");
            }
        } else if (http) {
            config.add("http-request set-header X-Country %[src,map_ip(/etc/haproxy/geolite/countrymap.txt)]");
            if (hasCities) {
                config.add("http-request set-header X-City %[src,map_ip(/etc/haproxy/geolite/citymap.txt)]");
            }
        }

        StringBuilder countryGeoids = new StringBuilder();
        StringBuilder cityGeoids = new StringBuilder();

        frontendConfig.getGeolocationRules().stream()
                .flatMap(geolocationRule -> geolocationRule.getLocations().stream())
                .map(GeolocationExpr::getRecord)
                .distinct()
                .forEach(geolocationRecord -> {
                    String location;
                    boolean isCountry = geolocationRecord.getType() == GeolocationRecordType.COUNTRY;
                    if (http) {
                        location = isCountry ? "req.hdr(X-Country)" :  "req.hdr(X-City)";
                    } else {
                        location = isCountry ? "src,map_ip(/etc/haproxy/geolite/countrymap.txt)" : "src,map_ip(/etc/haproxy/geolite/citymap.txt)";
                    }
                    config.add("acl geo" + geolocationRecord.getGeoid() + " " + location + " -m str " + geolocationRecord.getGeoid());

                    if (isCountry) {
                        countryGeoids.append(geolocationRecord.getGeoid()).append(" ");
                    } else {
                        cityGeoids.append(geolocationRecord.getGeoid()).append(" ");
                    }
                });

        if (countryGeoids.length() > 0) {
            config.add("# country geoids: " + countryGeoids.toString().trim());
        }
        if (cityGeoids.length() > 0) {
            config.add("# city geoids: " + cityGeoids.toString().trim());
        }

        frontendConfig.getGeolocationRules().forEach(geolocationRule -> {
            String rejectStr = http ? "http-request deny if " : "tcp-request content reject if ";
            String ruleStart = geolocationRule.getBackendName() == null ?
                    rejectStr : "use_backend " + geolocationRule.getBackendName() + " if ";
            StringBuilder builder = new StringBuilder();
            geolocationRule.getLocations().forEach(geolocationExpr -> {
                if (builder.length() != 0 && geolocationExpr.getOperator() != AclOperator.AND) {
                    builder.append(geolocationExpr.getOperator().name().toLowerCase()).append(" ");
                }
                if (geolocationExpr.getNegate()) {
                    builder.append("!");
                }

                builder.append("geo").append(geolocationExpr.getRecord().getGeoid()).append(" ");
            });
            builder.insert(0, ruleStart);
            config.add(builder.toString());
        });
    }

    private static void generateABTestingRules(String frontendName, HAProxyFrontendConfig frontendConfig, List<String> config) {
        // A/B testing ACLs
        int totalWeight = 100;

        if (frontendConfig.getStickyBackends() && frontendConfig.getMode() == HAProxyMode.HTTP) {
            config.add("acl cook-present req.cook(" + frontendName + "-BACKENDID) -m found");
            config.add("use_backend %[req.cook(" + frontendName + "-BACKENDID)] if cook-present");
            config.add("http-response add-header Set-Cookie " + frontendName + "-BACKENDID=%b");
        }

        for (BackendWeight backendWeight : frontendConfig.getBackendWeights()) {
            String backend = backendWeight.getBackendName();
            Integer weight = backendWeight.getWeight();
            config.add("acl use-" + backend + " rand(" + totalWeight + ") lt " + weight);
            config.add("use_backend " + backend + " if use-" + backend);
            totalWeight -= weight;
        }
    }

    public static String generateHAProxyServerOptions(String serverName, HAProxyBackendConfig backendConfig) {
        StringBuilder options = new StringBuilder(backendConfig.getServerOptions());
        if (backendConfig.getStickyServers() && backendConfig.getMode() == HAProxyMode.HTTP) {
            options.append(" cookie " + serverName);
        }
        return options.toString();
    }

    public static List<String> generateHAProxyBackendConfig(String name, HAProxyBackendConfig backendConfig) {
        List<String> config = new ArrayList<>();

        config.add("mode " + backendConfig.getMode());

        if (backendConfig.getStickyServers() && backendConfig.getMode() == HAProxyMode.HTTP) {
            config.add("cookie " + name + "-SRVID insert indirect nocache");
        }

        config.addAll(backendConfig.getConfig());
        return config;
    }

    private HAProxyConfigurator getConfigurator(ProducedServiceDeployment deployment) {
        if (haProxyConfigurator == null) {
            GatewayDeployment gatewayDeployment = getGatewayDeployment(deployment);

            String etcdAddress = GatewayDeployer.getEtcdAddress(gatewayDeployment);
            String etcdPath = GatewayDeployer.getEtcdPath(gatewayDeployment);
            haProxyConfigurator = HAProxyConfigurator.create(etcdPath, etcdAddress);
        }
        return haProxyConfigurator;
    }

    private InfrastructureHAProxyDeployer getInfrastructureHaproxyDeployer(ProducedServiceDeployment deployment) {
        GatewayVMDeployment gatewayVmDeployment = getGatewayVmDeployment(deployment);
        InfrastructureHAProxyDeployer d = new InfrastructureHAProxyDeployer();
        //return gatewayVmDeployment.getInfrastructure().getHaProxyDeployer();
        return d;
    }

    private void configure(ProducedServiceDeployment serviceDeployment) {
        List<String> config = getFrontendConfigWithBind(serviceDeployment);
        HAProxyConfigurator configurator = getConfigurator(serviceDeployment);
        configurator.saveFrontend(serviceDeployment.getName(), config);
        for (ComponentGroupDeployment componentGroupDeployment : serviceDeployment.getComponentGroups()) {
            List<String> backendConfig = generateHAProxyBackendConfig(componentGroupDeployment.getName(), componentGroupDeployment.getHaProxyBackendConfig());
            configurator.saveBackend(componentGroupDeployment.getName(), backendConfig);

            componentGroupDeployment.stream(VMDeployment.class).forEach(vmDeployment -> {
                String serverName = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
                String ip = InfrastructureDeployer.getIP(vmDeployment);
                @NotNull List<Endpoint> endpoints = vmDeployment.getVirtualMachineTemplate().getEndpoints();
                // TODO - the user shuould be able to select an endpoint for the backend server when there's more then one in a runlist
                String port = endpoints.get(0).getPort().toString();
                configurator.saveServer(
                        componentGroupDeployment.getName(),
                        serverName,
                        ip,
                        port,
                        generateHAProxyServerOptions(serverName, componentGroupDeployment.getHaProxyBackendConfig())
                );
            });
        }
    }

    private void deleteConfiguration(ProducedServiceDeployment deployable) {
        HAProxyConfigurator configurator = getConfigurator(deployable);
        configurator.deleteFrontend(deployable.getName());
        for (ComponentGroupDeployment cg : deployable.getComponentGroups()) {
            configurator.deleteBackend(cg.getName());
            cg.stream(VMDeployment.class).forEach(vmDeployment -> {
                String serverName = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
                configurator.deleteServer(cg.getName(), serverName);
            });
        }
    }

    @NotNull
    private List<String> getFrontendConfigWithBind(ProducedServiceDeployment serviceDeployment) {
        HAProxyFrontendConfig frontendConfig = serviceDeployment.getHaProxyFrontendConfig();

        List<String> config = generateHAProxyFrontendConfig(serviceDeployment.getName(), frontendConfig);

        String port = serviceDeployment.getConfigValue(PORT);
        String ip = serviceDeployment.getConfigValue(BIND_IP);

        config.add("bind " + ip + ":" + port);

        return config;
    }

    @Override
    public void specifyConstraints(ProducedServiceDeployment deployment) {
        deployment.addField(ConstraintField.builder().name(PORT).description("Service port").type(ConstraintField.Type.NUMBER).build());
        deployment.addField(ConstraintField.builder().name(BIND_IP).description("Bind IP").defaultValue("*").build());
        // TODO support multiple IP addresses??
        deployment.addField(ConstraintField.builder()
                .name(GATEWAY)
                .description("HAProxy Gateway")
                .allowReference(false)
                .suggestionsProvider(new GatewaySuggestionsProvider())
                .build()
        );
        deployment.addField(ConstraintField.builder()
                .name(HAPROXY_MONITORING)
                .description("HAProxy Monitoring Enabled")
                .required(false)
                .readOnly(true)
                .allowReference(false)
                .build()
        );
    }

    @Override
    public Action getBeforeChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return Action.actionGroup(
                "Configure HAProxy",
                Action.single("Configure HAProxy frontend/backend", () -> {
                    configure(deployable);
                    return null;
                }, deployable),
                getInfrastructureHaproxyDeployer(deployable).getCreateAction(deployable)
        );
    }

    @Override
    public Action getAfterChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return Action.actionGroup(
                "Delete HAProxy Configuration",
                Action.single("Delete HAProxy Frontend/Backend Configuration", () -> {
                    deleteConfiguration(deployable);
                    return null;
                }, deployable),
                getInfrastructureHaproxyDeployer(deployable).getDeleteAction(deployable)
        );
    }

    @Override
    public Action getBeforeChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        return null;
    }

    @Override
    public Action getAfterChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        // Doesn't make any sense to check if update is needed as this operation is pretty fast
        return Action.actionSequence(
                "Update HAProxy Configuration for " + after.getName(),
                getAfterChildrenDeletedAction(before),
                getAfterChildrenCreatedAction(after)
        );
    }

}
