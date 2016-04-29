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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.repository.monitoring.HaproxyStatsRepository;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ecloudmanager.monitoring.HaproxyStatsField.PROXY_NAME;
import static org.ecloudmanager.monitoring.HaproxyStatsField.SERVICE_NAME;

@Service
public class HaproxyStatsService {
    @Inject
    HaproxyStatsRepository haproxyStatsRepository;

    public HaproxyStatsData loadHaproxyStats(Date startDate, DeploymentObject deploymentObject, HaproxyStatsField timeSeriesField) {
        ProducedServiceDeployment producedServiceDeployment = DeploymentObject.getParentOfType(deploymentObject, ProducedServiceDeployment.class, false);
        if (producedServiceDeployment == null) {
            return null;
        }

        String haproxyStatsAddress = producedServiceDeployment.getConfigValue(HAProxyDeployer.HAPROXY_IP);
        if (StringUtils.isEmpty(haproxyStatsAddress)) {
            return null;
        }
        haproxyStatsAddress = haproxyStatsAddress + ":22002";

        List<HaproxyStats> stats = haproxyStatsRepository.getStats(haproxyStatsAddress, startDate);
        if (stats.size() == 0) {
            return null;
        }

        Map<HaproxyStats, Integer> indicesMap = stats.stream()
                .filter(s -> s.getData() != null)
                .map(s -> new ImmutablePair<>(s, getIndexInStatsRecordsList(s, deploymentObject)))
                .filter(p -> p.getRight() != null)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));

        HaproxyStats latestStats = stats.get(0);
        Integer latestStatsIndex = indicesMap.get(latestStats);
        if (latestStatsIndex == null) {
            return null; // In the latest stats there's no csv record for this deployment object
        }

        HaproxyStatsData data = new HaproxyStatsData();
        data.setTimestamp(latestStats.getTimestamp());
        Map<HaproxyStatsField, String> latestData =
                Stream.of(HaproxyStatsField.values())
                        .filter(sf -> !StringUtils.isEmpty(latestStats.getData().get(latestStatsIndex).get(sf.getKey())))
                        .sorted((o1, o2) -> Integer.compare(o2.ordinal(), o1.ordinal()))
                        .collect(Collectors.toMap(sf -> sf, sf -> latestStats.getData().get(latestStatsIndex).get(sf.getKey()), (k1,k2) -> k1, TreeMap::new));
        data.setLatestData(latestData);

        data.setTimeSeriesField(timeSeriesField);

        Map<Object, Number> timeSeries = createTimeSeries(timeSeriesField, stats, indicesMap);
        data.setTimeSeriesData(timeSeries);

        return data;
    }

    private Map<Object, Number> createTimeSeries(HaproxyStatsField field, List<HaproxyStats> stats, Map<HaproxyStats, Integer> indicesMap) {
        if (field == null) {
            return Collections.emptyMap();
        }

        return stats.stream()
                .filter(s -> indicesMap.get(s) != null)
                .filter(s -> !StringUtils.isEmpty(s.getData().get(indicesMap.get(s)).get(field.getKey())))
                .collect(Collectors.toMap(s -> s.getTimestamp().getTime(), s -> Long.parseLong(s.getData().get(indicesMap.get(s)).get(field.getKey()))));
    }

    private Integer getIndexInStatsRecordsList(HaproxyStats stats, DeploymentObject deploymentObject) {
        String pxname, svname;
        if (deploymentObject instanceof VMDeployment) {
            pxname = deploymentObject.getParent().getName();
            svname = deploymentObject.getName();
        } else if (deploymentObject instanceof ProducedServiceDeployment) {
            pxname = deploymentObject.getName();
            svname = "FRONTEND";
        } else if (deploymentObject instanceof ComponentGroupDeployment) {
            pxname = deploymentObject.getName();
            svname = "BACKEND";
        } else {
            return null;
        }

        return stats.getData().stream()
                .filter(r -> r.get(PROXY_NAME.getKey()).equals(pxname) && r.get(SERVICE_NAME.getKey()).startsWith(svname))
                .findAny()
                .map(r -> stats.getData().indexOf(r))
                .orElse(null);
    }
}
