package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.controllers.ApplicationController;
import org.iatoki.judgels.jophiel.controllers.ClientController;
import org.iatoki.judgels.jophiel.controllers.UserAccountController;
import org.iatoki.judgels.jophiel.controllers.UserActivityController;
import org.iatoki.judgels.jophiel.controllers.UserController;
import org.iatoki.judgels.jophiel.controllers.UserEmailController;
import org.iatoki.judgels.jophiel.controllers.UserProfileController;
import org.iatoki.judgels.jophiel.controllers.apis.ClientAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserActivityAPIController;

public class DefaultJophielControllerFactory implements JophielControllerFactory {

    private final JophielServiceFactory jophielServiceFactory;

    public DefaultJophielControllerFactory(JophielServiceFactory jophielServiceFactory) {
        this.jophielServiceFactory = jophielServiceFactory;
    }

    @Override
    public ApplicationController createApplicationController() {
        return new ApplicationController();
    }

    @Override
    public ClientController createClientController() {
        return new ClientController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserActivityService());
    }

    @Override
    public UserAccountController createUserAccountController() {
        return new UserAccountController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserService(), jophielServiceFactory.createUserEmailService(), jophielServiceFactory.createUserAccountService(), jophielServiceFactory.createUserActivityService());
    }

    @Override
    public UserActivityController createUserActivityController() {
        return new UserActivityController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserService(), jophielServiceFactory.createUserActivityService());
    }

    @Override
    public UserController createUserController() {
        return new UserController(jophielServiceFactory.createUserService(), jophielServiceFactory.createUserActivityService());
    }

    @Override
    public UserEmailController createUserEmailController() {
        return new UserEmailController(jophielServiceFactory.createUserService(), jophielServiceFactory.createUserEmailService());
    }

    @Override
    public UserProfileController createUserProfileController() {
        return new UserProfileController(jophielServiceFactory.createUserService(), jophielServiceFactory.createUserProfileService(), jophielServiceFactory.createUserActivityService());
    }

    @Override
    public UserAPIController createUserAPIController() {
        return new UserAPIController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserService(), jophielServiceFactory.createUserProfileService());
    }

    @Override
    public ClientAPIController createClientAPIController() {
        return new ClientAPIController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserService());
    }

    @Override
    public UserActivityAPIController createUserActivityAPIController() {
        return new UserActivityAPIController(jophielServiceFactory.createClientService(), jophielServiceFactory.createUserService(), jophielServiceFactory.createUserActivityService());
    }
}
