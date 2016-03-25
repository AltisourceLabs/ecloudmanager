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

package org.ecloudmanager.jeecore.web.faces;

import org.primefaces.context.RequestContext;

import javax.faces.application.*;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.event.PhaseId;
import javax.faces.flow.FlowHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Cookie;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * Support for any JSF backing bean
 *
 * @author irosu
 */
public abstract class FacesSupport {

    protected FacesContext facesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Prime Faces context
     *
     * @return
     */
    protected RequestContext requestContext() {
        return RequestContext.getCurrentInstance();
    }

    // Faces Context Facade
    protected ExternalContext getExternalContext() {
        return facesContext().getExternalContext();
    }

    protected Application getApplication() {
        return facesContext().getApplication();
    }

    protected ExceptionHandler getExceptionHandler() {
        return facesContext().getExceptionHandler();
    }

    protected ResponseStream getResponseStream() {
        return facesContext().getResponseStream();
    }

    protected ResponseWriter getResponseWriter() {
        return facesContext().getResponseWriter();
    }

    protected UIViewRoot getViewRoot() {
        return facesContext().getViewRoot();
    }

    protected PartialViewContext getPartialViewContext() {
        return facesContext().getPartialViewContext();
    }

    protected PhaseId getCurrentPhaseId() {
        return facesContext().getCurrentPhaseId();
    }

    protected boolean isRenderingResponse() {
        return PhaseId.RENDER_RESPONSE.equals(getCurrentPhaseId());
    }

    protected List<FacesMessage> getMessageList() {
        return facesContext().getMessageList();
    }

    protected void addMessage(FacesMessage message) {
        facesContext().addMessage(null, message);
    }

    protected void addMessage(String clientId, FacesMessage message) {
        facesContext().addMessage(clientId, message);
    }

    protected void addMessage(String clientId, FacesMessage message, boolean keepMessage) {
        getFlash().setKeepMessages(keepMessage);
        addMessage(clientId, message);
    }

    // External Context Facade
    protected HttpSession getSession() {
        return (HttpSession) getExternalContext().getSession(false);
    }

    protected HttpServletRequest getRequest() {
        return (HttpServletRequest) getExternalContext().getRequest();
    }

    protected HttpServletResponse getResponse() {
        return (HttpServletResponse) getExternalContext().getResponse();
    }

    protected Map<String, String> getRequestParameterMap() {
        return getExternalContext().getRequestParameterMap();
    }

    protected String getInitParameter(String name) {
        return getExternalContext().getInitParameter(name);
    }

    protected Flash getFlash() {
        return getExternalContext().getFlash();
    }

    protected void redirect(String url) throws IOException {
        getExternalContext().redirect(url);
    }

    protected void dispatch(String url) throws IOException {
        getExternalContext().dispatch(url);
    }

    // Response Facade
    protected void addResponseCookie(String name, String value, Map<String, Object> properties) {
        getExternalContext().addResponseCookie(name, value, properties);
    }

    protected void addResponseCookie(String name, String value, String path, int maxAge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("maxAge", maxAge);
        properties.put("path", path);

        addResponseCookie(name, value, properties);
    }

    protected void addResponseCookie(String name, String value, String path, int maxAge, boolean httpOnly) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("maxAge", maxAge);
        properties.put("path", path);
        properties.put("httpOnly", httpOnly);

        addResponseCookie(name, value, properties);
    }

    protected void removeResponseCookie(String name, String value) {
        addResponseCookie(name, value, null, 0);
    }

    protected void addResponseHeader(String name, String value) {
        getExternalContext().addResponseHeader(name, value);
    }

    // Request Facade
    protected Cookie getRequestCookie(String name) {
        return (Cookie) getExternalContext().getRequestCookieMap().get(name);

    }

    protected String getRequestHeader(String name) {
        return getExternalContext().getRequestHeaderMap().get(name);
    }

    protected boolean isUserInRole(String role) {
        return getRequest().isUserInRole(role);
    }

    protected boolean isUserInRole(HttpServletRequest request, String role) {
        return request.isUserInRole(role);
    }

    protected Principal getPrincipal() {
        return getRequest().getUserPrincipal();
    }

    protected String getPrincipalName() {
        Principal principal = getPrincipal();
        return principal != null ? principal.getName() : null;
    }

    protected String getPrincipalName(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null ? principal.getName() : null;
    }

    protected String getRequestParameter(String key) {
        return getRequestParameterMap().get(key);
    }

    protected boolean hasRequestParameter(String key) {
        return getRequestParameterMap().containsKey(key);
    }

    protected Locale getRequestLocale() {
        return getExternalContext().getRequestLocale();
    }

    // Application Facade
    protected NavigationHandler getNavigationHandler() {
        return getApplication().getNavigationHandler();
    }

    protected ViewHandler getViewHandler() {
        return getApplication().getViewHandler();
    }

    protected FlowHandler getFlowHandler() {
        return getApplication().getFlowHandler();
    }

    protected ResourceHandler getResourceHandler() {
        return getApplication().getResourceHandler();
    }

    protected ResourceBundle getResourceBundle(String name) {
        return getApplication().getResourceBundle(facesContext(), name);
    }

    protected ProjectStage getProjectStage() {
        return getApplication().getProjectStage();
    }

    protected boolean isDevelopment() {
        return ProjectStage.Development.equals(getProjectStage());
    }

    protected boolean isProduction() {
        return ProjectStage.Production.equals(getProjectStage());
    }

    // Navigation Handler Facade
    protected void navigate(String fromAction, String outcome) {
        getNavigationHandler().handleNavigation(facesContext(), fromAction, outcome);
    }

    protected void navigate(String fromAction, String outcome, String toFlowDocumentId) {
        getNavigationHandler().handleNavigation(facesContext(), fromAction, outcome, toFlowDocumentId);
    }

    // View Handler Facade
    protected String getActionURL(String viewId) {
        return getViewHandler().getActionURL(facesContext(), viewId);
    }

    // Partial View Context Facade
    protected boolean isAjaxRequest() {
        return getPartialViewContext().isAjaxRequest();
    }

    // Root View Facade
    protected boolean isRootView(String viewId) {
        return getViewRoot().getViewId().equals(viewId);
    }

    protected Locale getLocale() {
        return getViewRoot().getLocale();
    }

    protected void setLocale(Locale locale) {
        getViewRoot().setLocale(locale);
    }


    protected String getRootErrorMessage(Exception e) {
        // Default to general error message that registration failed.
        String errorMessage = "ERROR!";
        if (e == null) {
            // This shouldn't happen, but return the default messages
            return errorMessage;
        }

        // Start with the exception and recurse to find the root cause
        Throwable t = e;
        while (t != null) {
            // Get the message from the Throwable class instance
            errorMessage = t.getLocalizedMessage();
            t = t.getCause();
        }
        // This is the root cause message
        return errorMessage;
    }

}
