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

import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.ObjectRef;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.mongodb.morphia.Datastore;
import org.omnifaces.el.functions.Arrays;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ImportDeployableController extends FacesSupport implements Serializable{
    public static class ImportDeployableDialogResult {
        private DeploymentObject object;
        private Boolean includeConstraints;
        private String name;

        public ImportDeployableDialogResult(DeploymentObject object, String name, Boolean includeConstraints) {
            this.object = object;
            this.includeConstraints = includeConstraints;
            this.name = name;
        }

        public DeploymentObject getObject() {
            return object;
        }

        public Boolean getIncludeConstraints() {
            return includeConstraints;
        }

        public String getName() {
            return name;
        }
    }

    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private transient Datastore datastore;

    private ObjectRef selectedDeployable;
    private List<ObjectRef> deployables = new ArrayList<>();

    private Boolean includeConstraints;
    private String name;

    @PostConstruct
    public void init() {
        Map<String, String[]> parameterValuesMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
        String[] classNames = parameterValuesMap.get("classes");
        Set<Class> classes = Stream.of(classNames).map(n -> {
            try {
                return Class.forName(n);
            } catch (ClassNotFoundException e) {
                addMessage(new FacesMessage("Unexpected error: " + e.getMessage()));
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        boolean recursive = Arrays.contains(parameterValuesMap.get("recursive"), "true");

        applicationDeploymentRepository.getAll().forEach(d -> {
            Stream<DeploymentObject> stream = recursive ? d.stream() : d.children().stream();
            stream.forEach(dObj -> {
                if (classes.stream().anyMatch(c -> c.isInstance(dObj))) {
                    deployables.add(new ObjectRef(dObj.getParent(), dObj));
                }
            });
        });

        classes.forEach(cls -> {
            datastore.find(cls).asList().forEach(dObj -> deployables.add(new ObjectRef(null, (DeploymentObject) dObj)));
        });

    }

    public ObjectRef getSelectedDeployable() {
        return selectedDeployable;
    }

    public void setSelectedDeployable(ObjectRef selectedDeployable) {
        this.selectedDeployable = selectedDeployable;
    }

    public List<ObjectRef> getDeployables() {
        return deployables;
    }

    public void setDeployables(List<ObjectRef> deployables) {
        this.deployables = deployables;
    }

    public Boolean getIncludeConstraints() {
        return includeConstraints;
    }

    public void setIncludeConstraints(Boolean includeConstraints) {
        this.includeConstraints = includeConstraints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void save() {
        RequestContext.getCurrentInstance().closeDialog(new ImportDeployableDialogResult(selectedDeployable.getObject(), name, includeConstraints));
    }

    public void cancel() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }
}
