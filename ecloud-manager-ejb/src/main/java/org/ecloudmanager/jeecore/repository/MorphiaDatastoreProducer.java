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

package org.ecloudmanager.jeecore.repository;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.es.ExternalServiceDeployment;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.monitoring.HaproxyStats;
import org.ecloudmanager.repository.deployment.StackTraceElementEntityConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

public class MorphiaDatastoreProducer {
    private static final String CONFIG_FILE_NAME = "dp.properties";
    private static final String MONGODB_HOST = "mongodb.host";
    private static final String MONGODB_PORT = "mongodb.port";
    private static final String DB_NAME = "deployment_app";

    private static Datastore datastore;

    @Inject
    private Logger log;

    public static MongoClient mongoClient() throws UnknownHostException {
        return mongoClient(null);
    }

    private static MongoClient mongoClient(Logger log) throws UnknownHostException {
        Properties properties = new Properties();
        String fileName = System.getProperty("jboss.server.config.dir") + "/" + CONFIG_FILE_NAME;
        File f = new File(fileName);
        log(log, Level.INFO, "Loading MongoDB configuration from file " + fileName);
        try {
            properties.load(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            log(log, Level.WARN, "MongoDB configuration file " + fileName + " not found, using FongoDB");
            return fongoClient();
        } catch (IOException e) {
            log(log, Level.WARN, "Error loading MongoDB configuration file, using FongoDB", e);
        }
        String host = properties.getProperty(MONGODB_HOST, "127.0.0.1");
        String portString = properties.getProperty(MONGODB_PORT, "27017");
        log(log, Level.INFO, "Connecting to MongoDB on " + host + ":" + portString);
        int port = Integer.parseInt(portString);
        // TODO support other config options
        return new MongoClient(host, port);
    }

    private static MongoClient fongoClient() {
        Fongo f = new Fongo("fongo");
        return f.getMongo();
    }

    private static void log(Logger log, Level level, String message) {
        log(log, level, message, null);
    }

    private static void log(Logger log, Level level, String message, Throwable t) {
        if (log == null) {
            System.out.println(level + " : " + message);
            if (t != null) {
                t.printStackTrace();
            }
        } else {
            log.log(level, message, t);
        }

    }

    @Produces
    public Datastore datastore() throws UnknownHostException {
        if (datastore == null) {
            final Morphia morphia = new Morphia();
            morphia.getMapper().getConverters().addConverter(StackTraceElementEntityConverter.class);
            morphia.mapPackageFromClass(ApplicationDeployment.class)
                .mapPackageFromClass(DeploymentAttempt.class)
                .mapPackageFromClass(VMDeployment.class)
                .mapPackageFromClass(Recipe.class)
                .mapPackageFromClass(ExternalServiceDeployment.class)
                .mapPackageFromClass(ProducedServiceDeployment.class)
            ;
            morphia.map(HaproxyStats.class);
            datastore = morphia.createDatastore(mongoClient(log), DB_NAME);
            datastore.ensureIndexes();
            datastore.ensureCaps();
        }
        return datastore;
    }
}
