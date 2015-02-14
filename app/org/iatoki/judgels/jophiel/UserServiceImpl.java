package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.mvc.Http;

import javax.persistence.NoResultException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class UserServiceImpl implements UserService {

    private UserDao userDao;
    private EmailDao emailDao;

    public UserServiceImpl(UserDao userDao, EmailDao emailDao) {
        this.userDao = userDao;
        this.emailDao = emailDao;
    }

    @Override
    public List<User> findAllUser(String filterString) {
        try {
            List<UserModel> userModels = userDao.findAll(filterString);
            ImmutableList.Builder<User> userBuilder = ImmutableList.builder();

            for (UserModel userRecord : userModels) {
                EmailModel emailRecord  = emailDao.findByUserJid(userRecord.jid);
                userBuilder.add(new User(userRecord.id, userRecord.jid, userRecord.username, userRecord.name, emailRecord.email, new URL(controllers.routes.Assets.at("images/avatar/" + userRecord.profilePictureImageName).absoluteURL(Http.Context.current().request())), Arrays.asList(userRecord.roles.split(","))));
            }

            return userBuilder.build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findUserById(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public User findUserByJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
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
    public boolean existsByJid(String userJid) {
        return (userDao.findByJid(userJid) != null);
    }

    @Override
    public String registerUser(String username, String name, String email, String password) {
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
    }

    @Override
    public void createUser(String username, String name, String email, String password, List<String> roles) {
        roles.add("user");

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
        try {
            List<String> userUserJid = userDao.findUserJidByFilter(filterString);
            List<String> emailUserJid = emailDao.findUserJidByFilter(filterString);

            ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
            setBuilder.addAll(userUserJid);
            setBuilder.addAll(emailUserJid);

            ImmutableSet<String> userJidSet = setBuilder.build();
            long totalPage = userJidSet.size();
            ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

            if (totalPage > 0) {
                List<String> sortedUserJid;
                if (orderBy.equals("email")) {
                    sortedUserJid = emailDao.sortUserJid(userJidSet, orderBy, orderDir);
                } else {
                    sortedUserJid = userDao.sortUserJid(userJidSet, orderBy, orderDir);
                }

                List<UserModel> userModels = userDao.findBySetOfUserJid(sortedUserJid, pageIndex * pageSize, pageSize);
                List<EmailModel> emailModels = emailDao.findBySetOfUserJid(sortedUserJid, pageIndex * pageSize, pageSize);

                for (int i = 0; i < userModels.size(); ++i) {
                    UserModel userModel = userModels.get(i);
                    EmailModel emailModel = emailModels.get(i);
                    listBuilder.add(new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, new URL(controllers.routes.Assets.at("images/avatar/" + userModel.profilePictureImageName).absoluteURL(Http.Context.current().request())), Arrays.asList(userModel.roles.split(","))));
                }
            }

            return new Page<>(listBuilder.build(), totalPage, pageIndex, pageSize);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean login(String usernameOrEmail, String password) {
        try {
            UserModel userModel = userDao.findByUsername(usernameOrEmail);
            EmailModel emailModel;
            if (userModel == null) {
                emailModel = emailDao.findByEmail(usernameOrEmail);
                if (emailModel != null) {
                    userModel = userDao.findByJid(emailModel.userJid);
                }
            } else {
                emailModel = emailDao.findByUserJid(userModel.jid);
            }

            if ((userModel != null) && (userModel.password.equals(JudgelsUtils.hashSHA256(password))) && (emailModel.emailVerified)) {
                Http.Context.current().session().put("userJid", userModel.jid);
                Http.Context.current().session().put("username", userModel.username);
                Http.Context.current().session().put("name", userModel.name);
                Http.Context.current().session().put("avatar", controllers.routes.Assets.at("images/avatar/" + userModel.profilePictureImageName).absoluteURL(Http.Context.current().request()));
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
    public URL updateProfilePicture(String userJid, File imageFile, String extension) {
        try {
            String newImageName = IdentityUtils.getUserJid() + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
            imageFile.renameTo(new File("public/images/avatar/", newImageName));

            UserModel userModel = userDao.findByJid(userJid);
            userModel.profilePictureImageName = newImageName;

            userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            return new URL(controllers.routes.Assets.at("images/avatar/" + newImageName).absoluteURL(Http.Context.current().request()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean activateEmail(String emailCode) {
        if (emailDao.isExistByCode(emailCode)) {
            EmailModel emailModel = emailDao.findByCode(emailCode);
            emailModel.emailVerified = true;

            emailDao.edit(emailModel, emailModel.userJid, IdentityUtils.getIpAddress());
            return true;
        } else {
            return false;
        }
    }

    private User createUserFromModels(UserModel userModel, EmailModel emailModel) {
        try {
            return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, new URL(controllers.routes.Assets.at("images/avatar/" + userModel.profilePictureImageName).absoluteURL(Http.Context.current().request())), Arrays.asList(userModel.roles.split(",")));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
