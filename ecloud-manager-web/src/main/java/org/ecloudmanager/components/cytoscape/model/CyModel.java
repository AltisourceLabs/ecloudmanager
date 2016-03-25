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

package org.ecloudmanager.components.cytoscape.model;

import com.google.common.collect.Iterables;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CyModel {
    private List<CyNode> nodes = new ArrayList<>();
    private List<CyEdge> edges = new ArrayList<>();

    private String layout = null;
    private String style = null;

    public CyNode addNode(String id, String label) {
        CyNode node = new CyNode(id, label);
        return addNode(node);
    }

    public CyNode addNode(CyNode node) {
        nodes.add(node);
        return node;
    }

    public CyEdge addEdge(CyNode source, CyNode target) {
        String id = "EDGE_" + source.getId() + "_" + target.getId();
        return addEdge(id, "", source, target);
    }

    private CyEdge addEdge(String id, String label, CyNode source, CyNode target) {
        CyEdge edge = new CyEdge(id, label, source, target);
        return addEdge(edge);
    }

    public CyEdge addEdge(CyEdge edge) {
        edges.add(edge);
        return edge;
    }

    public List<CyNode> getNodes() {
        return nodes;
    }

    public List<CyEdge> getEdges() {
        return edges;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public CyElement getElement(String itemId) {
        return Stream.concat(getNodes().stream(), getEdges().stream())
                .filter(e -> e.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    public Collection<CyNode> getSelectedNodes() {
        return getNodes().stream().filter(CyElement::isSelected).collect(Collectors.toSet());
    }
}
