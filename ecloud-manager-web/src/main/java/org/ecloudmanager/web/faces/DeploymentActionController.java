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

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.ecloudmanager.components.cytoscape.model.CyModel;
import org.ecloudmanager.components.cytoscape.model.CyNode;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.domain.LogEventEntity;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.DeploymentAttemptRepository;
import org.ecloudmanager.repository.deployment.DeploymentLogsRepository;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.ActionGroup;
import org.ecloudmanager.service.execution.SingleAction;
import org.jetbrains.annotations.NotNull;
import org.omnifaces.cdi.Param;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.Visibility;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.ecloudmanager.deployment.history.DeploymentAttempt.Type;

@Controller
public class DeploymentActionController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = 1219058684206564494L;

    private static final String GRAPH_STYLE =
            "node {" +
            "content: data(label);" +
            "shape: data(shape);" +
            "background-color: #FFFFFC;" +
//                    "background-color: data(color);" +
            "width: label;" +
            "height: label;" +
            "border-color: black;" +
            "border-width: 1;" +
            "text-halign: center;" +
            "text-valign: center;" +
            "padding-left: 10;" +
            "padding-right: 10;" +
            "padding-top: 4;" +
            "padding-bottom: 4;" +
            "font-size: 10;" +
            "compound-sizing-wrt-labels: include;" +
            "} " +

            ":parent {" +
            "text-halign: center;" +
            "text-valign: top;" +
            "width: data(size);" +
            "height: data(size);" +
            "padding-left: 4;" +
            "padding-right: 4;" +
            "padding-top: 4;" +
            "padding-bottom: 4;" +
            "background-color: #F7F7FA;" +
            "} " +

            ":selected {" +
            "border-color: yellow;" +
            "line-color: yellow;" +
            "border-width: 1;" +
            "source-arrow-color: #000;" +
            "target-arrow-color: #000;" +
            "} " +

            "edge {" +
            "content: data(label);" +
            "line-color: data(color);" +
            "target-arrow-color: data(color);" +
            "target-arrow-shape: data(shape);" +
            "width: data(width);" +
