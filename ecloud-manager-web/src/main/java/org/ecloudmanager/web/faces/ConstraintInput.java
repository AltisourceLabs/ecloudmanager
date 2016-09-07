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

import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.ConstraintFieldSuggestion;
import org.ecloudmanager.deployment.core.ConstraintValue;
import org.ecloudmanager.deployment.core.DeploymentObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConstraintInput {
    private final DeploymentObject deploymentConstraint;
    private final ConstraintField field;
    private ConstraintValue value;
    private Option option;

    public ConstraintInput(DeploymentObject deploymentConstraint, ConstraintField field) {
        this.deploymentConstraint = deploymentConstraint;
        this.field = field;
        this.value = deploymentConstraint.getProvidedValue(getName());
        if (isUseDefaultValue()) {
            option = Option.DEFAULT;
        } else if (value != null && value.isReference()) {
            option = Option.REFERENCE;
        } else {
            option = Option.VALUE;
        }
    }

    public boolean isRequired() {
        return field.isRequired();
    }

    public boolean isReadOnly() {
        return field.isReadOnly();
    }

    public boolean isUseDefaultValue() {
        return hasDefaultValue() && getValue() == null;
    }

    public void setUseDefaultValue(boolean useDefaultValue) {
        if (useDefaultValue) {
            deploymentConstraint.removeValue(getName());
            value = null;
        } else {
            if (deploymentConstraint.getProvidedValue(getName()) == null) {
                setValue(getDefaultValue());
            }
        }
    }

    public boolean isMultiline() {
        return false; // Now there's no multiline fields any more
    }

    public String getName() {
        return field.getName();
    }

    public boolean hasDefaultValue() {
        String defaultValue = field.getDefaultValue();
        return defaultValue != null && !defaultValue.isEmpty();
    }

    public String getDefaultValue() {
        return field.getDefaultValue();
    }

    public String getValue() {
        if (value == null) {
            if (isReadOnly()) {
                return getDefaultValue();
            }
            return null;
        }
        return value.getValue();
    }

    public void setValue(String value) {
        if (this.value == null && (value == null || value.isEmpty())) {
            return;
        }
        this.value = ConstraintValue.value(value);
        deploymentConstraint.setValue(getName(), this.value);
    }

    public String getReference() {
        if (value == null) {
            return null;
        }
        return value.getReference();
    }

    public void setReference(String reference) {
        if (this.value == null && (reference == null || reference.isEmpty())) {
            return;
        }
        this.value = ConstraintValue.reference(reference);
        deploymentConstraint.setValue(getName(), this.value);
    }

    public String toString() {
        return field.toString();
    }

    public String getOption() {
        return option.name();
    }

    public void setOption(String option) {
        this.option = Option.valueOf(option);
        if (this.option == Option.DEFAULT) {
            setUseDefaultValue(true);
        } else {
            setUseDefaultValue(false);
        }
    }

    public String getUi() {
        if (option == Option.REFERENCE) {
            return "referenceConstraint";
        } else if (isMultiline()) {
            return "multilineConstraint";
        } else if (isReadOnly()) {
            return "readonlyConstraint";
        } else if (option == Option.DEFAULT) {
            return "defaultConstraint";
        } else if (field.getSuggestionsProvider() != null) {
            return "autocompleteConstraint";
        } else {
            return "simpleConstraint";
        }
    }

    public List<ConstraintFieldSuggestion> getSuggestions(String input) {
        if (field.getSuggestionsProvider() == null) {
            return Collections.emptyList();
        }
        return field.getSuggestionsProvider().getSuggestions(deploymentConstraint).stream()
            .filter(s -> s.getLabel().contains(input))
            .collect(Collectors.toList());
    }

    public enum Option {
        REFERENCE, VALUE, DEFAULT
    }
}
