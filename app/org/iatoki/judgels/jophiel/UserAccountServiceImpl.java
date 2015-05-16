package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.mvc.Http;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class UserAccountServiceImpl implements UserAccountService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;
    private final UserForgotPasswordDao userForgotPasswordDao;

    public UserAccountServiceImpl(UserDao userDao, UserEmailDao userEmailDao, UserForgotPasswordDao userForgotPasswordDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
        this.userForgotPasswordDao = userForgotPasswordDao;
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
            UserEmailModel emailModel = new UserEmailModel(email, emailCode);
            emailModel.userJid = userModel.jid;

            userEmailDao.persist(emailModel, "guest", IdentityUtils.getIpAddress());

            return emailCode;
        } catch (ConstraintViolationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String forgotPassword(String username, String email) {
        UserModel userModel = userDao.findByUsername(username);

        String code = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
        UserForgotPasswordModel forgotPasswordModel = new UserForgotPasswordModel();
        forgotPasswordModel.userJid = userModel.jid;
        forgotPasswordModel.code = code;
        forgotPasswordModel.used = false;

        userForgotPasswordDao.persist(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());
        return code;
    }

    @Override
    public boolean existForgotPassByCode(String code) {
        return userForgotPasswordDao.isExistByCode(code);
    }

    @Override
    public void changePassword(String code, String password) {
        UserForgotPasswordModel forgotPasswordModel = userForgotPasswordDao.findByCode(code);
        forgotPasswordModel.used = true;

        userForgotPasswordDao.edit(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());

        UserModel userModel = userDao.findByJid(forgotPasswordModel.userJid);
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
    }

    @Override
    public User login(String usernameOrEmail, String password) throws UserNotFoundException, EmailNotVerifiedException{
        try {
            UserModel userModel;
            UserEmailModel userEmailModel = null;
            if (userDao.existByUsername(usernameOrEmail)) {
                userModel = userDao.findByUsername(usernameOrEmail);
                userEmailModel = userEmailDao.findByEmail(userModel.primaryEmail);
            } else if (userEmailDao.isExistByEmail(usernameOrEmail)) {
                List<UserEmailModel> userEmailModelList = userEmailDao.findAllByEmail(usernameOrEmail);
                for (UserEmailModel emailRecord : userEmailModelList) {
                    if (emailRecord.emailVerified) {
                        userEmailModel = emailRecord;
                    }
                }
                if (userEmailModel == null) {
                    throw new EmailNotVerifiedException();
                }
                userModel = userDao.findByJid(userEmailModel.userJid);
            } else {
                throw new UserNotFoundException();
            }

            if (userModel.password.equals(JudgelsUtils.hashSHA256(password))) {
                if (userEmailModel.emailVerified) {
                    return createUserFromModels(userModel, userEmailModel);
                } else {
                    throw new EmailNotVerifiedException();
                }
            } else {
                return null;
            }
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }

    @Override
    public void updatePassword(String userJid, String password) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private User createUserFromModels(UserModel userModel, UserEmailModel emailModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    private URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


}
