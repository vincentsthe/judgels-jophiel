package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.commons.UserActivity;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import org.iatoki.judgels.jophiel.models.domains.ForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.domains.UserActivityModel;
import org.iatoki.judgels.jophiel.models.domains.UserActivityModel_;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel_;
import play.api.Play;
import play.mvc.Http;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolationException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class UserServiceImpl implements UserService {

    private UserDao userDao;
    private EmailDao emailDao;
    private ForgotPasswordDao forgotPasswordDao;
    private UserActivityDao userActivityDao;
    private ClientDao clientDao;
    private FileSystemProvider avatarFileProvider;

    public UserServiceImpl(UserDao userDao, EmailDao emailDao, ForgotPasswordDao forgotPasswordDao, UserActivityDao userActivityDao, ClientDao clientDao, FileSystemProvider avatarFileProvider) {
        this.userDao = userDao;
        this.emailDao = emailDao;
        this.forgotPasswordDao = forgotPasswordDao;
        this.userActivityDao = userActivityDao;
        this.clientDao = clientDao;
        this.avatarFileProvider = avatarFileProvider;
        if (!avatarFileProvider.fileExists(ImmutableList.of("avatar-default.png"))) {
            avatarFileProvider.uploadFile(ImmutableList.of(), Play.getFile("default assets/avatar-default.png", Play.current()),"avatar-default.png");
        }
    }

    @Override
    public List<User> findAllUserByTerm(String term) {
        List<UserModel> userModels = userDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<User> userBuilder = ImmutableList.builder();

        for (UserModel userRecord : userModels) {
            EmailModel emailRecord  = emailDao.findByUserJid(userRecord.jid);
            userBuilder.add(new User(userRecord.id, userRecord.jid, userRecord.username, userRecord.name, emailRecord.email, getAvatarImageUrl(userRecord.profilePictureImageName), Arrays.asList(userRecord.roles.split(","))));
        }

        return userBuilder.build();
    }

    @Override
    public boolean isEmailOwnedByUser(String email, String username) {
        UserModel userModel = userDao.findByUsername(username);
        EmailModel emailModel = emailDao.findByEmail(email);

        return ((emailModel.emailVerified) && (emailModel.userJid.equals(userModel.jid)));
    }

    @Override
    public User findUserById(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public User findUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public User findPublicUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        return createPublicUserFromModels(userModel);
    }

    @Override
    public User findUserByUsername(String username) {
        UserModel userModel = userDao.findByUsername(username);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public boolean existByUsername(String username) {
        return userDao.existByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return emailDao.existByEmail(email);
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        return userDao.existsByJid(userJid);
    }

    @Override
    public String registerUser(String username, String name, String email, String password) throws IllegalStateException {
        try {
            UserModel userModel = new UserModel();
            userModel.username = username;
            userModel.name = name;
            userModel.password = JudgelsUtils.hashSHA256(password);
            userModel.profilePictureImageName = "avatar-default.png";
            userModel.roles = "user";

            userDao.persist(userModel, "guest", IdentityUtils.getIpAddress());

            String emailCode = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
            EmailModel emailModel = new EmailModel(email, emailCode);
            emailModel.userJid = userModel.jid;

            emailDao.persist(emailModel, "guest", IdentityUtils.getIpAddress());

            return emailCode;
        } catch (ConstraintViolationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void createUser(String username, String name, String email, String password, List<String> roles) {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        EmailModel emailModel = new EmailModel(email, true);
        emailModel.userJid = userModel.jid;

        emailDao.persist(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, List<String> roles) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        userModel.username = username;
        userModel.name = name;
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        emailModel.email = email;

        emailDao.edit(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, String password, List<String> roles) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        userModel.username = username;
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        emailModel.email = email;

        emailDao.edit(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        emailDao.remove(emailModel);
        userDao.remove(userModel);
    }

    @Override
    public Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> userUserJid = userDao.findUserJidsByFilter(filterString);
        List<String> emailUserJid = emailDao.findUserJidByFilter(filterString);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJid);
        setBuilder.addAll(emailUserJid);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalRow = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalRow > 0) {
            List<String> sortedUserJid;
            if (orderBy.equals("email")) {
                sortedUserJid = emailDao.sortUserJid(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJid = userDao.sortUserJids(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.findBySetOfUserJids(sortedUserJid, pageIndex * pageSize, pageSize);
            List<EmailModel> emailModels = emailDao.findBySetOfUserJid(sortedUserJid, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                EmailModel emailModel = emailModels.get(i);
                listBuilder.add(new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(","))));
            }
        }

        return new Page<>(listBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<UserActivity> pageUserActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, String username) {
        List<String> clientJids = clientDao.findClientJidsByNames(clientsNames);
        String userJid = userDao.findByUsername(username).jid;

        ImmutableMap.Builder<SingularAttribute<? super UserActivityModel, String>, List<String>> inBuilder = ImmutableMap.builder();
        if (!clientJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.clientJid, clientJids);
        }

        long totalRow = userActivityDao.countByFilters(filterString, ImmutableMap.of(UserModel_.userCreate, userJid), inBuilder.build());
        List<UserActivityModel> userActivityModels = userActivityDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(UserModel_.userCreate, userJid), inBuilder.build(), pageIndex * pageSize, pageSize);

        Map<String, String> clientNameMap = Maps.newHashMap();

        ImmutableList.Builder<UserActivity> userActivityBuilder = ImmutableList.builder();
        for (UserActivityModel userActivityModel : userActivityModels) {
            if (!clientNameMap.containsKey(userActivityModel.clientJid)) {
                if (clientDao.existsByJid(userActivityModel.clientJid)) {
                    ClientModel clientModel = clientDao.findByJid(userActivityModel.clientJid);
                    clientNameMap.put(clientModel.jid, clientModel.name);
                } else {
                    clientNameMap.put(userActivityModel.clientJid, userActivityModel.clientJid);
                }
            }
            String clientName = clientNameMap.get(userActivityModel.clientJid);

            userActivityBuilder.add(createUserActivityFromModel(userActivityModel, username, clientName));
        }

        return new Page<>(userActivityBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<UserActivity> pageUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRow = userActivityDao.countByFilters(filterString);
        List<UserActivityModel> userActivityModels = userActivityDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        Map<String, String> usernameMap = Maps.newHashMap();
        Map<String, String> clientNameMap = Maps.newHashMap();

        ImmutableList.Builder<UserActivity> userActivityBuilder = ImmutableList.builder();
        for (UserActivityModel userActivityModel : userActivityModels) {
            if (!usernameMap.containsKey(userActivityModel.userCreate)) {
                if (userDao.existsByJid(userActivityModel.userCreate)) {
                    UserModel userModel = userDao.findByJid(userActivityModel.userCreate);
                    usernameMap.put(userModel.jid, userModel.username);
                } else {
                    usernameMap.put(userActivityModel.userCreate, userActivityModel.userCreate);
                }
            }
            String username = usernameMap.get(userActivityModel.userCreate);

            if (!clientNameMap.containsKey(userActivityModel.clientJid)) {
                if (clientDao.existsByJid(userActivityModel.clientJid)) {
                    ClientModel clientModel = clientDao.findByJid(userActivityModel.clientJid);
                    clientNameMap.put(clientModel.jid, clientModel.name);
                } else {
                    clientNameMap.put(userActivityModel.clientJid, userActivityModel.clientJid);
                }
            }
            String clientName = clientNameMap.get(userActivityModel.clientJid);

            userActivityBuilder.add(createUserActivityFromModel(userActivityModel, username, clientName));
        }

        return new Page<>(userActivityBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<UserActivity> pageUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, Set<String> usernames) {
        List<String> clientJids = clientDao.findClientJidsByNames(clientsNames);
        List<String> userJids = userDao.findUserJidsByUsernames(usernames);

        ImmutableMap.Builder<SingularAttribute<? super UserActivityModel, String>, List<String>> inBuilder = ImmutableMap.builder();
        if (!clientJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.clientJid, clientJids);
        }
        if (!userJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.userCreate, userJids);
        }

        long totalRow = userActivityDao.countByFilters(filterString, ImmutableMap.of(), inBuilder.build());
        List<UserActivityModel> userActivityModels = userActivityDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), inBuilder.build(), pageIndex * pageSize, pageSize);

        Map<String, String> usernameMap = Maps.newHashMap();
        Map<String, String> clientNameMap = Maps.newHashMap();

        ImmutableList.Builder<UserActivity> userActivityBuilder = ImmutableList.builder();
        for (UserActivityModel userActivityModel : userActivityModels) {
            if (!usernameMap.containsKey(userActivityModel.userCreate)) {
                if (userDao.existsByJid(userActivityModel.userCreate)) {
                    UserModel userModel = userDao.findByJid(userActivityModel.userCreate);
                    usernameMap.put(userModel.jid, userModel.username);
                } else {
                    usernameMap.put(userActivityModel.userCreate, userActivityModel.userCreate);
                }
            }
            String username = usernameMap.get(userActivityModel.userCreate);

            if (!clientNameMap.containsKey(userActivityModel.clientJid)) {
                if (clientDao.existsByJid(userActivityModel.clientJid)) {
                    ClientModel clientModel = clientDao.findByJid(userActivityModel.clientJid);
                    clientNameMap.put(clientModel.jid, clientModel.name);
                } else {
                    clientNameMap.put(userActivityModel.clientJid, userActivityModel.clientJid);
                }
            }
            String clientName = clientNameMap.get(userActivityModel.clientJid);

            userActivityBuilder.add(createUserActivityFromModel(userActivityModel, username, clientName));
        }

        return new Page<>(userActivityBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public void createUserActivity(String clientJid, String userJid, long time, String log, String ipAddress) {
        UserActivityModel userActivityModel = new UserActivityModel();
        userActivityModel.clientJid = clientJid;
        userActivityModel.time = time;
        userActivityModel.log = log;

        userActivityDao.persist(userActivityModel, userJid, ipAddress);
    }

    @Override
    public boolean login(String usernameOrEmail, String password) {
        try {
            UserModel userModel;
            EmailModel emailModel;
            if (userDao.existByUsername(usernameOrEmail)) {
                userModel = userDao.findByUsername(usernameOrEmail);
                emailModel = emailDao.findByUserJid(userModel.jid);
            } else if (emailDao.existByEmail(usernameOrEmail)) {
                emailModel = emailDao.findByEmail(usernameOrEmail);
                userModel = userDao.findByJid(emailModel.userJid);
            } else {
                return false;
            }

            if ((userModel != null) && (userModel.password.equals(JudgelsUtils.hashSHA256(password))) && (emailModel.emailVerified)) {
                Http.Context.current().session().put("userJid", userModel.jid);
                Http.Context.current().session().put("username", userModel.username);
                Http.Context.current().session().put("name", userModel.name);
                Http.Context.current().session().put("avatar", getAvatarImageUrl(userModel.profilePictureImageName).toString());
                Http.Context.current().session().put("role", userModel.roles);
                return true;
            } else {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public void updateProfile(String userJid, String name) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        Http.Context.current().session().put("name", userModel.name);
    }

    @Override
    public void updateProfile(String userJid, String name, String password) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        Http.Context.current().session().put("name", userModel.name);
    }

    @Override
    public String updateProfilePicture(String userJid, File imageFile, String extension) {
        String newImageName = IdentityUtils.getUserJid() + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        List<String> filePath = ImmutableList.of(newImageName);
        avatarFileProvider.uploadFile(ImmutableList.of(), imageFile, newImageName);
        avatarFileProvider.makeFilePublic(filePath);

        UserModel userModel = userDao.findByJid(userJid);
        userModel.profilePictureImageName = newImageName;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        return newImageName;
    }

    @Override
    public boolean activateEmail(String emailCode) {
        if (emailDao.isExistByCode(emailCode)) {
            EmailModel emailModel = emailDao.findByCode(emailCode);
            if (!emailModel.emailVerified) {
                emailModel.emailVerified = true;

                emailDao.edit(emailModel, emailModel.userJid, IdentityUtils.getIpAddress());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String forgotPassword(String username, String email) {
        UserModel userModel = userDao.findByUsername(username);

        String code = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
        ForgotPasswordModel forgotPasswordModel = new ForgotPasswordModel();
        forgotPasswordModel.userJid = userModel.jid;
        forgotPasswordModel.code = code;
        forgotPasswordModel.used = false;

        forgotPasswordDao.persist(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());
        return code;
    }

    @Override
    public boolean existForgotPassByCode(String code) {
        return forgotPasswordDao.isExistByCode(code);
    }

    @Override
    public void changePassword(String code, String password) {
        ForgotPasswordModel forgotPasswordModel = forgotPasswordDao.findByCode(code);
        forgotPasswordModel.used = true;

        forgotPasswordDao.edit(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());

        UserModel userModel = userDao.findByJid(forgotPasswordModel.userJid);
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
    }

    @Override
    public String getAvatarImageUrlString(String imageName) {
        return avatarFileProvider.getURL(ImmutableList.of(imageName));
    }

    private User createPublicUserFromModels(UserModel userModel) {
        return new User(userModel.jid, userModel.username, userModel.name, getAvatarImageUrl(userModel.profilePictureImageName));
    }

    private User createUserFromModels(UserModel userModel, EmailModel emailModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    private UserActivity createUserActivityFromModel(UserActivityModel userActivityModel, String username, String clientName) {
        return new UserActivity(userActivityModel.id, userActivityModel.time, userActivityModel.userCreate, username, userActivityModel.clientJid, clientName, userActivityModel.log, userActivityModel.ipCreate);
    }

    private URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.routes.UserController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