//            "curve-style: bezier;" +
//            "control-point-step-size: 40;" +
//            "curve-style: segments;" +
//            "segment-distances: 40;" +
//            "segment-weights: 0.5;" +
            "} " +

            "node.hidden {" +
            "display: none;" +
            "visibility: hidden;" +
            "} " +

            "node.labelHolder:active {" +
            "overlay-padding: 20;" +
            "} " +

            "node.labelHolder {" +
            "background-opacity: 0;" +
            "border-width: 0;" +
            "border-opacity: 0;" +
            "padding-top: 8;" +
            "} " +

            /////////////// Action statuses ///////////////
            // pending - render as default
            // rollback_running - now irrelevant

            "node.running {" +
            "background-color: #FFFF77;" +
            "} " +
            "node.running:parent {" +
            "background-color: #F7F7FA;" +
            "} " +

            "node.successful {" +
            "background-color: #5DFF5D;" +
            "} " +
            "node.successful:parent {" +
            "background-color: #4DEE4D;" +
            "} " +

            "node.not_run {" +
            "background-color: #FFFFFF;" +
            "} " +
            "node.not_run:parent {" +
            "background-color: #FFFFFF;" +
            "} " +

            "node.failed {" +
            "background-color: #FF9999;" +
            "} " +
            "node.failed:parent {" +
            "background-color: #FF8888;" +
            "}"
        /////////////////////////////////////////////////
        ;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "applicationDeploymentConverter")
    private ApplicationDeployment deployment;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private Type actionType;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "deploymentAttemptConverter")
    private DeploymentAttempt deploymentAttempt;

    @Inject
    private transient ApplicationDeploymentService applicationDeploymentService;

    @Inject
    private Logger log;

    @Inject
    private transient DeploymentAttemptRepository deploymentAttemptRepository;
    @Inject
    private transient DeploymentLogsRepository deploymentLogsRepository;

    private CyModel cyModel;

    private Action action;
    private HashMap<Action, CyNode> action2cyNode;

    private Set<ObjectId> expandedRows = new HashSet<>();

    @PostConstruct
    public void init() {
        if (deploymentAttempt == null) {
            DeploymentAttempt lastAttempt = deploymentAttemptRepository.findLastAttempt(deployment);
            if (actionType == Type.CREATE) {
                if (lastAttempt != null && lastAttempt.getType() != DeploymentAttempt.Type.DELETE) {
                    actionType = Type.UPDATE;
                }
            }

            switch (actionType) {
                case CREATE:
                    action = deployment.getDeployer().getCreateAction(deployment);
                    break;
                case UPDATE:
                    action = deployment.getDeployer().getUpdateAction(lastAttempt, (ApplicationDeployment)
                        lastAttempt.getDeployment(), deployment);
                    break;
                case DELETE:
                    action = deployment.getDeployer().getDeleteAction(deployment);
                    break;
            }
        } else {
            deployment = (ApplicationDeployment) deploymentAttempt.getDeployment();
            action = deploymentAttempt.getAction();
            action.restoreDependencies();
        }

        cyModel = generateGraphModel();
        loadActionStatuses();
    }

    public Deployable getDeployment() {
        return deployment;
    }

    public void setDeployment(ApplicationDeployment deployment) {
        this.deployment = deployment;
    }

    public Type getActionType() {
        return actionType;
    }

    public void setActionType(Type actionType) {
        this.actionType = actionType;
    }

    public DeploymentAttempt getDeploymentAttempt() {
        return deploymentAttempt;
    }

    public CyModel getGraphModel() {
        return cyModel;
    }

    @NotNull
    private CyModel generateGraphModel() {
        CyModel graphModel = new CyModel();

        graphModel.setStyle(GRAPH_STYLE);

        generateGraphModel(graphModel);

        //graphModel.setLayout("{name: 'breadthfirst', directed: true }");
        graphModel.setLayout("{name: 'dagre', rankDir: 'TB', nodeSep: 60, rankSep: 25, edgeSep: 3, padding: 8 }");

        return graphModel;
    }

    private void generateGraphModel(CyModel graphModel) {
        action2cyNode = new HashMap<>();
        generateActionNodes(graphModel, action, null, action2cyNode);
        generateDependencyEdges(graphModel, action, action2cyNode);
    }

    private void generateActionNodes(CyModel graphModel, Action action, CyNode parent, Map<Action, CyNode> action2node) {
        boolean hasChildren = action instanceof ActionGroup;
        CyNode node = graphModel.addNode(action.getId(), hasChildren ? "" : action.getLabel());
        CyNode node2 = node;
        if (hasChildren) {
            node2 = graphModel.addNode(action.getId() + "labelHolder", action.getLabel());
            node2.setClasses("labelHolder");
            node2.setParent(node);
        }
        final CyNode parentForChildren = node2;
        action2node.put(action, node);
        if (parent != null) {
            node.setParent(parent);
        }

        if (hasChildren) {
            ((ActionGroup) action).getActions()
                    .forEach(childAction -> generateActionNodes(graphModel, childAction, parentForChildren, action2node));
        }
    }

    private void generateDependencyEdges(CyModel graphModel, Action rootAction, Map<Action, CyNode> action2node) {
        rootAction.stream().forEach(action -> action.getDependencies(rootAction).stream()
                .forEach(dependency -> graphModel.addEdge(action2node.get(dependency), action2node.get(action))));
    }

    public boolean canExecuteAction() {
        return action.getStatus() == Action.Status.PENDING;
    }

    public void executeAction() {
        if (canExecuteAction()) {
            applicationDeploymentService.execute(deployment, action, actionType);
        }
    }

    public void loadActionStatuses() {
        action.stream().forEach(action -> {
            CyNode node = action2cyNode.get(action);
            setStatusClass(node, action.getStatus());
        });
    }

    private void setStatusClass(CyNode node, Action.Status status) {
        String statusClass = status.name().toLowerCase();
        HashSet<String> classes = Sets.newHashSet(node.getClasses().split(" "));
        if (classes.contains(statusClass)) {
            return;
        }
        HashSet<Action.Status> classesToRemove = Sets.newHashSet(Action.Status.values());
        classesToRemove.remove(status);
        classes.removeAll(classesToRemove.stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
        classes.add(statusClass);
        node.setClasses(StringUtils.join(classes, " ").trim());
    }

    public List<LogEventEntity> getLogs() {
        Action selectedAction = getSelectedAction();
        if (selectedAction != null) {
            Set<SingleAction> actions = selectedAction.stream()
                .flatMap(Action::stream)
                .filter(SingleAction.class::isInstance)
                .map(SingleAction.class::cast)
                .collect(Collectors.toSet());
            return deploymentLogsRepository.findForActions(actions);
        }
        return Collections.emptyList();
    }

    private Action getSelectedAction() {
        Collection<CyNode> selectedNodes = cyModel.getSelectedNodes();
        return action.stream()
            .filter((action) -> selectedNodes.contains(action2cyNode.get(action)))
            .findAny().orElseGet(() -> null);
    }

    public String getSelectedActionLabel() {
        Action selectedAction = getSelectedAction();
        return selectedAction == null ? "no action is selected" : selectedAction.getLabel();
    }

    public void onSelectNode(SelectEvent event) {
        expandedRows.clear();
    }

    public void onUnselectNode(UnselectEvent event) {
    }

    public void onRowToggle(ToggleEvent event) {
        if (event.getVisibility() == Visibility.VISIBLE) {
            expandedRows.add(((LogEventEntity) event.getData()).getId());
        } else {
            expandedRows.remove(((LogEventEntity) event.getData()).getId());
        }
    }

    public boolean isLogMessageExpanded(LogEventEntity logMessage) {
        return expandedRows.contains(logMessage.getId());
    }

}
