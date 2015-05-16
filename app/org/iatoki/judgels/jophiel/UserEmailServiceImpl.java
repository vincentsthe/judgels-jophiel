package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.Play;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Http;

import java.util.List;
import java.util.UUID;

public final class UserEmailServiceImpl implements UserEmailService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;

    public UserEmailServiceImpl(UserDao userDao, UserEmailDao userEmailDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
    }

    @Override
    public boolean isEmailOwnedByUser(long emailId, String username) {
        UserModel userModel = userDao.findByUsername(username);
        UserEmailModel emailModel = userEmailDao.findById(emailId);

        return (emailModel.userJid.equals(userModel.jid));
    }

    @Override
    public boolean isEmailOwnedByUser(String email, String username) {
        UserModel userModel = userDao.findByUsername(username);
        List<UserEmailModel> userEmailList = userEmailDao.findAllByEmail(email);

        boolean ownedByUser = false;
        for (UserEmailModel userEmailModel : userEmailList) {
            if (userEmailModel.emailVerified && (userEmailModel.userJid == userModel.jid)) {
                ownedByUser = true;
            }
        }

        return ownedByUser;
    }

    @Override
    public boolean existByEmail(String email) {
        return userEmailDao.isExistByEmail(email);
    }

    @Override
    public boolean activatedEmailExist(String email) {
        return userEmailDao.isExistByVerifiedEmail(email);
    }

    @Override
    public boolean activateEmail(String emailCode) {
        if (userEmailDao.isExistByCode(emailCode)) {
            UserEmailModel emailModel = userEmailDao.findByCode(emailCode);
            if (!emailModel.emailVerified && !activatedEmailExist(emailModel.email)) {
                emailModel.emailVerified = true;

                userEmailDao.edit(emailModel, emailModel.userJid, IdentityUtils.getIpAddress());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public UserEmail findEmailById(long id) {
        UserEmailModel userEmailModel = userEmailDao.findById(id);

        return new UserEmail(userEmailModel.id, userEmailModel.email, userEmailModel.emailVerified);
    }

    @Override
    public String getEmailCodeOfUnverifiedEmail(String email) {
        UserEmailModel userEmailModel = userEmailDao.findByEmail(email);
        return userEmailModel.emailCode;
    }

    @Override
    public String createUserEmail(String userJid, String email) {
        String emailCode = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
        UserEmailModel userEmailModel = new UserEmailModel(email, emailCode);
        userEmailModel.userJid = userJid;
        userEmailDao.persist(userEmailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return emailCode;
    }

    @Override
    public void deleteEmail(long emailId) {
        UserEmailModel userEmailModel = userEmailDao.findById(emailId);
        userEmailDao.remove(userEmailModel);
    }

    @Override
    public void sendActivationEmail(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(JudgelsProperties.getInstance().getAppCopyright() + " " + Messages.get("registrationEmail.userRegistration"));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml("<p>" + Messages.get("registrationEmail.thankYou") + " " + JudgelsProperties.getInstance().getAppCopyright() + ".</p><p>" + Messages.get("registrationEmail.pleaseActivate") + " <a href='" + link + "'>here</a>.</p>");
        MailerPlugin.send(mail);
    }

    @Override
    public void sendChangePasswordEmail(String email, String link) {
        Email mail = new Email();
        mail.setSubject(Play.application().configuration().getString("application.copyright") + " " + Messages.get("forgotPasswordEmail.forgotPassword"));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(email);
        mail.setBodyHtml("<p>" + Messages.get("forgotPasswordEmail.request") + " " + JudgelsProperties.getInstance().getAppCopyright() + ".</p><p>" + Messages.get("forgotPasswordEmail.changePassword") + " <a href='" + link + "'>here</a>.</p>");
        MailerPlugin.send(mail);
    }

    @Override
    public void resendVerification(long emailId) {
        UserEmailModel userEmailModel = userEmailDao.findById(emailId);
        Email mail = new Email();
        mail.setSubject(play.Play.application().configuration().getString("application.sub-title") + " " + Messages.get("registrationEmail.userRegistration"));
        mail.setFrom(play.Play.application().configuration().getString("email.name") + " <" + play.Play.application().configuration().getString("email.email") + ">");
        mail.addTo(IdentityUtils.getUserRealName() + " <" + userEmailModel.email + ">");
        mail.setBodyHtml("<p>" + Messages.get("registrationEmail.thankYou") + " " + play.Play.application().configuration().getString("application.sub-title") + ".</p><p>" + Messages.get("registrationEmail.pleaseActivate") + " <a href='" + org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(userEmailModel.emailCode).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()) + "'>here</a>.</p>");
        MailerPlugin.send(mail);
    }
}
