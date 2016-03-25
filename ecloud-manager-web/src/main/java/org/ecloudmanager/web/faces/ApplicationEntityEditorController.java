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

import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.service.template.ApplicationTemplateService;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Controller
public class ApplicationEntityEditorController extends EntityEditorController<ApplicationTemplate> {

    private static final long serialVersionUID = 6611210728736691504L;
    private ApplicationTemplateController applicationTemplateController = Beans.getInstance
        (ApplicationTemplateController.class, false);

    private ApplicationTemplateService applicationTemplateService = Beans.getInstance(ApplicationTemplateService
        .class, true);


    public ApplicationEntityEditorController() {
        super(ApplicationTemplate.class);
    }

    @Override
    @PostConstruct
    protected void init() {
        importing = false;
        super.init();
    }

    public boolean isImporting() {
        return importing;
    }

    public void startImport() {
        importing = true;
    }

    private boolean importing = false;

    @Override
    public void delete(ApplicationTemplate entity) {
        applicationTemplateService.removeApp(entity);
        applicationTemplateController.refresh();
    }

    @Override
    protected void doSave(ApplicationTemplate old, ApplicationTemplate entity) {
        applicationTemplateService.updateApp(entity);
        applicationTemplateController.refresh();
    }

    @Override
    protected void doAdd(ApplicationTemplate entity) {
        applicationTemplateService.saveApp(entity);
        applicationTemplateController.refresh();
    }

    public void handleFileUpload(FileUploadEvent event) throws IOException {
        String yaml = new String(event.getFile().getContents());
        ApplicationTemplate app = applicationTemplateService.fromYaml(yaml);
        selected = app;
    }

    public void handleClose(CloseEvent event) {
        super.handleClose(event);
        applicationTemplateController.getTemplateEntityEditorController().hideDialogs(false);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('dlg_edit').hide()");
    }

}
