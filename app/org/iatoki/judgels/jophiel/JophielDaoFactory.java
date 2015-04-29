package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserForgotPasswordDao;

public interface JophielDaoFactory {

    AccessTokenDao createAccessTokenDao();

    AuthorizationCodeDao createAuthorizationCodeDao();

    ClientDao createClientDao();

    IdTokenDao createIdTokenDao();

    RedirectURIDao createRedirectURIDao();

    RefreshTokenDao createRefreshTokenDao();

    UserActivityDao createUserActivityDao();

    UserDao createUserDao();

    UserEmailDao createUserEmailDao();

    UserForgotPasswordDao createUserForgotPasswordDao();
}
