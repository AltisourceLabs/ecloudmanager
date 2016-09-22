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
import org.primefaces.context.RequestContext;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Controller
public class ImportDeployableController extends FacesSupport implements Serializable{
    private static final String DIALOG = "dlg_import_deployable";

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

    @FunctionalInterface
    public interface ImportDeployableDialogSaveCallback {
        void save(ImportDeployableDialogResult importResult);
    }

    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private transient Datastore datastore;

    private ObjectRef selectedDeployable;
    private List<ObjectRef> deployables = new ArrayList<>();

    private Boolean includeConstraints;
    private String name;

    private ImportDeployableDialogSaveCallback callback;

    public void openDialog(Set<Class> classes, boolean recursive, ImportDeployableDialogSaveCallback callback) {
        this.callback = callback;
        this.name = null;
        selectedDeployable = null;
        deployables.clear();

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

        RequestContext.getCurrentInstance().execute("PF('" + DIALOG + "').show();");
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
        if (callback != null && selectedDeployable != null) {
            callback.save(new ImportDeployableDialogResult(selectedDeployable.getObject(), name, includeConstraints));
        }
        RequestContext.getCurrentInstance().execute("PF('" + DIALOG + "').hide();");
    }

    public void cancel() {
        RequestContext.getCurrentInstance().execute("PF('" + DIALOG + "').hide();");
    }
}
