package org.ecloudmanager.security;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class SecurityInitializer {
    @Inject
    private IdentityManager identityManager;
    @Inject
    private RelationshipManager relationshipManager;

    @PostConstruct
    public void create() {
        IdentityQuery<Role> roleIdentityQuery = identityManager.getQueryBuilder().createIdentityQuery(Role.class);
        int resultCount = roleIdentityQuery.getResultCount();

        Role adminRole = new Role("administrator");

        if (resultCount == 0) {
            identityManager.add(adminRole);
        }

        IdentityQuery<User> query = identityManager.getQueryBuilder().createIdentityQuery(User.class);
        resultCount = query.getResultCount();

        if (resultCount == 0) {
            User user = new User("admin");

            user.setEmail("");
            user.setFirstName("");
            user.setLastName("");

            identityManager.add(user);
            identityManager.updateCredential(user, new Password("secret"));

            BasicModel.grantRole(relationshipManager, user, adminRole);
        }
    }
}