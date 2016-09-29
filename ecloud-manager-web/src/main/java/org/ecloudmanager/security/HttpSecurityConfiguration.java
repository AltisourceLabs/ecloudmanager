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

import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;

import javax.enterprise.event.Observes;

public class HttpSecurityConfiguration {
    public void onInit(@Observes SecurityConfigurationEvent event) {
        SecurityConfigurationBuilder builder = event.getBuilder();

        builder
                .http()

                .allPaths()
                .authenticateWith().form()
                .authenticationUri("/login.xhtml")
                .loginPage("/login.xhtml")
                .errorPage("/login.xhtml?error=true")
                .restoreOriginalRequest()

                .forPath("/api/*").authenticateWith().basic()

                .forPath("/logout")
                .logout()
                .redirectTo("/login.xhtml")

                .forPath("/javax.faces.resource/*").unprotected()

                .forPath("/admin/*")
                .authorizeWith().role("administrator")
                .authenticateWith().form()
                .authenticationUri("/login.xhtml")
                .loginPage("/login.xhtml")
                .errorPage("/login.xhtml?error=true")
                .restoreOriginalRequest();
//                .forPath("/javax.faces.resource/*.css.xhtml").unprotected()
//                .forPath("/javax.faces.resource/primefaces.js.xhtml").unprotected()
//                .forPath("/javax.faces.resource/jquery/*").unprotected()
//                .forPath("/javax.faces.resource/fa/*").unprotected()
//                .forPath("/javax.faces.resource/spacer/*").unprotected();
    }
}