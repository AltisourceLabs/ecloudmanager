package org.ecloudmanager.node.model;

import java.util.Objects;

public class SecretKey implements Credentials {

    private String name;
    private String secret;

    public SecretKey(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    private SecretKey() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecretKey sk = (SecretKey) o;
        return Objects.equals(this.name, sk.name) &&
                Objects.equals(this.secret, sk.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, secret);
    }


}
