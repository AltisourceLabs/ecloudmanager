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

package org.ecloudmanager.service.provisioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mousio.etcd4j.EtcdClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;

public class HAProxyConfigurator {
    private Logger log = LogManager.getLogger(HAProxyConfigurator.class);

    private static final char PATH_SEPARATOR = '/';
    private static final String PATH_BACKEND = "backend";
    private static final String PATH_FRONTEND = "frontend";
    private static final String PATH_LISTEN = "listen";
    private static final String PATH_OPTIONS = "options";

    private String prefix;
    private EtcdClient etcdClient;

    private HAProxyConfigurator(String prefix, String... etcdServers) {
        this.prefix = prefix;
        URI[] uris = new URI[etcdServers.length];
        for (int i = 0; i < etcdServers.length; i++) {
            uris[i] = URI.create(etcdServers[i]);
        }
        etcdClient = new EtcdClient(uris);
    }

    public static HAProxyConfigurator create(String prefix, String... etcdServers) {
        return new HAProxyConfigurator(prefix, etcdServers);
    }


    private enum Path {
        BACKEND(PATH_BACKEND),
        FRONTEND(PATH_FRONTEND),
        LISTEN(PATH_LISTEN);
        private String type;

        Path(String type) {
            this.type = type;
        }

        public String optionsPath(String prefix, String name) {
            return keyPath(prefix, name) + PATH_SEPARATOR + PATH_OPTIONS;
        }

        public String keyPath(String prefix, String name) {
            return prefix + PATH_SEPARATOR + type + PATH_SEPARATOR + name;
        }
    }

    public void saveBackend(String name, List<String> options) {
        log.info("Saving HAProxy backend: " + name);
        save(Path.BACKEND, name, options);
    }

    public void saveFrontend(String name, List<String> options) {
        log.info("Saving HAProxy frontend: " + name);
        save(Path.FRONTEND, name, options);
    }

    public void saveListen(String name, List<String> options) {
        save(Path.LISTEN, name, options);
    }

    public void deleteBackend(String name) {
        log.info("Deleting HAProxy backend: " + name);
        delete(Path.BACKEND, name);
    }

    public void deleteFrontend(String name) {
        log.info("Deleting HAProxy frontend: " + name);
        delete(Path.FRONTEND, name);
    }

    public void deleteListen(String name) {
        delete(Path.LISTEN, name);
    }

    private void save(Path path, String name, List<String> options) {
        try {
            etcdClient.put(path.optionsPath(prefix, name), toJsonArray(options)).send().get();
        } catch (Exception e) {
            log.error("Can't save " + path.type + " " + name, e);
            throw new RuntimeException("Can't save " + path.type + " " + name, e);
        }
    }

    private void delete(Path path, String name) {
        try {
            etcdClient.delete(path.keyPath(prefix, name)).recursive().send().get();
        } catch (Exception e) {
            log.error("Can't delete " + path.type + " " + name, e);
            throw new RuntimeException("Can't delete " + path.type + " " + name, e);
        }
    }


    public static void main(String[] args) throws Exception {
        HAProxyConfigurator cfg = HAProxyConfigurator.create("/services/haproxy", "http://127.0.0.1:2379");

//        cfg.saveBackend("xxx", "option 1", "option 2");
//        cfg.deleteBackend("xxx");
    }

    private static String toJsonArray(List<String> strings) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(strings);
    }
}



