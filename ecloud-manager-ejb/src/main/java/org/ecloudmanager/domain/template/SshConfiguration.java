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

package org.ecloudmanager.domain.template;

import org.ecloudmanager.domain.OwnedMongoObject;
import org.ecloudmanager.security.Encrypted;
import org.ecloudmanager.security.EncryptedStringConverter;
import org.ecloudmanager.security.UserWithEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.PreSave;
import org.picketlink.Identity;
import org.picketlink.idm.model.Account;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;

@Entity(noClassnameStored = true)
@Converters(EncryptedStringConverter.class)
public class SshConfiguration extends OwnedMongoObject implements Serializable {
    private static final long serialVersionUID = 1293171298086413994L;

    private String environment;
    private String jumpHost1;
    private String jumpHost1Username;
    private String jumpHost2;
    private String jumpHost2Username;
    private String username;
    @Encrypted
    private String privateKey;
    @Encrypted
    private String privateKeyPassphrase;
    @Encrypted
    private String jumpHost1PrivateKey;
    @Encrypted
    private String jumpHost1PrivateKeyPassphrase;
    @Encrypted
    private String jumpHost2PrivateKey;
    @Encrypted
    private String jumpHost2PrivateKeyPassphrase;

    public SshConfiguration() {
    }

    public SshConfiguration(SshConfiguration configuration) {
        setId(configuration.getId());
        environment = configuration.getEnvironment();
        jumpHost1 = configuration.getJumpHost1();
        jumpHost1Username = configuration.getJumpHost1Username();
        jumpHost2 = configuration.getJumpHost2();
        jumpHost2Username = configuration.getJumpHost2Username();
        username = configuration.getUsername();
        privateKey = configuration.getPrivateKey();
        privateKeyPassphrase = configuration.getPrivateKeyPassphrase();
        jumpHost1PrivateKey = configuration.getJumpHost1PrivateKey();
        jumpHost1PrivateKeyPassphrase = configuration.getJumpHost1PrivateKeyPassphrase();
        jumpHost2PrivateKey = configuration.getJumpHost2PrivateKey();
        jumpHost2PrivateKeyPassphrase = configuration.getJumpHost2PrivateKeyPassphrase();
    }

    public String getJumpHost1() {
        return jumpHost1;
    }

    public void setJumpHost1(String jumpHost1) {
        this.jumpHost1 = jumpHost1;
    }

    public String getJumpHost1Username() {
        return jumpHost1Username;
    }

    public void setJumpHost1Username(String jumpHost1Username) {
        this.jumpHost1Username = jumpHost1Username;
    }

    public String getJumpHost2() {
        return jumpHost2;
    }

    public void setJumpHost2(String jumpHost2) {
        this.jumpHost2 = jumpHost2;
    }

    public String getJumpHost2Username() {
        return jumpHost2Username;
    }

    public void setJumpHost2Username(String jumpHost2Username) {
        this.jumpHost2Username = jumpHost2Username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public String getJumpHost1PrivateKey() {
        return jumpHost1PrivateKey;
    }

    public void setJumpHost1PrivateKey(String jumpHost1PrivateKey) {
        this.jumpHost1PrivateKey = jumpHost1PrivateKey;
    }

    public String getJumpHost1PrivateKeyPassphrase() {
        return jumpHost1PrivateKeyPassphrase;
    }

    public void setJumpHost1PrivateKeyPassphrase(String jumpHost1PrivateKeyPassphrase) {
        this.jumpHost1PrivateKeyPassphrase = jumpHost1PrivateKeyPassphrase;
    }

    public String getJumpHost2PrivateKey() {
        return jumpHost2PrivateKey;
    }

    public void setJumpHost2PrivateKey(String jumpHost2PrivateKey) {
        this.jumpHost2PrivateKey = jumpHost2PrivateKey;
    }

    public String getJumpHost2PrivateKeyPassphrase() {
        return jumpHost2PrivateKeyPassphrase;
    }

    public void setJumpHost2PrivateKeyPassphrase(String jumpHost2PrivateKeyPassphrase) {
        this.jumpHost2PrivateKeyPassphrase = jumpHost2PrivateKeyPassphrase;
    }

    @Override
    public String toString() {
        return "SshConfiguration{" +
            "environment='" + environment + '\'' +
            ", jumpHost1='" + jumpHost1 + '\'' +
            ", jumpHost1Username='" + jumpHost1Username + '\'' +
            ", jumpHost2='" + jumpHost2 + '\'' +
            ", jumpHost2Username='" + jumpHost2Username + '\'' +
            ", username='" + username + '\'' +
            '}';
    }
}
