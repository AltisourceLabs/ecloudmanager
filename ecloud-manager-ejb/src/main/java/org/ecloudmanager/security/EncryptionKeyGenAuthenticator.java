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

import com.rits.cloning.Cloner;
import org.jasypt.util.text.BasicTextEncryptor;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;

import javax.inject.Inject;

@PicketLink
public class EncryptionKeyGenAuthenticator extends IdmAuthenticator implements Authenticator {
    @Inject
    DefaultLoginCredentials credentials;

    @Inject
    Cloner cloner;

    @Override
    public Account getAccount() {
        Account account = super.getAccount();
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(credentials.getPassword());

        UserWithEncryptor userWithEncryptor = new UserWithEncryptor();
        cloner.copyPropertiesOfInheritedClass(account, userWithEncryptor);
        userWithEncryptor.setEncryptor(encryptor);

        return userWithEncryptor;
    }
}
