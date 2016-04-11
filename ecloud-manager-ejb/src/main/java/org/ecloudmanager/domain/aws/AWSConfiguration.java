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

package org.ecloudmanager.domain.aws;

import com.amazonaws.auth.AWSCredentials;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.domain.OwnedMongoObject;
import org.ecloudmanager.domain.RunlistHolder;
import org.ecloudmanager.jeecore.domain.MongoObject;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Entity(noClassnameStored = true)
public class AWSConfiguration extends OwnedMongoObject implements Serializable, AWSCredentials, RunlistHolder {

    private static final long serialVersionUID = 4201146027473993139L;
    private String name;
    private String awsAccessKeyId;
    private String awsSecretKey;
    @Reference(idOnly = true, ignoreMissing = true)
    private List<Recipe> runlist = new LinkedList<>();

    public AWSConfiguration() {
        name = "default AWS profile";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAWSAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAWSAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return awsSecretKey;
    }

    public void setAWSSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
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
}
