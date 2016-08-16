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

package org.ecloudmanager.deployment.ps.cg;

import org.ecloudmanager.deployment.ps.HAProxyMode;
import org.ecloudmanager.jeecore.domain.DefaultDomainObject;

import java.util.ArrayList;
import java.util.List;

public class HAProxyBackendConfig extends DefaultDomainObject {

    private String name;

    private List<String> config = new ArrayList<>();
    private HAProxyMode mode = HAProxyMode.HTTP;
    private boolean stickyServers;
    private String serverOptions;

    public HAProxyBackendConfig() {
    }

    public HAProxyBackendConfig(HAProxyBackendConfig cfg) {
        name = cfg.getName();
        config.addAll(cfg.getConfig());
        serverOptions = cfg.getServerOptions();
        stickyServers = cfg.getStickyServers();
        mode = cfg.getMode();
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

    public String getServerOptions() {
        return serverOptions;
    }

    public void setServerOptions(String serverOptions) {
        this.serverOptions = serverOptions;
    }

    public boolean getStickyServers() {
        return stickyServers;
    }

    public void setStickyServers(boolean stickyServers) {
        this.stickyServers = stickyServers;
    }

    public HAProxyMode getMode() {
        return mode;
    }

    public void setMode(HAProxyMode mode) {
        this.mode = mode;
    }
}
