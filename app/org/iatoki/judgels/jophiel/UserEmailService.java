package org.iatoki.judgels.jophiel;

public interface UserEmailService {

    boolean isEmailOwnedByUser(long emailId, String username);

    boolean isEmailOwnedByUser(String email, String username);

    boolean existByEmail(String email);

    boolean activatedEmailExist(String email);

    boolean activateEmail(String emailCode);

    UserEmail findEmailById(long id);

    String getEmailCodeOfUnverifiedEmail(String email);

    String createUserEmail(String userJid, String email);

    void deleteEmail(long emailId);

    void sendActivationEmail(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);

    void resendVerification(long emailId);
}
