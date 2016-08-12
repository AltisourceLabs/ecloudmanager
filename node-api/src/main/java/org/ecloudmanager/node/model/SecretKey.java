package org.ecloudmanager.node.model;

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
}
