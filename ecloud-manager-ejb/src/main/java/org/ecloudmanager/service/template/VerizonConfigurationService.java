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

package org.ecloudmanager.service.template;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.domain.verizon.VerizonConfiguration;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.VerizonConfigurationRepository;
import org.picketlink.Identity;

import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Stateless
public class VerizonConfigurationService extends ServiceSupport {
    @Inject
    private transient Logger log;
    @Inject
    private transient VerizonConfigurationRepository verizonConfigurationRepository;
    @Inject
    Identity identity;

    public void saveOrUpdate(VerizonConfiguration configuration) {
        log.info("Saving Verizon Configuration " + configuration.getName());
        configuration.setOwner(identity.getAccount().getId());
        super.saveOrUpdate(configuration);
        fireEvent(configuration);
    }

    public void remove(VerizonConfiguration configuration) {
        log.info("Deleting Verizon Configuration " + configuration.getName());
        delete(configuration);
        fireEvent(configuration);
    }

    public VerizonConfiguration getCurrentConfiguration() {
        List<VerizonConfiguration> verizonConfigurations = verizonConfigurationRepository.getAllForUser(identity.getAccount().getId());
        return verizonConfigurations.size() > 0 ? verizonConfigurations.get(0) : new VerizonConfiguration();
    }
}