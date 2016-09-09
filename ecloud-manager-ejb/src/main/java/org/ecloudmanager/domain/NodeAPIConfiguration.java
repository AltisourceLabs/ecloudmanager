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

package org.ecloudmanager.domain;

import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.security.Encrypted;
import org.ecloudmanager.security.EncryptedStringConverter;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Entity(noClassnameStored = true)
@Converters(EncryptedStringConverter.class)
public class NodeAPIConfiguration extends OwnedMongoObject implements Serializable, RunlistHolder {

    private static final long serialVersionUID = 4201146027473993139L;
    private String name;
    private String credentialsKey;
    @Encrypted
    private String credentialsSecret;
    @Reference(idOnly = true, ignoreMissing = true)
    private List<Recipe> runlist = new LinkedList<>();
    private Type type;
    private String nodeBaseAPIClassName;
    private String remoteNodeAPIAddress;

    public NodeAPIConfiguration() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getNodeBaseAPIClassName() {
        return nodeBaseAPIClassName;
    }

    public void setNodeBaseAPIClassName(String nodeBaseAPIClassName) {
        this.nodeBaseAPIClassName = nodeBaseAPIClassName;
    }

    public String getRemoteNodeAPIAddress() {
        return remoteNodeAPIAddress;
    }

    public void setRemoteNodeAPIAddress(String remoteNodeAPIAddress) {
        this.remoteNodeAPIAddress = remoteNodeAPIAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredentialsKey() {
        return credentialsKey;
    }

    public void setCredentialsKey(String credentialsKey) {
        this.credentialsKey = credentialsKey;
    }

    public String getCredentialsSecret() {
        return credentialsSecret;
    }

    public void setCredentialsSecret(String credentialsSecret) {
        this.credentialsSecret = credentialsSecret;
    }

    @NotNull
    public List<Recipe> getRunlist() {
        return runlist;
    }

    public void setRunlist(List<Recipe> runlist) {
        this.runlist.clear();
        if (runlist != null) {
            this.runlist.addAll(runlist);
        }
    }

    public void addRecipe(Recipe recipe) {
        if (!runlist.contains(recipe)) {
            runlist.add(recipe);
        }
    }

    public enum Type {
        LOCAL("Embedded"),
        REMOTE("Remote");

        private String label;

        Type(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
