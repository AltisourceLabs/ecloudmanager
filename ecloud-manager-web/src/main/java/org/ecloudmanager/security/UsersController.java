package org.ecloudmanager.security;

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Controller
@Transactional
public class UsersController extends FacesSupport implements Serializable {

    @Inject
    private transient IdentityManager identityManager;
    @Inject
    private transient RelationshipManager relationshipManager;

    private boolean edit = false;

    private Collection<User> users;
    private User user = new User();
    private String password;

    private Collection<UserRole> userRoles = new ArrayList<>();

    public static class UserRole {
        private boolean granted;
        private Role role;

        UserRole(Role role) {
            this.role = role;
        }

        public boolean isGranted() {
            return granted;
        }

        public void setGranted(boolean granted) {
            this.granted = granted;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }

    @PostConstruct
    private void init() {
        refresh();

        IdentityQuery<Role> roleIdentityQuery = identityManager.getQueryBuilder().createIdentityQuery(Role.class);
        roleIdentityQuery.getResultList().forEach(role -> {
            userRoles.add(new UserRole(role));});
    }

    private void refresh() {
        IdentityQuery<User> query = identityManager.getQueryBuilder().createIdentityQuery(User.class);
        users = query.getResultList();
    }

    public void startEdit(User user) {
        edit = true;
        this.user = BasicModel.getUser(identityManager, user.getLoginName());

        password = "\u25CF\u25CF\u25CF\u25CF\u25CF";

        userRoles.forEach(userRole -> {
            userRole.setGranted(BasicModel.hasRole(relationshipManager, this.user, userRole.getRole()));
        });
    }

    public void handleClose() {
        reset();
    }

    private void reset() {
        edit = false;

        user = new User();

        password = null;

        userRoles.forEach(userRole -> {
            userRole.setGranted(false);
        });
    }

    public boolean isEdit() {
        return edit;
    }

    public void save() {
        if (edit) {
            identityManager.update(user);
        } else {
            identityManager.add(user);
        }

        if (!StringUtils.isEmpty(password) && !password.contains("\u25CF")) {
            Password passwd = new Password(password);
            identityManager.updateCredential(user, passwd);
        }

        userRoles.forEach(userRole -> {
            if (userRole.isGranted()) {
                BasicModel.grantRole(relationshipManager, user, userRole.getRole());
            } else {
                BasicModel.revokeRole(relationshipManager, user, userRole.getRole());
            }
        });

        refresh();
        reset();
    }

    public void cancel() {
        reset();
    }

    public void delete(User user) {
        identityManager.remove(user);
        refresh();
    }

    public Collection<User> getUsers() {
        return users;
    }

    public User getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<UserRole> getUserRoles() {
        return userRoles;
    }
}