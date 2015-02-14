package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.controllers.ClientController;
import org.iatoki.judgels.jophiel.controllers.OpenIdConnectController;
import org.iatoki.judgels.jophiel.controllers.UserController;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AccessTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AuthorizationCodeHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ClientHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.EmailHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.IdTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RedirectURIHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RefreshTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import play.Application;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {

    private Map<Class, Controller> cache;

    @Override
    public void onStart(Application application) {
        cache = new HashMap<>();

        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!cache.containsKey(controllerClass)) {
            if (controllerClass.equals(UserController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                UserController userController = new UserController(userService, clientService);
                cache.put(UserController.class, userController);
            } else if (controllerClass.equals(ClientController.class)) {
                ClientService clientService = createClientService();

                ClientController clientController = new ClientController(clientService);
                cache.put(ClientController.class, clientController);
            } else if (controllerClass.equals(OpenIdConnectController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                OpenIdConnectController openIdConnectController = new OpenIdConnectController(userService, clientService);
                cache.put(OpenIdConnectController.class, openIdConnectController);
            }
        }
        return controllerClass.cast(cache.get(controllerClass));
    }

    private UserService createUserService() {
        UserDao userDao = new UserHibernateDao();
        EmailDao emailDao = new EmailHibernateDao();

        UserService userService = new UserServiceImpl(userDao, emailDao);

        return userService;
    }

    private ClientService createClientService() {
        ClientDao clientDao = new ClientHibernateDao();
        RedirectURIDao redirectURIDao = new RedirectURIHibernateDao();
        AuthorizationCodeDao authorizationCodeDao = new AuthorizationCodeHibernateDao();
        AccessTokenDao accessTokenDao = new AccessTokenHibernateDao();
        RefreshTokenDao refreshTokenDao = new RefreshTokenHibernateDao();
        IdTokenDao idTokenDao = new IdTokenHibernateDao();

        ClientService clientService = new ClientServiceImpl(clientDao, redirectURIDao, authorizationCodeDao, accessTokenDao, refreshTokenDao, idTokenDao);

        return clientService;
    }

}