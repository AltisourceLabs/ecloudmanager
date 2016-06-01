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

package org.ecloudmanager.monitoring;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.monitoring.rrd.RrdDbService;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.monitoring.HaproxyStatsRepository;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Startup
@Singleton
public class HaproxyStatsCollector {
    @Inject
    ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    Logger log;
    @Inject
    HaproxyStatsRepository haproxyStatsRepository;
    @Inject
    RrdDbService rrdDbService;

    private Client client = new ResteasyClientBuilder().connectionPoolSize(100).connectionTTL(5, TimeUnit.MINUTES).build();

    @Schedule(second="*/10", minute="*",hour="*", persistent=false)
    private void collect() {
        applicationDeploymentRepository.getAll().stream()
                .flatMap(applicationDeployment -> applicationDeployment.children(ProducedServiceDeployment.class).stream())
                .filter(producedServiceDeployment -> {
                    String monitored = producedServiceDeployment.getConfigValue(HAProxyDeployer.HAPROXY_MONITORING);
                    return "true".equals(monitored) &&
                           !StringUtils.isEmpty(HAProxyDeployer.getHaproxyIp(producedServiceDeployment));
                })
                .map(HAProxyDeployer::getHaproxyIp)
                .distinct()
                .forEach(this::collectStats);
    }

    private void collectStats(String haproxyStatsAddr) {
        client.target("http://" + haproxyStatsAddr  + ":22002" + "/;csv").request().async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                if (response.getStatus() == 200) {
                    String csv = response.readEntity(String.class);
                    csv = csv.replaceFirst("# ", "");
                    List<CSVRecord> records = null;
                    try {
                        records = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader()).getRecords();
                        collectRecords(haproxyStatsAddr, records);
                    } catch (IOException e) {
                        log.error(e);
                    }
                } else {
                    log.error("Cannot connect to haproxy stats endpoint " + haproxyStatsAddr + " response code " + response.getStatus());
                }
            }

            @Override
            public void failed(Throwable throwable) {
                log.trace("Can't get haproxy stats from " + haproxyStatsAddr, throwable);
            }
        });
    }

    private void collectRecords(String haproxyStatsAddr, List<CSVRecord> records) {
        applicationDeploymentRepository.getAll().stream()
                .flatMap(applicationDeployment -> applicationDeployment.children(ProducedServiceDeployment.class).stream())
                .filter(producedServiceDeployment -> {
                    String monitored = producedServiceDeployment.getConfigValue(HAProxyDeployer.HAPROXY_MONITORING);
                    String haproxyAddr = HAProxyDeployer.getHaproxyIp(producedServiceDeployment);
                    return haproxyStatsAddr.equals(haproxyAddr) &&
                           "true".equals(monitored);
                })
                .forEach(producedServiceDeployment -> {
                    updateRrdDbAndLatestStats(producedServiceDeployment, producedServiceDeployment.getName(), "FRONTEND", records);
                    producedServiceDeployment.children(ComponentGroupDeployment.class).forEach(componentGroupDeployment -> {
                        updateRrdDbAndLatestStats(componentGroupDeployment, componentGroupDeployment.getName(), "BACKEND", records);
                    });
                    producedServiceDeployment.stream(VMDeployment.class).forEach(vmDeployment -> {
                        updateRrdDbAndLatestStats(vmDeployment, vmDeployment.getParent().getName(), vmDeployment.getName(), records);
                    });
                });
    }

    private void updateRrdDbAndLatestStats(DeploymentObject deploymentObject, String pxname, String svname, List<CSVRecord> records) {
        records.stream()
                .filter(record ->
                        record.get(HaproxyStatsField.PROXY_NAME.getKey()).equals(pxname) &&
                        record.get(HaproxyStatsField.SERVICE_NAME.getKey()).startsWith(svname))
                .forEach(record -> {
                    // Update the latest stats
                    HaproxyStats haproxyStats = haproxyStatsRepository.getStats(deploymentObject.getId());
                    if (haproxyStats == null) {
                        haproxyStats = new HaproxyStats();
                        haproxyStats.setDeploymentObjectId(deploymentObject.getId());
                    } else {
                        haproxyStats.setTimestamp(new Date());
                    }
                    haproxyStats.setData(record.toMap());

                    haproxyStatsRepository.save(haproxyStats);

                    // Update rrd db
                    RrdDb rrdDb = rrdDbService.openRrdDb(deploymentObject);
                    try {
                        Sample sample = rrdDb.createSample();
                        Stream.of(sample.getDsNames()).forEach(dsName -> {
                            if (!StringUtils.isEmpty(record.get(dsName))) {
                                sample.setValue(dsName, Double.parseDouble(record.get(dsName)));
                            }
                        });
                        sample.update();
                        rrdDb.close(); // Close flushes data to mongodb, don't need to close when couldn't create sample
                    } catch (IOException e) {
                        log.error("Failed to store sample in rrd db", e);
                    }
                });
    }
}
