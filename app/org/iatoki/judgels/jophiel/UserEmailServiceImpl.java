package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.Play;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;

public final class UserEmailServiceImpl implements UserEmailService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;

    public UserEmailServiceImpl(UserDao userDao, UserEmailDao userEmailDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
    }

    @Override
    public boolean isEmailOwnedByUser(String email, String username) {
        UserModel userModel = userDao.findByUsername(username);
        UserEmailModel emailModel = userEmailDao.findByEmail(email);

        return (emailModel.userJid.equals(userModel.jid));
    }

    @Override
    public boolean existByEmail(String email) {
        return userEmailDao.isExistByEmail(email);
    }

    @Override
    public boolean activateEmail(String emailCode) {
        if (userEmailDao.isExistByCode(emailCode)) {
            UserEmailModel emailModel = userEmailDao.findByCode(emailCode);
            if (!emailModel.emailVerified) {
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
    public boolean isEmailNotVerified(String userJid) {
        return userEmailDao.isExistNotVerifiedByUserJid(userJid);
    }

    @Override
    public String getEmailCodeOfUnverifiedEmail(String userJid) {
        UserEmailModel userEmailModel = userEmailDao.findByUserJid(userJid);
        return userEmailModel.emailCode;
    }

    @Override
    public void sendActivationEmail(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(Play.application().configuration().getString("application.copyright") + " " + Messages.get("registrationEmail.userRegistration"));
        mail.setFrom(Play.application().configuration().getString("email.name") + " <" + Play.application().configuration().getString("email.email") + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml("<p>" + Messages.get("registrationEmail.thankYou") + " " + Play.application().configuration().getString("application.copyright") + ".</p><p>" + Messages.get("registrationEmail.pleaseActivate") + " <a href='" + link + "'>here</a>.</p>");
        MailerPlugin.send(mail);
    }

    @Override
    public void sendChangePasswordEmail(String email, String link) {
        Email mail = new Email();
        mail.setSubject(Play.application().configuration().getString("application.copyright") + " " + Messages.get("forgotPasswordEmail.forgotPassword"));
        mail.setFrom(Play.application().configuration().getString("email.name") + " <" + Play.application().configuration().getString("email.email") + ">");
        mail.addTo(email);
        mail.setBodyHtml("<p>" + Messages.get("forgotPasswordEmail.request") + " " + Play.application().configuration().getString("application.copyright") + ".</p><p>" + Messages.get("forgotPasswordEmail.changePassword") + " <a href='" + link + "'>here</a>.</p>");
        MailerPlugin.send(mail);
    }
}
