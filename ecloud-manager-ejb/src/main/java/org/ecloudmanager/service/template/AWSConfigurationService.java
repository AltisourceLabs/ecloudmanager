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
import org.ecloudmanager.domain.aws.AWSConfiguration;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.AWSConfigurationRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class AWSConfigurationService extends ServiceSupport {
    @Inject
    private Logger log;
    @Inject
    private AWSConfigurationRepository AWSConfigurationRepository;

    public void saveOrUpdate(AWSConfiguration configuration) {
        log.info("Saving AWS Configuration " + configuration.getName());
        super.saveOrUpdate(configuration);
        fireEvent(configuration);
    }

    public void remove(AWSConfiguration configuration) {
        log.info("Deleting AWS Configuration " + configuration.getName());
        delete(configuration);
        fireEvent(configuration);
    }

    public AWSConfiguration getCurrentConfiguration() {
        List<AWSConfiguration> AWSConfigurations = AWSConfigurationRepository.getAll();
        return AWSConfigurations.size() > 0 ? AWSConfigurations.get(0) : new AWSConfiguration();
    }
}