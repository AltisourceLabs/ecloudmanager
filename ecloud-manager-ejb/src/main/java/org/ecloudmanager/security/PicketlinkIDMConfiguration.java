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

import org.picketlink.annotations.PicketLink;
import org.picketlink.event.IdentityConfigurationEvent;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.model.sample.simple.*;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.*;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class PicketlinkIDMConfiguration {
    @Produces
    @PersistenceContext(unitName = "jpa-picketlink-idm")
    @PicketLink
    private EntityManager em;

    public void observeIdentityConfigurationEvent(@Observes IdentityConfigurationEvent event) throws Exception {
        IdentityConfigurationBuilder builder = event.getConfig();

        builder.named("idm-config")
                .stores()
                .jpa()
                .supportAllFeatures();
//                .supportType(IdentityType.class, User.class, Agent.class, Role.class, Group.class)
//                .supportCredentials(true)
//                .supportGlobalRelationship(Relationship.class)
//                .supportGlobalRelationship(Grant.class)
//                .supportGlobalRelationship(GroupMembership.class)
//                .supportGlobalRelationship(GroupRole.class)
//                .supportAttributes(true)
//                .supportPermissions(true)
//                .addContextInitializer(new ContextInitializer() {
//                    @Override
//                    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
//                        if (store instanceof JPAIdentityStore) {
//                            context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManager);
//                        }
//                    }
//                })
//                .mappedEntity(
//                        IdentityTypeEntity.class,
//                        PartitionTypeEntity.class,
//                        AccountTypeEntity.class,
//                        RelationshipTypeEntity.class,
//                        AttributeTypeEntity.class,
////                        AbstractCredentialTypeEntity.class,
////                        X509CredentialTypeEntity.class,
//                        GroupTypeEntity.class,
////                        OTPCredentialTypeEntity.class,
////                        TokenCredentialTypeEntity.class,
//                        RoleTypeEntity.class,
//                        PasswordCredentialTypeEntity.class,
//                        RelationshipIdentityTypeEntity.class,
////                        DigestCredentialTypeEntity.class,
//                        PermissionTypeEntity.class
//                    );
//        builder.named("idm-config")
//                .stores()
//                .file()
//                .supportAllFeatures();
    }

}