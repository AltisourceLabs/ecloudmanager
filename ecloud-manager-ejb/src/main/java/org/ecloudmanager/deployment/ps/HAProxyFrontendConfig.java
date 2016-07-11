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

package org.ecloudmanager.deployment.ps;

import org.ecloudmanager.service.deployment.geolite.GeolocationRule;

import java.util.ArrayList;
import java.util.List;

public class HAProxyFrontendConfig {

    private String name;

    private HAProxyMode mode = HAProxyMode.HTTP;
    private String defaultBackend;
    private boolean stickyBackends;
    private List<BackendWeight> backendWeights = new ArrayList<>();
    private List<GeolocationRule> geolocationRules = new ArrayList<>();
    private boolean useXff;
    private List<String> config = new ArrayList<>();

    public HAProxyFrontendConfig() {
    }

    public HAProxyFrontendConfig(HAProxyFrontendConfig cfg) {
        name = cfg.getName();
        config.addAll(cfg.getConfig());
        backendWeights.addAll(cfg.getBackendWeights());
        geolocationRules.addAll(cfg.getGeolocationRules());
        defaultBackend = cfg.getDefaultBackend();
        stickyBackends = cfg.getStickyBackends();
        mode = cfg.getMode();
        useXff = cfg.getUseXff();
    }

    public List<String> getConfig() {
        return config;
    }

    public void setConfig(List<String> config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultBackend() {
        return defaultBackend;
    }

    public void setDefaultBackend(String defaultBackend) {
        this.defaultBackend = defaultBackend;
    }

    public List<BackendWeight> getBackendWeights() {
        return backendWeights;
    }

    public void setBackendWeights(List<BackendWeight> backendWeights) {
        this.backendWeights = backendWeights;
    }

    public boolean getStickyBackends() {
        return stickyBackends;
    }

    public void setStickyBackends(boolean stickyBackends) {
        this.stickyBackends = stickyBackends;
    }

    public HAProxyMode getMode() {
        return mode;
    }

    public void setMode(HAProxyMode mode) {
        this.mode = mode;
    }

    public List<GeolocationRule> getGeolocationRules() {
        return geolocationRules;
    }

    public void setGeolocationRules(List<GeolocationRule> geolocationRules) {
        this.geolocationRules = geolocationRules;
    }

    public boolean getUseXff() {
        return useXff;
    }

    public void setUseXff(boolean useXff) {
        this.useXff = useXff;
    }
}
