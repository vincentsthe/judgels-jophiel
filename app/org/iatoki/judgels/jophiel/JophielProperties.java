package org.iatoki.judgels.jophiel;

import com.amazonaws.services.s3.model.Region;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class JophielProperties {
    private static JophielProperties INSTANCE;

    private File avatarDir;
    private String baseURL;
    private String idTokenPrivateKey;
    private String awsAvatarAccessKey;
    private String awsAvatarSecretKey;
    private String awsAvatarS3BucketName;
    private Region awsAvatarS3BucketRegion;
    private String awsAvatarCloudFrontURL;
    private boolean awsAvatarS3UseKeyCredentials;
    private boolean awsAvatarUse;

    private JophielProperties() {
        awsAvatarS3UseKeyCredentials = false;
        awsAvatarUse = false;
    }

    public File getAvatarDir() {
        return avatarDir;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getIdTokenPrivateKey() {
        return idTokenPrivateKey;
    }

    public String getAwsAvatarAccessKey() {
        if (awsAvatarUse) {
            return awsAvatarAccessKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAwsAvatarSecretKey() {
        if (awsAvatarUse) {
            return awsAvatarSecretKey;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAwsAvatarS3BucketName() {
        if (awsAvatarUse) {
            return awsAvatarS3BucketName;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Region getAwsAvatarS3BucketRegion() {
        if (awsAvatarUse) {
            return awsAvatarS3BucketRegion;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAwsAvatarCloudFrontURL() {
        if (awsAvatarUse) {
            return awsAvatarCloudFrontURL;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isAwsAvatarUse() {
        return awsAvatarUse;
    }

    public boolean isAwsAvatarS3UseKeyCredentials() {
        return awsAvatarS3UseKeyCredentials;
    }

    public synchronized static void buildInstance(Config config) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        } else {
            INSTANCE = new JophielProperties();

            verifyConfiguration(config);

            String baseDirName = config.getString("jophiel.baseDataDir");

            if (INSTANCE.isAwsAvatarUse()) {
                if (INSTANCE.isAwsAvatarS3UseKeyCredentials()) {
                    INSTANCE.awsAvatarAccessKey = config.getString("aws.avatar.s3.key.access");
                    INSTANCE.awsAvatarSecretKey = config.getString("aws.avatar.s3.key.secret");
                }

                INSTANCE.awsAvatarS3BucketName = config.getString("aws.avatar.s3.bucketName");
                INSTANCE.awsAvatarS3BucketRegion = Region.fromValue(config.getString("aws.avatar.s3.bucketRegionId"));
                INSTANCE.awsAvatarCloudFrontURL = config.getString("aws.avatar.cloudFront.url");
            }

            INSTANCE.baseURL = config.getString("jophiel.baseUrl");
            INSTANCE.idTokenPrivateKey = config.getString("jophiel.idToken.key.private");

            File baseDir = new File(baseDirName);
            if (!baseDir.isDirectory()) {
                throw new RuntimeException("jophiel.baseDataDir: " + baseDirName + " does not exist");
            }

            try {
                INSTANCE.avatarDir = new File(baseDir, "avatar");
                FileUtils.forceMkdir(INSTANCE.avatarDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create folder inside " + baseDir.getAbsolutePath());
            }

        }
    }

    public static JophielProperties getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        } else {
            return INSTANCE;
        }
    }

    private static void verifyConfiguration(Config config) {
        List<String> requiredKeys = Lists.newArrayList(
              "jophiel.baseDataDir",
              "jophiel.baseUrl",
              "jophiel.idToken.key.private",
              "aws.avatar.use",
              "aws.avatar.s3.credentials.useKey"
        );

        if (config.getBoolean("aws.avatar.use")) {
            INSTANCE.awsAvatarUse = true;
            if (config.getBoolean("aws.avatar.s3.credentials.useKey")) {
                INSTANCE.awsAvatarS3UseKeyCredentials = true;
                requiredKeys.add("aws.avatar.s3.key.access");
                requiredKeys.add("aws.avatar.s3.key.secret");
            }
            requiredKeys.add("aws.avatar.s3.bucketName");
            requiredKeys.add("aws.avatar.s3.bucketRegionId");
        }

        for (String key : requiredKeys) {
            if (config.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
