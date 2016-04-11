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

import org.picketlink.Identity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GlobalExecutionContext {
    private ConcurrentHashMap<Runnable, Identity> identityMap = new ConcurrentHashMap<>();

    @Inject
    BeanManager beanManager;

    public void put(Runnable runnable, Identity identity) {
        Bean<Identity> bean = (Bean<Identity>) beanManager.resolve(beanManager.getBeans(Identity.class));
        Identity identityInst = beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));
        identityMap.put(runnable, identityInst);
    }

    public Identity remove(Runnable runnable) {
         return identityMap.remove(runnable);
    }
}
