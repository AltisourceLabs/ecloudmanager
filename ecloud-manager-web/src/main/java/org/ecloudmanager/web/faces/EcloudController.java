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

import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.tmrk.cloudapi.model.EnvironmentType;
import org.ecloudmanager.tmrk.cloudapi.model.EnvironmentsType;
import org.ecloudmanager.tmrk.cloudapi.model.OrganizationType;
import org.ecloudmanager.tmrk.cloudapi.model.OrganizationsType;
import org.ecloudmanager.tmrk.cloudapi.service.environment.EnvironmentService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author irosu
 */
@Controller
public class EcloudController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = -6573735320071911299L;

    private MindmapNode root;

    private MindmapNode selectedNode;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private EnvironmentService environmentService;

    @PostConstruct
    protected void init() {
        root = new DefaultMindmapNode("ecloud", "ecloud", "FFCC00", false);

        MindmapNode organizations = new DefaultMindmapNode("Organizations", "organizations", "6e9ebf", true);

        root.addNode(organizations);
    }

    public MindmapNode getRoot() {
        return root;
    }

    public MindmapNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(MindmapNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void onNodeSelect(SelectEvent event) {
        MindmapNode selectedNode = (MindmapNode) event.getObject();
        //populate if not already loaded
        if (selectedNode.getChildren().isEmpty()) {
            Object label = selectedNode.getLabel();

            if (selectedNode.getData() instanceof OrganizationType) {
                String href = ((OrganizationType) selectedNode.getData()).getHref();
                String orgId = href.substring(href.lastIndexOf("/") + 1, href.length());
                EnvironmentsType environments = environmentService.getEnvironments(orgId);
                List<EnvironmentType> environmentsList = environments.getEnvironment();
                for (EnvironmentType envType : environmentsList) {
                    selectedNode.addNode(new DefaultMindmapNode(envType.getName(), envType, "6e9ebf", true));
                }
            }
            if (label.equals("Organizations")) {
                OrganizationsType organizations = organizationService.getOrganizations();

                List<OrganizationType> organizationsType = organizations.getOrganization();
                for (OrganizationType org : organizationsType) {
                    selectedNode.addNode(new DefaultMindmapNode(org.getName(), org, "6e9ebf", true));
                }
            }
        }
    }

    public void onNodeDblselect(SelectEvent event) {
        this.selectedNode = (MindmapNode) event.getObject();
    }

    public void importVm() {
        navigate(null, "importVmFromCatalog.jsf?faces-redirect=true");
    }
}
