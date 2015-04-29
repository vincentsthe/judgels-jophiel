package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.daos.hibernate.AccessTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AuthorizationCodeHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ClientHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.IdTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RedirectURIHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RefreshTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserActivityHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserEmailHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserForgotPasswordHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserHibernateDao;
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

public final class HibernateJophielDaoFactory implements JophielDaoFactory {

    @Override
    public AccessTokenDao createAccessTokenDao() {
        return new AccessTokenHibernateDao();
    }

    @Override
    public AuthorizationCodeDao createAuthorizationCodeDao() {
        return new AuthorizationCodeHibernateDao();
    }

    @Override
    public ClientDao createClientDao() {
        return new ClientHibernateDao();
    }

    @Override
    public IdTokenDao createIdTokenDao() {
        return new IdTokenHibernateDao();
    }

    @Override
    public RedirectURIDao createRedirectURIDao() {
        return new RedirectURIHibernateDao();
    }

    @Override
    public RefreshTokenDao createRefreshTokenDao() {
        return new RefreshTokenHibernateDao();
    }

    @Override
    public UserActivityDao createUserActivityDao() {
        return new UserActivityHibernateDao();
    }

    @Override
    public UserDao createUserDao() {
        return new UserHibernateDao();
    }

    @Override
    public UserEmailDao createUserEmailDao() {
        return new UserEmailHibernateDao();
    }

    @Override
    public UserForgotPasswordDao createUserForgotPasswordDao() {
        return new UserForgotPasswordHibernateDao();
    }
}
