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

package org.ecloudmanager.web.faces;

import com.google.common.base.Strings;
import org.ecloudmanager.domain.NodeAPIConfiguration;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.node.model.APIInfo;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.repository.NodeAPIConfigurationRepository;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.picketlink.Identity;
import org.primefaces.event.CloseEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@Named("nodeAPIConfigurationController")
public class NodeAPIConfigurationController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -189251095657463746L;

    @Inject
    private transient NodeAPIConfigurationService service;
    @Inject
    private transient NodeAPIConfigurationRepository repository;
    @Inject
    private transient Identity identity;
    private List<NodeAPIConfiguration> configurations;
    private Map<String, APIInfo> availableAPIs;
    private NodeAPIConfiguration configuration = new NodeAPIConfiguration();

    public List<NodeAPIConfiguration.Type> getTypes() {
        return Arrays.asList(NodeAPIConfiguration.Type.values());
    }

    public String getDescription(NodeAPIConfiguration cfg) {
        switch (cfg.getType()) {
            case LOCAL:
                String className = cfg.getNodeBaseAPIClassName();
                if (Strings.isNullOrEmpty(className)) {
                    return "not defined";
                }
                if (!getAvailableAPIs().containsKey(className)) {
                    return "Class not found: " + className;
                }
                APIInfo info = getAvailableAPIs().get(className);
                return info.getDescription() + " [" + info.getId() + "]";
            case REMOTE:
                return "Address: " + cfg.getRemoteNodeAPIAddress();
        }
        return "";
    }

    public Map<String, APIInfo> getAvailableAPIs() {
        if (availableAPIs == null || availableAPIs.isEmpty()) {
            availableAPIs = NodeUtil.getAvailableAPIs();
        }
        return availableAPIs;
    }

    @PostConstruct
    private void init() {
        refresh();
    }

    public List<NodeAPIConfiguration> getConfigurations() {
        return configurations;
    }

    public void delete(NodeAPIConfiguration configuration) {
        repository.delete(configuration);
        refresh();
    }

    public void save() {
        service.saveOrUpdate(configuration);
        refresh();
    }

    private void refresh() {
        configurations = repository.getAllForUser(identity.getAccount().getId());
    }

    public NodeAPIConfiguration getConfiguration() {
        return configuration;
    }

    public void startEdit(NodeAPIConfiguration configuration) {
        this.configuration = configuration;
    }

    public void createConfiguration() {
        this.configuration = new NodeAPIConfiguration();
    }

    public void handleClose(CloseEvent event) {

    }

}
