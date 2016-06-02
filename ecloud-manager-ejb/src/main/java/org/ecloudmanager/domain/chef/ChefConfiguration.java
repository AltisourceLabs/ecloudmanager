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

package org.ecloudmanager.domain.chef;

import org.ecloudmanager.domain.OwnedMongoObject;
import org.ecloudmanager.security.Encrypted;
import org.ecloudmanager.security.EncryptedStringConverter;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;

import java.io.Serializable;

@Entity(noClassnameStored = true)
@Converters(EncryptedStringConverter.class)
public class ChefConfiguration extends OwnedMongoObject implements Serializable {
    private String name;
    private String chefServerAddress;
    private String chefClientName;
    @Encrypted
    private String chefClientSecret;
    private String chefValidationClientName;
    @Encrypted
    private String chefValidationClientSecret;

    public ChefConfiguration() {
    }

    public ChefConfiguration(ChefConfiguration configuration) {
        setId(configuration.getId());
        name = configuration.getName();
        chefServerAddress = configuration.getChefServerAddress();
        chefClientName = configuration.getChefClientName();
        chefClientSecret = configuration.getChefClientSecret();
        chefValidationClientName = configuration.getChefValidationClientName();
        chefValidationClientSecret = configuration.getChefValidationClientSecret();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChefServerAddress() {
        return chefServerAddress;
    }

    public void setChefServerAddress(String chefServerAddress) {
        this.chefServerAddress = chefServerAddress;
    }

    public String getChefClientName() {
        return chefClientName;
    }

    public void setChefClientName(String chefClientName) {
        this.chefClientName = chefClientName;
    }

    public String getChefClientSecret() {
        return chefClientSecret;
    }

    public void setChefClientSecret(String chefClientSecret) {
        this.chefClientSecret = chefClientSecret;
    }

    public String getChefValidationClientName() {
        return chefValidationClientName;
    }

    public void setChefValidationClientName(String chefValidationClientName) {
        this.chefValidationClientName = chefValidationClientName;
    }

    public String getChefValidationClientSecret() {
        return chefValidationClientSecret;
    }

    public void setChefValidationClientSecret(String chefValidationClientSecret) {
        this.chefValidationClientSecret = chefValidationClientSecret;
    }

    @Override
    public String toString() {
        return "ChefServerConfiguration{" +
               "name='" + name + '\'' +
               ", chefServerAddress='" + chefServerAddress + '\'' +
               ", chefClientName='" + chefClientName + '\'' +
               ", chefClientSecret='" + chefClientSecret + '\'' +
               ", chefValidationClientName='" + chefValidationClientName + '\'' +
               ", chefValidationClientSecret='" + chefValidationClientSecret + '\'' +
               '}';
    }
}
