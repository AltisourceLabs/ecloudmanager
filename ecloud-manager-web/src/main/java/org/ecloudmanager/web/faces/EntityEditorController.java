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

import com.rits.cloning.Cloner;
import org.ecloudmanager.util.ClonerProducer;
import org.primefaces.event.CloseEvent;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Constructor;

@Deprecated
public abstract class EntityEditorController<T> implements Serializable {
    private static final long serialVersionUID = -8687883396621377676L;
    protected T selected;
    private T old;
    private boolean edit = false;
    private Constructor<? extends T> ctor;
    private Class<? extends T> impl;
    protected EntityEditorController(Class<? extends T> impl) {
        setClassImpl(impl);
    }

    public T getOld() {
        return old;
    }

    protected void setClassImpl(Class<? extends T> impl) {
        try {
            this.impl = impl;
            this.ctor = impl.getConstructor();
            init();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't find constructor", e);
        }

    }

    public void startEdit(T entity) {
        this.old = entity;
        Cloner cloner = new ClonerProducer().produceCloner();
        try {
            this.selected = cloner.deepClone(entity);
        } catch (Exception e) {
            // should not happen
            e.printStackTrace();
        }
        this.edit = true;
    }

    protected void init() {
        try {
            selected = ctor.newInstance();
        } catch (Exception e) {
            // should not happen
            e.printStackTrace();
        }
        edit = false;
    }

    public abstract void delete(T entity);

    public void save() {
        doSave(old, selected);
        init();
    }

    protected abstract void doSave(T old, T entity);

    protected abstract void doAdd(T entity);


    public void cancel() {
        init();
    }


    public void add() {
        doAdd(selected);
        init();
    }

    @NotNull
    public T getSelected() {
        return selected;
    }

    public boolean isEdit() {
        return edit;
    }

    public void handleClose(CloseEvent event) {
        cancel();
    }

    public Class<? extends T> getImpl() {
        return impl;
    }
}
