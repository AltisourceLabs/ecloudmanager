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

public class CyNode extends CyElement {
    private static final long serialVersionUID = -1761667179076065895L;

    private CyNode parent;
    private CyNodeShape shape = CyNodeShape.ROUNDRECTANGLE;

    public CyNode(String id, String label) {
        super(id, label);
    }

    public CyNode(String id, String label, CyNode parent) {
        this(id, label);
        this.parent = parent;
    }

    public CyNode getParent() {
        return parent;
    }

    public void setParent(CyNode parent) {
        this.parent = parent;
    }

    public CyNodeShape getShape() {
        return shape;
    }

    public void setShape(CyNodeShape shape) {
        this.shape = shape;
    }

    @Override
    public String toJS(StringBuilder sb) {
        sb.append("{ 'group': 'nodes'");
        sb.append(", 'data': { ");
        sb.append("'id':'").append(getId()).append("'");
        sb.append(", 'label':'").append(getLabel()).append("'");
        sb.append(", 'shape':'").append(shape).append("'");
        sb.append(", 'color':'").append(getColor()).append("'");
        sb.append(", 'size':").append(100);
        if (parent != null) {
            sb.append(", 'parent': '").append(parent.getId()).append("'");
        }
        sb.append("}");
        sb.append(", 'classes': '").append(getClasses()).append("'");
        sb.append("}");
        return sb.toString();
    }
}
