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

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.EntityListeners;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The base class for Deployment stage including templates and constraints specification
 */
@EntityListeners(DeployableEntityListener.class)
public abstract class DeploymentObject extends DeploymentConstraint {
    public static final String PATH_SEPARATOR = "/";
    public static final String PARENT_REFERENCE = ".." + PATH_SEPARATOR;

    private String name;
    private String description = "";

    private String pathToExt;

    @Transient
    private DeploymentObject parent;

    private List<DeploymentObject> children = new ArrayList<>();


    public DeploymentObject() {
    }

    private static @Nullable StringBuilder buildRelativePath(StringBuilder stringBuilder, @NotNull DeploymentObject
            from, @NotNull DeploymentObject to) {
        if (from.equals(to)) {
            return stringBuilder;
        }
        if (from.contains(to)) {
            DeploymentObject child = from.children().stream().filter(c -> c.equals(to)).findFirst().orElse(null);
            if (child != null) {
                return stringBuilder.append(child.getName()).append(PATH_SEPARATOR);
            }
            DeploymentObject containingChild = from.children().stream().filter(c -> c.contains(to)).findFirst()
                    .orElse(null);
            if (containingChild != null) {
                return buildRelativePath(stringBuilder.append(containingChild.getName()).append(PATH_SEPARATOR),
                        containingChild, to);
            }
            // should not happen
            throw new RuntimeException();
        }
        if (from.getParent() == null) {
            return null;
        }
        return buildRelativePath(stringBuilder.append(PARENT_REFERENCE), from.getParent(), to);

    }

    public DeploymentObject getParent() {
        return parent;
    }

    @Nullable
    public static <T extends DeploymentObject> T  getParentOfType(@Nullable DeploymentObject element, @NotNull Class<T> aClass, boolean strict) {
        if (element == null) return null;

        if (strict) {
            element = element.getParent();
        }

        while (element != null) {
            if (aClass.isInstance(element)) {
                //noinspection unchecked
                return (T)element;
            }

            if (element instanceof ApplicationDeployment) return null;
            element = element.getParent();
        }

        return null;
    }

    public void setParent(DeploymentObject parent) {
        this.parent = parent;
    }

    public void addChild(DeploymentObject child) {
        child.setParent(this);
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public List<DeploymentObject> children() {
        return children;
    }

    @NotNull
    public DeploymentObject getTop() {
        return getParent() == null ? this : getParent().getTop();
    }

    public Stream<DeploymentObject> stream() {
        return Stream.concat(
            Stream.of(this),
            children().stream().flatMap(DeploymentObject::stream));
    }

    public <T extends DeploymentObject> Stream<T> stream(Class<T> type) {
        return stream().filter(type::isInstance).map(type::cast);
    }

    public <T extends DeploymentObject> List<T> children(Class<T> type) {
        return children().stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }

    @Nullable
    public DeploymentObject getChildByName(String name) {
        return children().stream().filter(s -> name.equals(s.getName())).findFirst().orElse(null);
    }

    @NotNull
    public DeploymentObject createIfMissingAndGetConfig(String name) {
        return createIfMissingAndGetConfig(name, "");
    }

    @NotNull
    public DeploymentObject createIfMissingAndGetConfig(String name, String description) {
        DeploymentObject result = getChildByName(name);
        if (result != null) {
            return result;
        }
        result = new Config(name, description);
        addChild(result);
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + getName() + "]" +
            ((getDescription() == null) ? "" : " " + getDescription());
    }

    public String getConfigValue(String constraintName) {
        if (constraintName.startsWith(PARENT_REFERENCE)) {
            return getParent().getConfigValue(constraintName.substring(PARENT_REFERENCE.length()));
        }
        if (constraintName.startsWith(PATH_SEPARATOR)) {
            return getTop().getConfigValue(constraintName.substring(PATH_SEPARATOR.length()));
        }

        ConstraintValue value = getValue(constraintName);
        if (value != null) {
            if (value.isReference()) {

                if (value.getReference().startsWith(PATH_SEPARATOR)) {
                    return getTop().getConfigValue(value.getReference().substring(PATH_SEPARATOR.length()));
                } else {
                    return getConfigValue(value.getReference());
                }
            } else {
                return value.getValue();
            }
        } else {
            for (DeploymentObject d : children()) {
                String prefix = d.getName() + PATH_SEPARATOR;
                if (constraintName.startsWith(prefix)) {
                    return d.getConfigValue(constraintName.substring(prefix.length()));
                }
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getConfigValues() {
        Map<String, String> config = new HashMap<>();
        getConstraintFields().forEach(f -> {
            config.put(f.getName(), getConfigValue(f.getName()));
        });
        DeploymentObject extended = getExtendedConfig();
        if (extended != null) {
            extended.getConstraintFields().forEach(f -> {
                config.put(f.getName(), getConfigValue(f.getName()));
            });
        }
        return config;
    }

    public String relativePathTo(@Nullable DeploymentObject to) {
        if (to == null) {
            return null;
        }
        StringBuilder result = buildRelativePath(new StringBuilder(), this, to);
        return result == null ? null : result.toString();
    }

    private boolean contains(DeploymentObject child) {
        return stream().anyMatch(c -> c.equals(child));
    }

    public ConstraintValue getValue(String name) {
        ConstraintValue value = super.getValue(name);
        return value != null ? value : getExtendedConfig() != null ? getExtendedConfig().getValue(name) : null;
    }

    public DeploymentObject getExtendedConfig() {
        if (pathToExt == null) {
            return null;
        }
        DeploymentObject result = getByPath(pathToExt);
        if (result == this) {
            return null;
        }
        return result;
    }

    public void setExtendedConfig(DeploymentObject ext) {
        pathToExt = relativePathTo(ext);
    }

    @Nullable
    public DeploymentObject getByPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.isEmpty()) {
            return this;
        }
        if (path.startsWith(PARENT_REFERENCE)) {
            return getParent().getByPath(path.substring(PARENT_REFERENCE.length()));
        }
        if (path.startsWith(PATH_SEPARATOR)) {
            return getTop().getByPath(path.substring(PATH_SEPARATOR.length()));
        }
        for (DeploymentObject d : children()) {
            String prefix = d.getName() + PATH_SEPARATOR;
            if (path.startsWith(prefix)) {
                return d.getByPath(path.substring(prefix.length()));
            }
        }

        return null;
    }

    public String getPath(String separator) {
        if (getTop().equals(this)) {
            return "";
        }
        String parentPath = getParent().getPath(separator);

        return parentPath + (parentPath.isEmpty() ? "" : separator) + getName();
    }
//    public List<DeploymentObject> getDependencies() {
//
//    }

}
