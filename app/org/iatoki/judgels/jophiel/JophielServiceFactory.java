package org.iatoki.judgels.jophiel;

public interface JophielServiceFactory {

    ClientService createClientService();

    UserService createUserService();

    UserAccountService createUserAccountService();

    UserActivityService createUserActivityService();

    UserEmailService createUserEmailService();

    UserProfileService createUserProfileService();

}
