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

import org.ecloudmanager.components.cytoscape.model.CyModel;
import org.primefaces.context.RequestContext;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.SharedStringBuilder;
import org.primefaces.util.WidgetBuilder;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.Map;

public class CytoscapeRenderer extends CoreRenderer {
    private static final String SB_CYTOSCAPE = CoreRenderer.class.getName() + "#cytoscape";

    @Override
    public void decode(FacesContext context, UIComponent component) {
        decodeBehaviors(context, component);

        Map<String,String> params = context.getExternalContext().getRequestParameterMap();
        Cytoscape cytoscapeComponent = (Cytoscape) component;

        if(params.containsKey(cytoscapeComponent.getClientId(context) + "_synchModel")){
            processSynchAjaxRequest(context, cytoscapeComponent);
        }
    }

    private void processSynchAjaxRequest(FacesContext context, Cytoscape cytoscapeComponent) {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        CyModel model = (CyModel) cytoscapeComponent.getValue();
        if(requestContext != null && model != null) {
            StringBuilder sb = SharedStringBuilder.get(SB_CYTOSCAPE);
            sb.append("[");
            model.getNodes().stream().forEach(node -> {
                node.toJS(sb);
                sb.append(",");
            });
            sb.append("]");
            String synchData = sb.toString().replaceFirst(",]$", "]").replaceAll("'", "\"");
            sb.setLength(0);

            requestContext.addCallbackParam(cytoscapeComponent.getClientId(context) + "_synchModel", synchData);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Cytoscape cytoscape = (Cytoscape) component;
        encodeMarkup(context, cytoscape);
        encodeScript(context, cytoscape);
    }

    private void encodeMarkup(FacesContext context, Cytoscape cytoscape) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = cytoscape.getClientId(context);
        String style = cytoscape.getStyle();
        String styleClass = cytoscape.getStyleClass();
        styleClass = (styleClass == null) ? Cytoscape.CONTAINER_CLASS : Cytoscape.CONTAINER_CLASS + " " + styleClass;

        writer.startElement("div", cytoscape);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("class", styleClass, null);
        if(style != null) {
            writer.writeAttribute("style", style, null);
        }

        writer.endElement("div");
    }

    protected void encodeScript(FacesContext context, Cytoscape cytoscape) throws IOException {
        String clientId = cytoscape.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Cytoscape", cytoscape.resolveWidgetVar(), clientId, "cytoscape");

        CyModel model = (CyModel) cytoscape.getValue();
        if(model != null) {
            encodeModel(wb, model);
        }

        encodeClientBehaviors(context, cytoscape);

        wb.finish();
    }

    private void encodeModel(WidgetBuilder wb, CyModel model) throws IOException {
        StringBuilder sb = SharedStringBuilder.get(SB_CYTOSCAPE);

        if(model.getLayout() != null) wb.append(", layout:").append(model.getLayout());
        if(model.getStyle() != null) wb.append(", style:'").append(model.getStyle()).append("'");
        wb.append(", elements: [ ");
        model.getNodes().stream().forEach(node -> {
            node.toJS(sb);
            sb.append(",");
        });
        model.getEdges().stream().forEach(edge -> {
            edge.toJS(sb);
            sb.append(",");
        });
        String str = sb.toString().replaceFirst(",$", "");
        sb.setLength(0);
        wb.append(str);

        wb.append("]");

        wb.append(", wheelSensitivity : 0.1");
        wb.append(", minZoom: 0.25");
        wb.append(", maxZoom: 5");
        wb.append(", zoom: 1");
    }
}
