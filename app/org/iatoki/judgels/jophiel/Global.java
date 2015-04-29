package org.iatoki.judgels.jophiel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.controllers.ApplicationController;
import org.iatoki.judgels.jophiel.controllers.ClientController;
import org.iatoki.judgels.jophiel.controllers.UserActivityController;
import org.iatoki.judgels.jophiel.controllers.UserController;
import org.iatoki.judgels.jophiel.controllers.apis.ClientAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserActivityAPIController;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AccessTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AuthorizationCodeHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ClientHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.EmailHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ForgotPasswordHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.IdTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RedirectURIHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RefreshTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserActivityHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import play.Application;
import play.Play;
import play.mvc.Controller;

import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {
    private static final String CONF_LOCATION = "conf/application.conf";

    private AccessTokenDao accessTokenDao;
    private AuthorizationCodeDao authorizationCodeDao;
    private ClientDao clientDao;
    private EmailDao emailDao;
    private ForgotPasswordDao forgotPasswordDao;
    private IdTokenDao idTokenDao;
    private RedirectURIDao redirectURIDao;
    private RefreshTokenDao refreshTokenDao;
    private UserActivityDao userActivityDao;
    private UserDao userDao;

    private JophielProperties jophielProps;

    private FileSystemProvider avatarFileProvider;

    private UserService userService;
    private ClientService clientService;

    private Map<Class<?>, Controller> controllersCache;

    @Override
    public void onStart(Application application) {
        buildDaos();
        buildProperties();
        buildFileProviders();
        buildServices();
        buildControllers();
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        @SuppressWarnings("unchecked")
        A controller = (A) controllersCache.get(controllerClass);

        return controller;
    }
    
    private void buildDaos() {
        accessTokenDao = new AccessTokenHibernateDao();
        authorizationCodeDao = new AuthorizationCodeHibernateDao();
        clientDao = new ClientHibernateDao();
        emailDao = new EmailHibernateDao();
        forgotPasswordDao = new ForgotPasswordHibernateDao();
        idTokenDao = new IdTokenHibernateDao();
        redirectURIDao = new RedirectURIHibernateDao();
        refreshTokenDao = new RefreshTokenHibernateDao();
        userActivityDao = new UserActivityHibernateDao();
        userDao = new UserHibernateDao();
    }

    private void buildProperties() {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), Play.application().configuration(), CONF_LOCATION);

        JophielProperties.buildInstance(Play.application().configuration(), CONF_LOCATION);
        jophielProps = JophielProperties.getInstance();
    }

    private void buildFileProviders() {
        if (jophielProps.isAvatarUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (jophielProps.isAvatarAWSS3PermittedByIAMRoles()) {
                awsS3Client = new AmazonS3Client();
            } else {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(jophielProps.getAvatarAWSAccessKey(), jophielProps.getAvatarAWSSecretKey()));
            }
            avatarFileProvider = new AWSFileSystemProvider(awsS3Client, jophielProps.getAvatarAWSS3BucketName(), jophielProps.getAvatarAWSCloudFrontUrl(), jophielProps.getAvatarAWSS3BucketRegion());
        } else {
            avatarFileProvider = new LocalFileSystemProvider(jophielProps.getAvatarLocalDir());
        }
    }

    private void buildServices() {
        userService = new UserServiceImpl(userDao, emailDao, forgotPasswordDao, userActivityDao, clientDao, avatarFileProvider);
        clientService = new ClientServiceImpl(clientDao, redirectURIDao, authorizationCodeDao, accessTokenDao, refreshTokenDao, idTokenDao);
    }

    private void buildControllers() {
        controllersCache = ImmutableMap.<Class<?>, Controller> builder()
                .put(ApplicationController.class, new ApplicationController())
                .put(ClientController.class, new ClientController(clientService, userService))
                .put(UserActivityController.class, new UserActivityController(clientService, userService))
                .put(UserController.class, new UserController(clientService, userService))
                .put(ClientAPIController.class, new ClientAPIController(clientService, userService))
                .put(UserActivityAPIController.class, new UserActivityAPIController(clientService, userService))
                .put(UserAPIController.class, new UserAPIController(clientService, userService))
                .build();
    }
}