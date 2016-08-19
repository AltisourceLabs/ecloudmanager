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

package org.ecloudmanager.service;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.domain.NodeAPIConfiguration;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.node.LocalNodeAPI;
import org.ecloudmanager.node.NodeAPI;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.node.rest.RestNodeAPI;
import org.ecloudmanager.repository.NodeAPIConfigurationRepository;
import org.picketlink.Identity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class NodeAPIConfigurationService extends ServiceSupport {
    @Inject
    Identity identity;
    @Inject
    private Logger log;
    @Inject
    private NodeAPIConfigurationRepository repository;

    public void saveOrUpdate(NodeAPIConfiguration configuration) {
        log.info("Saving NodeAPI Configuration " + configuration.getName());
        configuration.setOwner(identity.getAccount().getId());
        super.saveOrUpdate(configuration);
        fireEvent(configuration);
    }

    public void remove(NodeAPIConfiguration configuration) {
        log.info("Deleting NodeAPI Configuration " + configuration.getName());
        delete(configuration);
        fireEvent(configuration);
    }

    public NodeAPIConfiguration getConfiguration(String name) {
        return repository.getAllForUser(identity.getAccount().getId()).stream().filter(c -> name.equals(c.getName())).findAny().orElse(null);
    }

    public NodeAPI getAPI(String name) {
        NodeAPIConfiguration cfg = getConfiguration(name);
        if (cfg == null) {
            return null;
        }
        switch (cfg.getType()) {
            case LOCAL:
                try {
                    Class<? extends NodeBaseAPI> clazz = (Class<? extends NodeBaseAPI>) Class.forName(cfg.getNodeBaseAPIClassName());
                    NodeBaseAPI baseAPI = clazz.newInstance();
                    return new LocalNodeAPI(baseAPI);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            case REMOTE:
                return new RestNodeAPI(cfg.getRemoteNodeAPIAddress());
        }
        return null;
    }

    public SecretKey getCredentials(String name) {
        NodeAPIConfiguration cfg = getConfiguration(name);
        if (cfg == null) {
            return null;
        }
        return new SecretKey(cfg.getCredentialsKey(), cfg.getCredentialsSecret());
    }

    public List<Recipe> getRunlist(String name) {
        NodeAPIConfiguration cfg = getConfiguration(name);
        if (cfg == null) {
            return null;
        }
        return cfg.getRunlist();
    }

    public List<String> getAPIs() {
        return repository.getAllForUser(identity.getAccount().getId()).stream().map(c -> c.getName()).collect(Collectors.toList());
    }
}