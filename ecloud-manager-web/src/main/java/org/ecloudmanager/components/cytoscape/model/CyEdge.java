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

public class CyEdge extends CyElement {
    private static final long serialVersionUID = 2973606306957838977L;

    private CyArrowShape shape = CyArrowShape.TRIANGLE;
    private CyNode source;
    private CyNode target;
    private boolean directed = true;

    protected CyEdge(String id, String label, CyNode source, CyNode target) {
        super(id, label);
        this.source = source;
        this.target = target;
    }

    public CyArrowShape getShape() {
        return shape;
    }

    public void setShape(CyArrowShape shape) {
        this.shape = shape;
    }

    public CyNode getSource() {
        return source;
    }

    public void setSource(CyNode source) {
        this.source = source;
    }

    public CyNode getTarget() {
        return target;
    }

    public void setTarget(CyNode target) {
        this.target = target;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    @Override
    public String toJS(StringBuilder sb) {
        sb.append("{ group: 'edges'");
        sb.append(", data: { ");
        sb.append("id:'").append(getId()).append("'");
        sb.append(", label:'").append(getLabel()).append("'");
        sb.append(", shape:'").append(shape).append("'");
        sb.append(", source:'").append(source.getId()).append("'");
        sb.append(", target:'").append(target.getId()).append("'");
        sb.append(", color:'").append(getColor()).append("'");
        sb.append(", width: 1");
        sb.append("}");
        sb.append(", classes: '").append(getClasses()).append("'");
        sb.append("}");
        return sb.toString();
    }
}
