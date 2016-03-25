/*
 *  MIT License
 *
 *  Copyright (c) 2016  Altisource
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package org.ecloudmanager.components.cytoscape;

import com.google.common.collect.ImmutableSet;
import org.ecloudmanager.components.cytoscape.model.CyElement;
import org.ecloudmanager.components.cytoscape.model.CyModel;
import org.primefaces.component.api.Widget;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.util.Constants;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import java.util.Collection;
import java.util.Map;

@ResourceDependencies({
        @ResourceDependency(library="primefaces", name="jquery/jquery.js"),
        @ResourceDependency(library="primefaces", name="primefaces.js"),
        @ResourceDependency(library="primefaces", name="cytoscape/cytoscape.css"),
        @ResourceDependency(library="primefaces", name="cytoscape/dagre/dagre.js"),
        @ResourceDependency(library="primefaces", name="cytoscape/cytoscape.js"),
        @ResourceDependency(library="primefaces", name="cytoscape/cytoscape-dagre.js"),
        @ResourceDependency(library="primefaces", name="cytoscape/cytoscape-primefaces.js")
})
public class Cytoscape extends UIOutput implements ClientBehaviorHolder, Widget {
    public static final String COMPONENT_TYPE = "org.ecloudmanager.components.Cytoscape";
    public static final String COMPONENT_FAMILY = "org.ecloudmanager.components";
    private static final String DEFAULT_RENDERER = "org.ecloudmanager.components.CytoscapeRenderer";

    public static final String CONTAINER_CLASS = "ui-cytoscape ui-widget";

    protected enum PropertyKeys {
        widgetVar
        ,style
        ,styleClass;

        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {}

        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    public final static String CYTOSCAPE_AJAX_EVENT = "cytoscapeAjax";
    public final static String DEFAULT_EVENT = CYTOSCAPE_AJAX_EVENT;

    public final static String ITEM_SELECT_EVENT = "select";
    public final static String ITEM_UNSELECT_EVENT = "unselect";
    public final static String NODE_SELECT_EVENT = "selectNode";
    public final static String NODE_UNSELECT_EVENT = "unselectNode";
    public final static String EDGE_SELECT_EVENT = "selectEdge";
    public final static String EDGE_UNSELECT_EVENT = "unselectEdge";

    private static final Collection<String> SELECTION_EVENT_NAMES =
            ImmutableSet.of(ITEM_SELECT_EVENT, NODE_SELECT_EVENT, EDGE_SELECT_EVENT);
    private static final Collection<String> UNSELECTION_EVENT_NAMES =
            ImmutableSet.of(ITEM_UNSELECT_EVENT, NODE_UNSELECT_EVENT, EDGE_UNSELECT_EVENT);

    private static final Collection<String> EVENT_NAMES = ImmutableSet.of(
            CYTOSCAPE_AJAX_EVENT,
            ITEM_SELECT_EVENT, ITEM_UNSELECT_EVENT,
            NODE_SELECT_EVENT, NODE_UNSELECT_EVENT,
            EDGE_SELECT_EVENT, EDGE_UNSELECT_EVENT
    );


    public Cytoscape() {
        setRendererType(DEFAULT_RENDERER);
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public java.lang.String getWidgetVar() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.widgetVar, null);
    }
    public void setWidgetVar(java.lang.String _widgetVar) {
        getStateHelper().put(PropertyKeys.widgetVar, _widgetVar);
    }

    public java.lang.String getStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.style, null);
    }
    public void setStyle(java.lang.String _style) {
        getStateHelper().put(PropertyKeys.style, _style);
    }

    public java.lang.String getStyleClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass, null);
    }
    public void setStyleClass(java.lang.String _styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, _styleClass);
    }

    private boolean isRequestSource(FacesContext context) {
        return this.getClientId(context).equals(context.getExternalContext().getRequestParameterMap().get(Constants.RequestParams.PARTIAL_SOURCE_PARAM));
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT;
    }

    @Override
    public void queueEvent(FacesEvent event) {
        FacesContext context = getFacesContext();

        if(isRequestSource(context) && event instanceof AjaxBehaviorEvent) {
            Map<String,String> params = context.getExternalContext().getRequestParameterMap();
            String eventName = params.get(Constants.RequestParams.PARTIAL_BEHAVIOR_EVENT_PARAM);
            String clientId = this.getClientId(context);
            AjaxBehaviorEvent behaviorEvent = (AjaxBehaviorEvent) event;
            CyModel model = (CyModel) this.getValue();

            if(model != null) {
                if(SELECTION_EVENT_NAMES.contains(eventName)){
                    String itemId = params.get(clientId + "_itemId");

                    // Update model:
                    CyElement selectedElement = model.getElement(itemId);
                    selectedElement.setSelected(true);

                    //raise select event
                    SelectEvent selectEvent = new SelectEvent(this, behaviorEvent.getBehavior(), selectedElement);
                    selectEvent.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                    super.queueEvent(selectEvent);
                } else if(UNSELECTION_EVENT_NAMES.contains(eventName)){
                    String itemId = params.get(clientId + "_itemId");

                    // Update model:
                    CyElement unselectedElement = model.getElement(itemId);
                    unselectedElement.setSelected(false);

                    //raise select event
                    UnselectEvent unselectEvent = new UnselectEvent(this, behaviorEvent.getBehavior(), unselectedElement);
                    unselectEvent.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                    super.queueEvent(unselectEvent);
                }
            }
        } else {
            super.queueEvent(event);
        }
    }


    @Override
    public String resolveWidgetVar() {
        FacesContext context = getFacesContext();
        String userWidgetVar = (String) getAttributes().get("widgetVar");

        if(userWidgetVar != null)
            return userWidgetVar;
        else
            return "widget_" + getClientId(context).replaceAll("-|" + UINamingContainer.getSeparatorChar(context), "_");
    }
}
