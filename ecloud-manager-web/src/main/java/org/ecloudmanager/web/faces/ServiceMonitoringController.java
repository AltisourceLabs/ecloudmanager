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

package org.ecloudmanager.web.faces;

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.ConstraintValue;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.monitoring.HaproxyStatsData;
import org.ecloudmanager.monitoring.HaproxyStatsField;
import org.ecloudmanager.monitoring.HaproxyStatsService;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ServiceMonitoringController extends FacesSupport implements Serializable {
    private static Map<Integer, String> tabIndexToGroupName = new HashMap<>();
    static {
        tabIndexToGroupName.put(0, "STATUS");
        tabIndexToGroupName.put(1, "QUEUE");
        tabIndexToGroupName.put(2, "SESSIONS");
        tabIndexToGroupName.put(3, "RESPONSE");
        tabIndexToGroupName.put(4, "TRAFFIC");
        tabIndexToGroupName.put(5, "ERRORS");
        tabIndexToGroupName.put(6, "LATENCY");
    }

    @Inject
    ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    ApplicationDeploymentService applicationDeploymentService;
    @Inject
    HaproxyStatsService haproxyStatsService;

    private SortableDefaultTreeNode tree = null;
    private TreeNode selectedNode = null;

    private HaproxyStatsData haproxyStatsData;

    private LineChartModel chartModel;

    private String chartRange = "PT1H";

    private HaproxyStatsField chartField;

    private Integer activeTab;

    public static class ServiceMonitoringTreeNode extends SortableDefaultTreeNode {
        private boolean monitoringEnabled;

        public ServiceMonitoringTreeNode(Object data, TreeNode parent) {
            super(data, parent);
        }

        public boolean getMonitoringEnabled() {
            return monitoringEnabled;
        }

        public void setMonitoringEnabled(boolean monitoringEnabled) {
            this.monitoringEnabled = monitoringEnabled;
        }
    }

    @PostConstruct
    private void init() {
        createTree();
        createChartModel();
    }

    private void createChartModel() {
        chartModel = new LineChartModel();
        //chartModel.setZoom(true);
        chartModel.setShadow(false);
        DateAxis dateAxis = new DateAxis();
        dateAxis.setTickFormat("%b %#d, %H:%M:%S");
        //dateAxis.setTickAngle(-60);
        //dateAxis.setTickInterval("360");
        //dateAxis.setTickCount(10);
        chartModel.getAxes().put(AxisType.X, dateAxis);

//        LinearAxis yAxis = new LinearAxis();
//        chartModel.getAxes().put(AxisType.Y, yAxis);

        chartModel.setExtender("configureJqplot");

        LineChartSeries series = new LineChartSeries();
        series.setShowMarker(false);
        series.setLabel(" ");
        chartModel.addSeries(series);
    }

    private void createTree() {
        tree = new SortableDefaultTreeNode("root");

        applicationDeploymentRepository.getAll().forEach(applicationDeployment -> {
            if (applicationDeploymentService.isDeployed(applicationDeployment)) {
                addNode(applicationDeployment, tree);
            }
        });

        tree.sort();
    }

    private void addNode(DeploymentObject deployable, TreeNode parent) {
        if (
                deployable instanceof VMDeployment ||
                deployable instanceof ProducedServiceDeployment ||
                deployable instanceof ComponentGroupDeployment ||
                deployable instanceof ApplicationDeployment
        ) {
            ServiceMonitoringTreeNode node = new ServiceMonitoringTreeNode(deployable, parent);
            for (DeploymentObject d : deployable.children()) {
                if (!(d instanceof VMDeployment) || d.getParent() != d.getTop()) { // Exclude top-level VMDeployments
                    addNode(d, node);
                }
            }
            node.setExpanded(true);

            if (deployable instanceof ProducedServiceDeployment) {
                node.setMonitoringEnabled(isMonitoringEnabled(deployable));
            }

            if (deployable instanceof ApplicationDeployment) {
                node.setSelectable(false);
            }
        }
    }

    public void treeNodeSelected(NodeSelectEvent event) {
        selectedNode = event.getTreeNode();
        loadData();
    }

    public void loadData() {
        if (selectedNode != null && chartRange != null) {
            Duration duration = Period.parse(chartRange).toDurationTo(new DateTime());
            DateTime dateTime = new DateTime().minus(duration);
            Date startDate = dateTime.toDate();
            Axis dateAxis = chartModel.getAxes().get(AxisType.X);
            dateAxis.setMin(startDate.getTime());
            dateAxis.setMax(new Date().getTime());

            haproxyStatsData = haproxyStatsService.loadHaproxyStats(startDate, (DeploymentObject) selectedNode.getData(), chartField);

            if (chartField != null && haproxyStatsData != null) {
                ChartSeries series = chartModel.getSeries().get(0);
                series.setData(haproxyStatsData.getTimeSeriesData());

                if (series.getData().size() > 0) {
                    chartModel.setTitle("<b>" + chartField.getName() + "</b><br>" + chartField.getDescription());
                } else {
                    chartModel.setTitle(chartField.getDescription() + " (no data)");
                }
            } else {
                chartModel.setTitle(chartField == null ? "" : chartField.getDescription() + " (no data)");
                ChartSeries series = chartModel.getSeries().get(0);
                series.getData().clear();
            }
        }
    }

    public HaproxyStatsData getHaproxyStatsData() {
        return haproxyStatsData;
    }

    public List<Map.Entry<HaproxyStatsField, String>> getGroupData(String groupName) {
        HaproxyStatsField.Group group = HaproxyStatsField.Group.valueOf(groupName);

        if (haproxyStatsData == null) {
            return Collections.emptyList();
        }

        return haproxyStatsData.getLatestData().entrySet()
                .stream()
                .filter(e -> e.getKey().getGroup() == group)
                .collect(Collectors.toList());
    }

    public List<HaproxyStatsField> getChartFields(String groupName) {
        HaproxyStatsField.Group group = HaproxyStatsField.Group.valueOf(groupName);
        DeploymentObject deploymentObject = selectedNode == null ? null : (DeploymentObject) selectedNode.getData();
        Set<HaproxyStatsField> fields = HaproxyStatsField.getFields(group, deploymentObject);

        return fields.stream()
                .filter(HaproxyStatsField::isGraphSupported)
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isMonitoringEnabled(DeploymentObject deploymentObject) {
        String monitoringEnabled = deploymentObject.getConfigValue(HAProxyDeployer.HAPROXY_MONITORING);
        return monitoringEnabled != null && monitoringEnabled.equals("true");
    }

    public void monitoringChanged(TreeNode node) {
        ServiceMonitoringTreeNode monitoringNode = (ServiceMonitoringTreeNode) node;
        DeploymentObject deploymentObject = (DeploymentObject) node.getData();
        boolean monitored = isMonitoringEnabled(deploymentObject);
        if (monitored != monitoringNode.getMonitoringEnabled()) {
            String value = monitoringNode.getMonitoringEnabled() ? "true" : "false";
            deploymentObject.setValue(HAProxyDeployer.HAPROXY_MONITORING, ConstraintValue.value(value));
            applicationDeploymentService.save((ApplicationDeployment) deploymentObject.getTop());
            addMessage(new FacesMessage("Monitoring setting changed to '" + value + "' for service " + deploymentObject.getName()));
        }
    }

    public void onTabChange(TabChangeEvent event) {
        String group = tabIndexToGroupName.get(activeTab);
        List<HaproxyStatsField> chartFields = getChartFields(group);
        if (chartFields.size() > 0) {
            chartField = chartFields.get(0);
        }
        loadData();
    }

    public TreeNode getTree() {
        return tree;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public LineChartModel getChartModel() {
        return chartModel;
    }

    public String getChartRange() {
        return chartRange;
    }

    public void setChartRange(String chartRange) {
        if (!StringUtils.isEmpty(chartRange)) {
            this.chartRange = chartRange;
        }
    }

    public HaproxyStatsField getChartField() {
        return chartField;
    }

    public void setChartField(HaproxyStatsField chartField) {
        if (chartField != null) {
            this.chartField = chartField;
        }
    }

    public Integer getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(Integer activeTab) {
        this.activeTab = activeTab;
    }
}
