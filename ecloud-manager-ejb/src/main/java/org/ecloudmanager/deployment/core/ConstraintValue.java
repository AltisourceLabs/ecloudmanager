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

package org.ecloudmanager.deployment.core;

public class ConstraintValue {
    private String value;
    private String reference;

    private ConstraintValue() {
    }

    private ConstraintValue(String value, String reference) {
        this.value = value;
        this.reference = reference;
    }

    public static ConstraintValue value(String value) {
        return new ConstraintValue(value, null);
    }

    public static ConstraintValue reference(String reference) {
        return new ConstraintValue(null, reference);
    }

    public String getReference() {
        return reference;
    }

    public boolean isReference() {
        return reference != null;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return isReference() ? "[" + getReference() + "]" : value;
    }

    public ConstraintValue copy() {
        return new ConstraintValue(this.value, this.reference);
    }
}

