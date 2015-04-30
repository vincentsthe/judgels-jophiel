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
import play.Play;
import play.mvc.Controller;

import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {
    private static final String CONF_LOCATION = "conf/application.conf";

    private final Map<Class, Controller> controllerRegistry;

    public Global() {
        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);

        FileSystemProvider avatarProvider;
        if (JophielProperties.getInstance().isAwsAvatarUse()) {
            AmazonS3Client s3Client;
            if (JophielProperties.getInstance().isAwsAvatarS3UseKeyCredentials()) {
                s3Client = new AmazonS3Client(new BasicAWSCredentials(JophielProperties.getInstance().getAwsAvatarAccessKey(), JophielProperties.getInstance().getAwsAvatarSecretKey()));
            } else {
                s3Client = new AmazonS3Client();
            }
            avatarProvider = new AWSFileSystemProvider(s3Client, JophielProperties.getInstance().getAwsAvatarS3BucketName(), JophielProperties.getInstance().getAwsAvatarCloudFrontURL(), JophielProperties.getInstance().getAwsAvatarS3BucketRegion());
        } else {
            avatarProvider = new LocalFileSystemProvider(JophielProperties.getInstance().getAvatarDir());
        }
        JophielControllerFactory jophielControllerFactory = new DefaultJophielControllerFactory(new DefaultJophielServiceFactory(new HibernateJophielDaoFactory(), avatarProvider));

        ImmutableMap.Builder<Class, Controller> controllerRegistryBuilder = ImmutableMap.builder();
        controllerRegistryBuilder.put(ApplicationController.class, jophielControllerFactory.createApplicationController());
        controllerRegistryBuilder.put(ClientController.class, jophielControllerFactory.createClientController());
        controllerRegistryBuilder.put(UserAccountController.class, jophielControllerFactory.createUserAccountController());
        controllerRegistryBuilder.put(UserActivityController.class, jophielControllerFactory.createUserActivityController());
        controllerRegistryBuilder.put(UserController.class, jophielControllerFactory.createUserController());
        controllerRegistryBuilder.put(UserEmailController.class, jophielControllerFactory.createUserEmailController());
        controllerRegistryBuilder.put(UserProfileController.class, jophielControllerFactory.createUserProfileController());
        controllerRegistryBuilder.put(UserAPIController.class, jophielControllerFactory.createUserAPIController());
        controllerRegistryBuilder.put(ClientAPIController.class, jophielControllerFactory.createClientAPIController());
        controllerRegistryBuilder.put(UserActivityAPIController.class, jophielControllerFactory.createUserActivityAPIController());

        controllerRegistry = controllerRegistryBuilder.build();
    }

    @Override
    public void onStart(Application application) {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), Play.application().configuration(), CONF_LOCATION);

        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!controllerRegistry.containsKey(controllerClass)) {
            return super.getControllerInstance(controllerClass);
        } else {
            return controllerClass.cast(controllerRegistry.get(controllerClass));
        }
    }

}