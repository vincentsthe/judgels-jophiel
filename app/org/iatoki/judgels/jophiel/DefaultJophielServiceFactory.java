package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.FileSystemProvider;

public class DefaultJophielServiceFactory implements JophielServiceFactory {

    private final JophielDaoFactory jophielDaoFactory;
    private final FileSystemProvider fileSystemProvider;

    public DefaultJophielServiceFactory(JophielDaoFactory jophielDaoFactory, FileSystemProvider fileSystemProvider) {
        this.jophielDaoFactory = jophielDaoFactory;
        this.fileSystemProvider = fileSystemProvider;
    }

    @Override
    public ClientService createClientService() {
        return new ClientServiceImpl(jophielDaoFactory.createClientDao(), jophielDaoFactory.createRedirectURIDao(), jophielDaoFactory.createAuthorizationCodeDao(), jophielDaoFactory.createAccessTokenDao(), jophielDaoFactory.createRefreshTokenDao(), jophielDaoFactory.createIdTokenDao());
    }

    @Override
    public UserService createUserService() {
        return new UserServiceImpl(jophielDaoFactory.createUserDao(), jophielDaoFactory.createUserEmailDao());
    }

    @Override
    public UserAccountService createUserAccountService() {
        return new UserAccountServiceImpl(jophielDaoFactory.createUserDao(), jophielDaoFactory.createUserEmailDao(), jophielDaoFactory.createUserForgotPasswordDao());
    }

    @Override
    public UserActivityService createUserActivityService() {
        return new UserActivityServiceImpl(jophielDaoFactory.createClientDao(), jophielDaoFactory.createUserDao(), jophielDaoFactory.createUserActivityDao());
    }


    @Override
    public UserEmailService createUserEmailService() {
        return new UserEmailServiceImpl(jophielDaoFactory.createUserDao(), jophielDaoFactory.createUserEmailDao());
    }

    @Override
    public UserProfileService createUserProfileService() {
        return new UserProfileServiceImpl(jophielDaoFactory.createUserDao(), fileSystemProvider);
    }
}
