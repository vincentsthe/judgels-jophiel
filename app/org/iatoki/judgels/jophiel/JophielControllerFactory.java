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

public interface JophielControllerFactory {

    ApplicationController createApplicationController();

    ClientController createClientController();

    UserAccountController createUserAccountController();

    UserActivityController createUserActivityController();

    UserController createUserController();

    UserEmailController createUserEmailController();

    UserProfileController createUserProfileController();

    UserAPIController createUserAPIController();

    ClientAPIController createClientAPIController();

    UserActivityAPIController createUserActivityAPIController();

}
