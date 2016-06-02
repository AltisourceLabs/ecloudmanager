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

import org.ecloudmanager.domain.chef.ChefConfiguration;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.ChefConfigurationRepository;
import org.ecloudmanager.service.template.ChefConfigurationService;
import org.picketlink.Identity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class ChefConfigurationController extends FacesSupport implements Serializable {
    @Inject
    private transient Identity identity;

    @Inject
    private transient ChefConfigurationRepository chefConfigurationRepository;
    @Inject
    private transient ChefConfigurationService chefConfigurationService;

    private List<ChefConfiguration> chefConfigurations;

    public class ChefConfigurationEntityEditorController extends EncryptedEntityEditorController<ChefConfiguration> {
        protected ChefConfigurationEntityEditorController() {
            super(ChefConfiguration.class);
        }

        @Override
        public void delete(ChefConfiguration entity) {
            chefConfigurationService.remove(entity);
            refresh();
        }

        @Override
        protected void doSave(ChefConfiguration old, ChefConfiguration entity) {
            chefConfigurationService.update(entity);
            refresh();
        }

        @Override
        protected void doAdd(ChefConfiguration entity) {
            chefConfigurationService.save(entity);
            refresh();
        }
    }

    private ChefConfigurationEntityEditorController editorController = new ChefConfigurationEntityEditorController();

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        chefConfigurations = chefConfigurationRepository.getAllForUser(identity.getAccount().getId());
    }

    public EntityEditorController<ChefConfiguration> getEditorController() {
        return editorController;
    }

    public List<ChefConfiguration> getChefConfigurations() {
        return chefConfigurations;
    }
}
