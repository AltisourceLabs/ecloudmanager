package org.ecloudmanager.security;

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.jeecore.repository.MongoDBRepositorySupport;
import org.ecloudmanager.jeecore.repository.RepositorySupport;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.converters.TypeConverter;
import org.omnifaces.el.functions.Arrays;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;

@Controller
@Transactional
public class UsersController extends FacesSupport implements Serializable {
    @Inject
    Identity identity;
    @Inject
    private transient IdentityManager identityManager;
    @Inject
    private transient RelationshipManager relationshipManager;
    @Inject
    private transient Instance<RepositorySupport> repositories;


    private boolean edit = false;
    private boolean current = false;

    private Collection<User> users;
    private User user = new User();
    private String password;
    private String oldPassword;
    private String newPassword;
    private String newPassword1;

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
    public boolean isCurrent() {
        return current;
    }

    public void save() {
        if (edit) {
            identityManager.update(user);
        } else {
            identityManager.add(user);
        }

        if (!edit && !StringUtils.isEmpty(password) && !password.contains("\u25CF")) {
            Password passwd = new Password(password);
            identityManager.updateCredential(user, passwd);
        }

        if (!current) {
            userRoles.forEach(userRole -> {
                if (userRole.isGranted()) {
                    BasicModel.grantRole(relationshipManager, user, userRole.getRole());
                } else {
                    BasicModel.revokeRole(relationshipManager, user, userRole.getRole());
                }
            });
        }

        if (edit || current) {
            addMessage(new FacesMessage("User profile is updated.", ""));
        }

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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword1() {
        return newPassword1;
    }

    public void setNewPassword1(String newPassword1) {
        this.newPassword1 = newPassword1;
    }

    public Collection<UserRole> getUserRoles() {
        return userRoles;
    }

    public void currentUser() {
        edit = true;
        current = true;
        if (identity.getAccount() instanceof User) {
            user = BasicModel.getUser(identityManager, ((User)identity.getAccount()).getLoginName());
        }
    }

    private List<MongoDBRepositorySupport> getRepositoriesWithEncryption() {
        List<MongoDBRepositorySupport> result = new ArrayList<>();
        repositories.forEach(repositorySupport -> {
            MongoDBRepositorySupport repository = (MongoDBRepositorySupport) repositorySupport;
            Class entityType = repository.getEntityType();
            if (entityType != null) {
                Annotation annotation = entityType.getAnnotation(Converters.class);
                if (annotation != null && annotation instanceof Converters) {
                    Class<? extends TypeConverter>[] value = ((Converters) annotation).value();
                    if (Arrays.contains(value, EncryptedStringConverter.class)) {
                        result.add(repository);
                    }
                }
            }
        });
        return result;
    }

    private Map<MongoDBRepositorySupport, List> getAllEncryptedObjects() {
        Map<MongoDBRepositorySupport, List> result = new HashMap<>();
        List<MongoDBRepositorySupport> repositoriesWithEncryption = getRepositoriesWithEncryption();
        repositoriesWithEncryption.forEach(repository -> {
            result.put(repository, repository.getAllForUser(user.getId()));
        });

        return result;
    }

    private void restoreEncryptedObjects(Map<MongoDBRepositorySupport, List> allObjects) {
        allObjects.forEach(MongoDBRepositorySupport::saveAll);
    }

    public void changePassword() {
        UserWithEncryptor userWithEncryptor = (UserWithEncryptor) identity.getAccount();
        TextEncryptor oldEncryptor = userWithEncryptor.getEncryptor();

        // Make sure that we can read encrypted data
        boolean userIsLoggedIn = userWithEncryptor.getLoginName().equals(user.getLoginName());
        if (!userIsLoggedIn) {
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(oldPassword);
            userWithEncryptor.setEncryptor(encryptor);
        }

        // Save user's encrypted data to memory
        Map<MongoDBRepositorySupport, List> allEncryptedObjects = getAllEncryptedObjects();

        // Update password hash in DB
        Password passwd = new Password(newPassword);
        identityManager.updateCredential(user, passwd);

        // Make sure that we use a new password to write the encrypted data
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(newPassword);
        userWithEncryptor.setEncryptor(encryptor);

        // Write all user's encrypted data to mongodb
        allEncryptedObjects.forEach(MongoDBRepositorySupport::saveAll);

        // Restore the current user's encryptor if needed
        if (!userIsLoggedIn) {
            userWithEncryptor.setEncryptor(oldEncryptor);
        }

        addMessage(new FacesMessage("Password has been changed.", ""));
    }

    public void resetChangePassword() {
        oldPassword = null;
        newPassword = null;
        newPassword1 = null;
    }

    public void validateOldPassword(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String oldPassword = (String) value;
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), new Password(oldPassword));
        identityManager.validateCredentials(credentials);
        if (!credentials.getStatus().equals(Credentials.Status.VALID)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Old password is invalid", "");
            throw new ValidatorException(message);
        }
    }
}