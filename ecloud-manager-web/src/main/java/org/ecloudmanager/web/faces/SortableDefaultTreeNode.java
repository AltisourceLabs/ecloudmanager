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

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.Collections;

class SortableDefaultTreeNode extends DefaultTreeNode {
    private static final long serialVersionUID = 1148053228529718801L;

    public SortableDefaultTreeNode(Object data) {
        super(data);
    }

    public SortableDefaultTreeNode(Object data, TreeNode parent) {
        super(data, parent);
    }

    public void sort() {
        Collections.sort(this.getChildren(), (n1, n2) -> n1.getData().toString().compareTo(n2.getData().toString
            ()));
        for (TreeNode child : getChildren()) {
            ((SortableDefaultTreeNode) child).sort();
        }
    }
}
