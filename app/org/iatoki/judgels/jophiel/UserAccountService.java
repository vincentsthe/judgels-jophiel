package org.iatoki.judgels.jophiel;

import javax.persistence.NoResultException;

public interface UserAccountService {

    String registerUser(String username, String name, String email, String password) throws IllegalStateException;

    String forgotPassword(String username, String email);

    boolean existForgotPassByCode(String code);

    void changePassword(String code, String password);

    User login(String usernameOrEmail, String password) throws NoResultException, UserNotFoundException, EmailNotVerifiedException ;

    void updatePassword(String userJid, String password);


}
