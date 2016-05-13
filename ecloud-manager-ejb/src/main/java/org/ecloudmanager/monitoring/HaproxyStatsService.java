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
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.monitoring.rrd.RrdDbService;
import org.ecloudmanager.repository.monitoring.HaproxyStatsRepository;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HaproxyStatsService {
    @Inject
    HaproxyStatsRepository haproxyStatsRepository;
    @Inject
    RrdDbService rrdDbService;
    @Inject
    Logger log;

    public HaproxyStatsData loadHaproxyStats(Date startDate, DeploymentObject deploymentObject, HaproxyStatsField timeSeriesField) {
        HaproxyStats latestStats = haproxyStatsRepository.getStats(deploymentObject.getId());
        if (latestStats == null) {
            return null;
        }

        HaproxyStatsData data = new HaproxyStatsData();
        data.setTimestamp(latestStats.getTimestamp());
        Map<HaproxyStatsField, String> latestData =
                Stream.of(HaproxyStatsField.values())
                        .filter(sf -> !StringUtils.isEmpty(latestStats.getData().get(sf.getKey())))
                        .sorted((o1, o2) -> Integer.compare(o2.ordinal(), o1.ordinal()))
                        .collect(Collectors.toMap(sf -> sf, sf -> latestStats.getData().get(sf.getKey()), (k1,k2) -> k1, TreeMap::new));
        data.setLatestData(latestData);

        data.setTimeSeriesField(timeSeriesField);

        Map<Object, Number> timeSeries = createTimeSeries(startDate, deploymentObject, timeSeriesField);
        data.setTimeSeriesData(timeSeries);

        return data;
    }

    private Map<Object, Number> createTimeSeries(Date startDate, DeploymentObject deploymentObject, HaproxyStatsField field) {
        if (field == null) {
            return Collections.emptyMap();
        }

        RrdDb rrdDb = rrdDbService.openRrdDb(deploymentObject, true);
        if (rrdDb == null) {
            return Collections.emptyMap();
        }

        long start = startDate.getTime() / 1000;
        long end = new Date().getTime() / 1000;
        //ConsolFun consolFun = end - start < 60 * 60 * 24 ? ConsolFun.LAST : ConsolFun.AVERAGE;
        ConsolFun consolFun = ConsolFun.AVERAGE;
        FetchRequest fetchRequest = rrdDb.createFetchRequest(consolFun, start, end);
        fetchRequest.setFilter(field.getKey());
        try {
            FetchData fetchData = fetchRequest.fetchData();
            long[] timestamps = fetchData.getTimestamps();
            double[] values = fetchData.getValues(field.getKey());
            Map<Object, Number> result = new TreeMap<>();
            for (int i = 0; i < timestamps.length; i++) {
                result.put(timestamps[i]*1000, Double.isNaN(values[i]) ? null : values[i]);
            }

            // Don't need to close - we don't write here
            // rrdDb.close();

            return result;
        } catch (IOException e) {
            log.error("Cannot fetch data from rrd", e);
            return null;
        }
    }
}
