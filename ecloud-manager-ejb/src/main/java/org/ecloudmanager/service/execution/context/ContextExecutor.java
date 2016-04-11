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

package org.ecloudmanager.service.execution.context;

import com.rits.cloning.Cloner;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.picketlink.Identity;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.concurrent.Executor;

@Stateless
@LocalBean
public class ContextExecutor implements Executor {
    @Inject
    BoundSessionContext boundSessionContext;
    @Inject
    GlobalExecutionContext globalExecutionContext;
    @Inject
    Instance<Identity> identityInstance;
    @Inject
    Cloner cloner;
    @Inject
    private BeanManager beanManager;

    @Asynchronous
    @Override
    public void execute(Runnable command) {
        HashMap<String, Object> storage = new HashMap<>();
        boundSessionContext.associate(storage);
        boundSessionContext.activate();

        Identity identity = globalExecutionContext.remove(command);
        Bean<Identity> bean = (Bean<Identity>) beanManager.resolve(beanManager.getBeans(Identity.class));
        Identity localIdentity = beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));

        cloner.copyPropertiesOfInheritedClass(identity, localIdentity);

        command.run();

        boundSessionContext.invalidate();
        boundSessionContext.deactivate();
        boundSessionContext.dissociate(storage);
    }
}
