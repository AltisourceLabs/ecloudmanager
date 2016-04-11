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

import org.picketlink.Identity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import static org.picketlink.idm.model.basic.BasicModel.*;

@Named
@Stateless
public class AuthorizationHelper {
    @Inject
    private Identity identity;
    @Inject
    private IdentityManager identityManager;
    @Inject
    private RelationshipManager relationshipManager;

    public boolean hasAppRole(String roleName) {
        Role role = getRole(identityManager, roleName);
        return hasRole(relationshipManager, identity.getAccount(), role);
    }

    public boolean isGroupMember(String groupName) {
        Group group = getGroup(identityManager, groupName);
        return isMember(relationshipManager, identity.getAccount(), group);
    }

    public boolean hasRoleAndGroupMember(String roleName, String groupName) {
        Group group = getGroup(identityManager, groupName);
        Role role = getRole(identityManager, roleName);
        return hasGroupRole(relationshipManager, identity.getAccount(), role, group);
    }
}
