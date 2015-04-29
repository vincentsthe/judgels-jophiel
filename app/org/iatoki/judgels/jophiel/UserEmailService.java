package org.iatoki.judgels.jophiel;

public interface UserEmailService {

    boolean isEmailOwnedByUser(String email, String username);

    boolean existByEmail(String email);

    boolean activateEmail(String emailCode);

    boolean isEmailNotVerified(String userJid);

    String getEmailCodeOfUnverifiedEmail(String userJid);

    void sendActivationEmail(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);
}
