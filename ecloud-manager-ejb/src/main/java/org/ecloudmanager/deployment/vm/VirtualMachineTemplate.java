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

package org.ecloudmanager.deployment.vm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.util.ClonerProducer;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"version", "new"})
public class VirtualMachineTemplate extends DeploymentObject implements Serializable, Template<VMDeployment> {

    private static final long serialVersionUID = 8513536191362885293L;
    //    Map<String, App> dependencies = new HashMap<>();
    private int processorCount = 1;
    private int memory = 1;
    private int storage = 20;

    private List<ObjectId> runlist = new LinkedList<>();

    public VirtualMachineTemplate() {
    }

    public int getProcessorCount() {
        return processorCount;
    }

    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    public int getMemory() {
        return memory;
    }

//    public List<VirtualMachineTemplate> getChildren() {
//        return children;
//    }
//
//    public void setChildren(List<VirtualMachineTemplate> children) {
//        this.children = children;
//    }
//
//    @OneToMany(mappedBy = "from", fetch = FetchType.EAGER)
//    private List<VirtualMachineTemplate> children;

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    @NotNull
    public List<Recipe> getRunlist() {
        RecipeRepository recipeRepository = CDI.current().select(RecipeRepository.class).get();
        DeploymentObject top = getTop();
        return runlist.stream()
                .map(id -> recipeRepository.get(id, top instanceof ApplicationDeployment ? (ApplicationDeployment) top : null))
                .collect(Collectors.toList());
    }

    public void setRunlist(List<Recipe> runlist) {
        this.runlist.clear();
        if (runlist != null) {
            this.runlist.addAll(runlist.stream().map(Recipe::getId).collect(Collectors.toList()));
        }
    }

    public void addRecipe(Recipe recipe) {
        if (!runlist.contains(recipe.getId())) {
            runlist.add(recipe.getId());
        }
    }

    public void removeRecipe(Recipe recipe) {
        runlist.remove(recipe.getId());
    }

    public String toString() {
        return getName();
    }

//    @Override
//    protected Collection<String> getExcludeFieldNames() {
//        return Arrays.asList("storage", "from", "runlist", "children");
//    }

    @NotNull
    @Override
    public List<Endpoint> getEndpoints() {
        Set<Endpoint> result = new LinkedHashSet<>();
        for (Recipe r : getRunlist()) {
            result.addAll(r.getEndpoints());
        }
        return new ArrayList<>(result);
    }

    @NotNull
    @Override
    public List<String> getRequiredEndpoints() {
        return getRunlist().stream().
            flatMap(r -> r.getRequiredEndpoints().stream()).
            distinct().collect(Collectors.toList());
    }

//    void initDependencies() {
//        List<Endpoint> produced = getEndpoints();
//        for (Recipe r : getRunlist()) {
//            for (String s : r.getRequiredEndpoints()) {
//                if (produced.contains(s)) {
//                    dependencies.put(s, this);
//                } else {
//                    dependencies.put(s, null);
//                }
//            }
//        }
//
//    }

    @NotNull
    @Override
    public VMDeployment toDeployment() {
        VMDeployment d = new VMDeployment();
        d.setVirtualMachineTemplate(new ClonerProducer().produceCloner().deepClone(this));
        d.setName(getName());
        return d;
    }
}
