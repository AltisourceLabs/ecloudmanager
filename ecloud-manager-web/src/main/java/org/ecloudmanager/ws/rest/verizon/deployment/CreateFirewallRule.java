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

package org.ecloudmanager.ws.rest.verizon.deployment;


public class CreateFirewallRule {

    private String environmentName;

    private String permission;

    private String protocol;

    private String sourceType;

    private String sourceTypeValue;

    private String destinationType;

    private Long portStartRange;

    private Long portEndRange;

    public CreateFirewallRule() {
    }


    public String getEnvironmentName() {
        return environmentName;
    }


    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }


    public String getPermission() {
        return permission;
    }


    public void setPermission(String permission) {
        this.permission = permission;
    }


    public String getProtocol() {
        return protocol;
    }


    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getSourceType() {
        return sourceType;
    }


    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }


    public String getSourceTypeValue() {
        return sourceTypeValue;
    }


    public void setSourceTypeValue(String sourceTypeValue) {
        this.sourceTypeValue = sourceTypeValue;
    }


    public String getDestinationType() {
        return destinationType;
    }


    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }


    public Long getPortStartRange() {
        return portStartRange;
    }


    public void setPortStartRange(Long portStartRange) {
        this.portStartRange = portStartRange;
    }


    public Long getPortEndRange() {
        return portEndRange;
    }


    public void setPortEndRange(Long portEndRange) {
        this.portEndRange = portEndRange;
    }

}
