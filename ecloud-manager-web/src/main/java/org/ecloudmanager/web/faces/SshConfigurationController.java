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

import org.ecloudmanager.domain.template.SshConfiguration;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.SshConfigurationRepository;
import org.ecloudmanager.service.template.SshConfigurationService;
import org.picketlink.Identity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class SshConfigurationController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = 5537710186791195525L;

    @Inject
    private transient Identity identity;

    @Inject
    private transient SshConfigurationRepository sshConfigurationRepository;
    @Inject
    private transient SshConfigurationService sshConfigurationService;

    private List<SshConfiguration> sshConfigurations;

    public class SshConfigurationEntityEditorController extends EntityEditorController<SshConfiguration> {
        protected SshConfigurationEntityEditorController() {
            super(SshConfiguration.class);
        }

        @Override
        public void delete(SshConfiguration entity) {
            sshConfigurationService.remove(entity);
            refresh();
        }

        @Override
        protected void doSave(SshConfiguration old, SshConfiguration entity) {
            sshConfigurationService.update(entity);
            refresh();
        }

        @Override
        protected void doAdd(SshConfiguration entity) {
            sshConfigurationService.save(entity);
            refresh();
        }
    }

    private SshConfigurationEntityEditorController editorController = new SshConfigurationEntityEditorController();

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        sshConfigurations = sshConfigurationRepository.getAllForUser(identity.getAccount().getId());
    }

    public EntityEditorController<SshConfiguration> getEditorController() {
        return editorController;
    }

    public List<SshConfiguration> getSshConfigurations() {
        return sshConfigurations;
    }
}
