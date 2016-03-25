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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.jeecore.service.ServiceSupport;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;

@Stateless
public class ApplicationTemplateService extends ServiceSupport {

    @Inject
    private Logger log;

    public void saveApp(ApplicationTemplate app) {
        log.info("Saving " + app.getName());
        save(app);
        fireEntityCreated(app);
    }

    public void updateApp(ApplicationTemplate app) {
        log.info("Updating " + app.getName());
        update(app);
        fireEntityUpdated(app);
    }

    public void removeApp(ApplicationTemplate app) {
        log.info("Deleting " + app.getName());
        delete(app);
        fireEntityDeleted(app);
    }

    public String toYaml(ApplicationTemplate app) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String str = mapper.writeValueAsString(app);
        return str;
    }

    public ApplicationTemplate fromYaml(String yamlStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ObjectReader reader = mapper.reader(ApplicationTemplate.class);
        return reader.readValue(yamlStr);
    }
}