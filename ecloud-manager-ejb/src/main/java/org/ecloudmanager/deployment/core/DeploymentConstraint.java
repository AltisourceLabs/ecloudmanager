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


import org.ecloudmanager.jeecore.domain.MongoObject;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Embedded;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DeploymentConstraint extends MongoObject {
    private static final long serialVersionUID = 7123310787534731308L;
    // The fields may be modified concurrently by the actions executed on different threads
    // Not 100% sure that it is absolutely needed, but just in case...
    // CopyOnWriteArrayList throws NPE when creating deployment, TODO investigate this issue
    private final List<ConstraintField> fields = Collections.synchronizedList(new ArrayList<>());
    @Embedded
    private final Map<String, ConstraintValue> values = new HashMap<>();


    public DeploymentConstraint() {
    }

    public static String getPrefix(DeploymentObject deploymentObject) {
        DeploymentObject parent = deploymentObject.getParent();
        if (parent == null) {
            return DeploymentObject.PATH_SEPARATOR;
        }
        return getPrefix(parent) + deploymentObject.getName() + DeploymentObject.PATH_SEPARATOR;
    }


    public boolean satisfied() {
        for (ConstraintField f : fields) {
            if (f.isRequired() && getValue(f.getName()) == null) {
                return false;
            }
        }
        return true;
    }

    public List<ConstraintField> getConstraintFields() {
        return fields;
    }

    public ConstraintField getConstraintField(String name) {
        for (ConstraintField f : fields) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public void addField(String name, String description) {
        if (getConstraintField(name) == null) {
            fields.add(ConstraintField.builder().name(name).description(description).build());
        }
    }

    public void addOptionalField(String name, String description) {
        if (getConstraintField(name) == null) {
            fields.add(ConstraintField.builder().name(name).description(description).required(false).build());
        }
    }

    public void addField(ConstraintField field) {
        if (getConstraintField(field.getName()) == null) {
            fields.add(field);
        }
    }

    public void removeField(String name) {
        ConstraintField field = getConstraintField(name);
        if (field != null) {
            fields.remove(field);
            removeValue(name);
        }
    }

    public void clear() {
        fields.clear();
        values.clear();
    }

    protected void addFields(List<ConstraintField> fieldList) {
        fields.addAll(fieldList);
    }

    public ConstraintValue getValue(String name) {
        ConstraintValue value = values.get(name);
        if (value != null) {
            return value;
        }
        ConstraintField field = getConstraintField(name);
        if (field != null && field.getDefaultValue() != null) {
            return ConstraintValue.value(field.getDefaultValue());
        }
        return null;
    }

    public ConstraintValue getProvidedValue(String name) {
        return values.get(name);
    }

    public void setValue(String name, ConstraintValue value) {
        values.put(name, value);
    }

    public void removeValue(String name) {
        values.remove(name);
    }

    public @NotNull DeploymentConstraint copyConfig(DeploymentConstraint copy) {
        copy.clear();
        copy.fields.addAll(fields.stream().map(ConstraintField::copy).collect(Collectors.toList()));
        copy.values.putAll(values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
            .copy())));
        copy.setId(getId());
        return copy;
    }


}
