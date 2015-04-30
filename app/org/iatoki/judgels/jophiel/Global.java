package org.iatoki.judgels.jophiel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.*;
import org.iatoki.judgels.jophiel.controllers.*;
import org.iatoki.judgels.jophiel.controllers.apis.ClientAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserActivityAPIController;
import play.Application;
import play.mvc.Controller;

import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {
    private final Map<Class<?>, Controller> controllersRegistry;

    public Global() {
        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);

        FileSystemProvider avatarProvider;
        if (JophielProperties.getInstance().isAvatarUsingAWSS3()) {
            AmazonS3Client s3Client;
            if (JophielProperties.getInstance().isAvatarAWSUsingKeys()) {
                s3Client = new AmazonS3Client(new BasicAWSCredentials(JophielProperties.getInstance().getAvatarAWSAccessKey(), JophielProperties.getInstance().getAvatarAWSSecretKey()));
            } else {
                s3Client = new AmazonS3Client();
            }
            avatarProvider = new AWSFileSystemProvider(s3Client, JophielProperties.getInstance().getAvatarAWSS3BucketName(), JophielProperties.getInstance().getAvatarAWSCloudFrontUrl(), JophielProperties.getInstance().getAvatarAWSS3BucketRegion());
        } else {
            avatarProvider = new LocalFileSystemProvider(JophielProperties.getInstance().getAvatarLocalDir());
        }
        JophielControllerFactory jophielControllerFactory = new DefaultJophielControllerFactory(new DefaultJophielServiceFactory(new HibernateJophielDaoFactory(), avatarProvider));

        controllersRegistry = ImmutableMap.<Class<?>, Controller> builder()
                .put(ApplicationController.class, jophielControllerFactory.createApplicationController())
                .put(ClientController.class, jophielControllerFactory.createClientController())
                .put(UserAccountController.class, jophielControllerFactory.createUserAccountController())
                .put(UserActivityController.class, jophielControllerFactory.createUserActivityController())
                .put(UserController.class, jophielControllerFactory.createUserController())
                .put(UserEmailController.class, jophielControllerFactory.createUserEmailController())
                .put(UserProfileController.class, jophielControllerFactory.createUserProfileController())
                .put(UserAPIController.class, jophielControllerFactory.createUserAPIController())
                .put(ClientAPIController.class, jophielControllerFactory.createClientAPIController())
                .put(UserActivityAPIController.class, jophielControllerFactory.createUserActivityAPIController())
                .build();
    }

    @Override
    public void onStart(Application application) {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!controllersRegistry.containsKey(controllerClass)) {
            return super.getControllerInstance(controllerClass);
        } else {
            return controllerClass.cast(controllersRegistry.get(controllerClass));
        }
    }

}