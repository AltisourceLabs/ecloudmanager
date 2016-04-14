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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.security.Encrypted;
import org.picketlink.common.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EncryptedEntityEditorController<T> extends EntityEditorController<T> {
    private static final String DOTS_STR = "\u25CF\u25CF\u25CF\u25CF\u25CF";
    private static final String DOT_STR = "\u25CF";

    Logger log = LogManager.getLogger(EncryptedEntityEditorController.class);

    private final Set<Field> encryptedFields;

    protected EncryptedEntityEditorController(Class<? extends T> impl) {
        super(impl);
        encryptedFields = Stream.of(getImpl().getDeclaredFields())
                .filter(field -> field.getAnnotation(Encrypted.class) != null)
                .collect(Collectors.toSet());
    }

    @Override
    public void startEdit(T entity) {
        super.startEdit(entity);

        encryptedFields.forEach(field -> {
            try {
                field.setAccessible(true);
                if (!StringUtils.isEmpty((CharSequence) field.get(getSelected()))) {
                    field.set(getSelected(), DOTS_STR);
                }
            } catch (IllegalAccessException e) {
                log.error(e);
            }
        });
    }

    @Override
    public void save() {
        encryptedFields.forEach(field -> {
            try {
                String val = (String) field.get(getSelected());
                if (!StringUtils.isEmpty(val) && val.contains(DOT_STR)) {
                    String oldVal = (String) field.get(getOld());
                    // Restore the old value
                    field.set(getSelected(), oldVal);
                }
            } catch (IllegalAccessException e) {
                log.error(e);
            }
        });
        super.save();
    }
}
