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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.deployment.core.*;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.deployment.DeploymentAttemptRepository;
import org.ecloudmanager.repository.template.ApplicationTemplateRepository;
import org.ecloudmanager.service.chef.ChefGenerationService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.deployment.GatewayService;
import org.omnifaces.cdi.Param;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ApplicationDeploymentController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = -4809521504415887873L;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "topLevelDeployableConverter")
    private ApplicationDeployment deployment;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "applicationTemplateConverter")
    private ApplicationTemplate template;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private String infrastructure;

    // Parameters for a gateway deployment
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "vmTemplateConverter")
    private VirtualMachineTemplate vmTemplate;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private String gatewayName;

    @Inject
    private Logger log;

    @Inject
    private transient ApplicationDeploymentService applicationDeploymentService;
    @Inject
    private transient GatewayService gatewayService;
    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private transient ApplicationTemplateRepository applicationTemplateRepository;
    @Inject
    private transient DeploymentAttemptRepository deploymentAttemptRepository;

    @Inject
    private transient ChefGenerationService chefGenerationService;

    private TreeNode selectedNode;

    private List<SelectItem> selectItems;

    public List<ConstraintInput> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintInput> constraints) {
        this.constraints = constraints;
    }

    private List<ConstraintInput> constraints = new ArrayList<>();

    private TreeNode tree = null;

    private String jsonString;

    @PostConstruct
    public void init() {
        if (template != null) {
            deployment = applicationDeploymentService.create(template, infrastructure);
        } else if (gatewayName != null) {
            deployment = gatewayService.create(gatewayName, vmTemplate, infrastructure);
        }
    }

    public Deployable getDeployment() {
        return deployment;
    }

    public TreeNode getTree() {
        if (tree == null) {
            tree = initTree();
        }
        return tree;
    }

    private TreeNode initTree() {
        SortableDefaultTreeNode root = new SortableDefaultTreeNode("root");

        if (deployment == null) {
            return root;
        }
        getTree(deployment, root);
        root.sort();

//        new DefaultTreeNode(deployment, root);
//
//        TreeNode consumes = new DefaultTreeNode("Consumed Services", root);
//        consumes.setExpanded(true);
//        for (ExternalServiceDeployment es : deployment.getConsumes()) {
//            new DefaultTreeNode(es, consumes);
//        }
//        TreeNode produces = new DefaultTreeNode("Produces Services", root);
//        produces.setExpanded(true);
//        for (ProducedServiceDeployment es : deployment.getProvides()) {
//            TreeNode srv = new DefaultTreeNode(es, produces);
//            srv.setExpanded(true);
//            for (ComponentGroupDeployment cg : es.getComponentGroups()) {
//                new DefaultTreeNode(cg, srv);
//            }
//        }
        return root;
    }

    private TreeNode getTree(DeploymentObject deployable, TreeNode parent) {
        if (deployable instanceof Config && deployable.children().isEmpty() && deployable.getConstraintFields()
            .isEmpty()) {
            return null;
        }
        TreeNode node = new SortableDefaultTreeNode(deployable, parent);
        for (DeploymentObject d : deployable.children()) {
            getTree(d, node);
        }
        node.setExpanded(true);
        return node;
    }

    public List<SelectItem> getConstraintList() {
        if (selectItems == null) {
            selectItems = initConstraintList();
        }
        return selectItems;
    }


    private SelectItemGroup createGroup(String name, String prefix, DeploymentConstraint constraint) {
        SelectItemGroup group = new SelectItemGroup(name);
        List<SelectItem> items = new ArrayList<>();
        constraint.getConstraintFields().stream()
            .filter(ConstraintField::isAllowReference)
            .forEach(field -> items.add(new SelectItem(prefix + field.getName(), prefix + field.getName())));
        group.setSelectItems(items.toArray(new SelectItem[items.size()]));
        return group;
    }

    private List<SelectItem> initConstraintList() {
        List<SelectItem> result = new ArrayList<>();
        deployment.stream()
            .filter(deploymentObject -> deploymentObject.getConstraintFields().size() > 0)
            .forEach(deploymentObject -> {
                result.add(createGroup(deploymentObject.getName(), DeploymentConstraint.getPrefix
                    (deploymentObject), deploymentObject));
            });
        return result;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        constraints.clear();
        Object data = event.getTreeNode().getData();
        if (data instanceof DeploymentObject) {
            DeploymentObject obj = (DeploymentObject) data;
            for (ConstraintField f : obj.getConstraintFields()) {
                constraints.add(new ConstraintInput(obj, f));
            }
        }
    }

    public void save() {
        applicationDeploymentService.update(deployment);
    }

    public void generateChefEnv(ActionEvent ev) {
        StringWriter sw = new StringWriter();
        if (selectedNode != null && selectedNode.getData() instanceof VMDeployment) {
            VMDeployment vmDeployment = (VMDeployment) selectedNode.getData();
            chefGenerationService.generateChefNodeEnv(sw, vmDeployment);
        } else {
            chefGenerationService.generateChefEnv(sw, ChefEnvironmentDeployer.getChefEnvironment(deployment));
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Object o = mapper.readValue(sw.toString(), Object.class);
            jsonString = mapper.writeValueAsString(o);
        } catch (IOException e) {
            jsonString = e.toString();
        }
        //jsonString = sw.toString();
    }

    public String saveAndRedirectToAction() {
        save();
        return "deploymentAction?faces-redirect=true&includeViewParams=true";
    }

    public boolean canDeploy() {
        return deployment != null && deployment.stream().allMatch(d -> d.satisfied());
    }

    public boolean canUndeploy() {
        if (deployment != null) {
            DeploymentAttempt lastAttempt = deploymentAttemptRepository.findLastAttempt(deployment);
            return lastAttempt != null &&
                (lastAttempt.getType() == DeploymentAttempt.Type.CREATE || lastAttempt.getType() == DeploymentAttempt
                    .Type.UPDATE);
        }
        return false;
    }

    public void scaleComponentGroups() {
        deployment.stream(ComponentGroupDeployment.class).collect(Collectors.toList()).forEach
            (ComponentGroupDeployment::scale);
        deployment.specifyConstraints();
        // rebuild the tree
        tree = null;
    }

    public String getJsonString() {
        return jsonString;
    }
}
