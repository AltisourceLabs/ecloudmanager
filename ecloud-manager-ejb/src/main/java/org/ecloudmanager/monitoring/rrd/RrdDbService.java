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

package org.ecloudmanager.monitoring.rrd;


import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.monitoring.HaproxyStatsField;
import org.mongodb.morphia.Datastore;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Stream;

@Service
public class RrdDbService {
    private static final double DEFAULT_XFF = 0.9999999;
    private static final int RRD_STEP = 10;
    private static final int RRD_HEARTBEAT = 120;

    @Inject
    Logger log;

    @Inject
    Datastore datastore;

    @PostConstruct
    private void init() {
        RrdMongoDBBackendFactory factory = new RrdMongoDBBackendFactory(datastore.getDB().getCollection("rrd"));
        RrdDb.setDefaultFactory(factory.getName());
    }

    public RrdDb openRrdDb(DeploymentObject deploymentObject) {
        return openRrdDb(deploymentObject, false);
    }

    public RrdDb openRrdDb(DeploymentObject deploymentObject, boolean readonly) {
        String rrdPath = deploymentObject.getId().toString();
        try {
            try {
                return new RrdDb(rrdPath, readonly);
            } catch (FileNotFoundException e) {
                if (readonly) {
                    return null;
                }
                RrdDef def = new RrdDef(rrdPath, new Date().getTime()/1000, RRD_STEP);
                Stream.of(HaproxyStatsField.values())
                        .filter(HaproxyStatsField::isGraphSupported)
                        .forEach(haproxyStatsField -> {
                            DsType dsType = haproxyStatsField.getType() == HaproxyStatsField.Type.NUMBER ? DsType.GAUGE : DsType.COUNTER;
                            def.addDatasource(haproxyStatsField.getKey(), dsType, RRD_HEARTBEAT, Double.NaN, Double.NaN);
                        });
                def.addArchive(ConsolFun.AVERAGE, DEFAULT_XFF, 1, 6*60*24); // One day, 10 sec resolution
                def.addArchive(ConsolFun.AVERAGE, DEFAULT_XFF, 6, 60*24*7); // One week, 1 min resolution
                def.addArchive(ConsolFun.AVERAGE, DEFAULT_XFF, 60, 6*24*30); // One month, 10 min resolution

                return new RrdDb(def);
            }
        } catch (IOException e) {
            log.error("Cannot open rrd database", e);
            return null;
        }
    }
}
