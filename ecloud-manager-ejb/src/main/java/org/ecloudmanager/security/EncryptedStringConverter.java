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

package org.ecloudmanager.security;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;
import org.mongodb.morphia.converters.StringConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.picketlink.Identity;

import javax.enterprise.inject.spi.CDI;

public class EncryptedStringConverter extends StringConverter {
    public EncryptedStringConverter() {
        setSupportedTypes(null);
    }

    @Override
    protected boolean isSupported(Class<?> c, MappedField optionalExtraInfo) {
        Encrypted encrypted = null;
        if (optionalExtraInfo != null) {
            encrypted = optionalExtraInfo.getField().getAnnotation(Encrypted.class);
        }

        return encrypted != null;
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        Identity identity = CDI.current().select(Identity.class).get();
        Object obj = super.decode(targetClass, fromDBObject, optionalExtraInfo);
        try {
            if (identity != null && obj instanceof String && identity.getAccount() instanceof UserWithEncryptor) {
                TextEncryptor encryptor = ((UserWithEncryptor) identity.getAccount()).getEncryptor();
                return encryptor.decrypt((String) obj);
            }
        } catch (EncryptionOperationNotPossibleException e) {
            // Fallback to return raw data if we can't decrypt
        }
        return obj;
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        Identity identity = CDI.current().select(Identity.class).get();
        if (identity != null && value instanceof String && identity.getAccount() instanceof UserWithEncryptor) {
            TextEncryptor encryptor = ((UserWithEncryptor) identity.getAccount()).getEncryptor();
            return encryptor.encrypt((String) value);
        }
        return super.encode(value, optionalExtraInfo);
    }
}
