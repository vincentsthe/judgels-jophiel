package org.iatoki.judgels.jophiel;

import com.amazonaws.services.s3.model.Region;
import org.apache.commons.io.FileUtils;
import play.Configuration;

import java.io.File;
import java.io.IOException;

public final class JophielProperties {
    private static JophielProperties INSTANCE;

    private final Configuration conf;
    private final String confLocation;

    private String jophielBaseUrl;
    private File jophielBaseDataDir;

    private String idTokenPrivateKey;

    private String noreplyName;
    private String noreplyEmail;

    private boolean avatarAWSS3Use;
    private File avatarLocalDir;
    private String avatarAWSAccessKey;
    private String avatarAWSSecretKey;
    private String avatarAWSS3BucketName;
    private Region avatarAWSS3BucketRegion;
    private boolean avatarAWSS3PermittedByIAMRoles;
    private String avatarAWSCloudFrontUrl;

    public String getJophielBaseUrl() {
        return jophielBaseUrl;
    }

    public String getIdTokenPrivateKey() {
        return idTokenPrivateKey;
    }

    public String getNoreplyName() {
        return noreplyName;
    }

    public String getNoreplyEmail() {
        return noreplyEmail;
    }

    public boolean isAvatarUsingAWSS3() {
        return avatarAWSS3Use;
    }

    public File getAvatarLocalDir() {
        if (isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is using AWS S3");
        }
        return avatarLocalDir;
    }

    public String getAvatarAWSAccessKey() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSAccessKey;
    }

    public String getAvatarAWSSecretKey() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSSecretKey;
    }

    public String getAvatarAWSS3BucketName() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSS3BucketName;
    }

    public Region getAvatarAWSS3BucketRegion() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSS3BucketRegion;
    }

    public boolean isAvatarAWSS3PermittedByIAMRoles() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSS3PermittedByIAMRoles;
    }

    public String getAvatarAWSCloudFrontUrl() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSCloudFrontUrl;
    }

    private JophielProperties(Configuration conf, String confLocation) {
        this.conf = conf;
        this.confLocation = confLocation;
    }

    public static synchronized void buildInstance(Configuration conf, String confLocation) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JophielProperties instance has already been built");
        }

        INSTANCE = new JophielProperties(conf, confLocation);
        INSTANCE.build();
    }

    public static JophielProperties getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JophielProperties instance has not been built");
        }
        return INSTANCE;
    }

    private void build() {
        jophielBaseUrl = requireStringValue("jophiel.baseUrl");
        jophielBaseDataDir = requireDirectoryValue("jophiel.baseDataDir");

        idTokenPrivateKey = requireStringValue("jophiel.idToken.privateKey");

        noreplyName = requireStringValue("noreply.name");
        noreplyEmail = requireStringValue("noreply.email");

        requireStringValue("smtp.host");
        requireIntegerValue("smtp.port");
        requireStringValue("smtp.ssl");
        requireStringValue("smtp.user");
        requireStringValue("smtp.password");

        avatarAWSS3Use = requireBooleanValue("avatar.aws.s3.use");

        if (avatarAWSS3Use) {
            avatarAWSAccessKey = requireStringValue("avatar.aws.accessKey");
            avatarAWSSecretKey = requireStringValue("avatar.aws.secretKey");
            avatarAWSS3BucketName = requireStringValue("avatar.aws.s3.bucket.name");
            avatarAWSS3BucketRegion = Region.fromValue(requireStringValue("avatar.aws.s3.bucket.regionId"));
            avatarAWSS3PermittedByIAMRoles = requireBooleanValue("avatar.aws.s3.permittedByIAMRoles");
            avatarAWSCloudFrontUrl = requireStringValue("avatar.aws.cloudFront.baseUrl");
        } else {
            try {
                avatarLocalDir = new File(jophielBaseDataDir, "teamAvatar");
                FileUtils.forceMkdir(avatarLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getStringValue(String key) {
        return conf.getString(key);
    }

    private String requireStringValue(String key) {
        String value = getStringValue(key);
        if (value == null) {
            throw new RuntimeException("Missing " + key + " property in " + confLocation);
        }
        return value;
    }

    private Integer getIntegerValue(String key) {
        return conf.getInt(key);
    }

    private int requireIntegerValue(String key) {
        Integer value = getIntegerValue(key);
        if (value == null) {
            throw new RuntimeException("Missing " + key + " property in " + confLocation);
        }
        return value;
    }

    private Boolean getBooleanValue(String key) {
        return conf.getBoolean(key);
    }

    private boolean requireBooleanValue(String key) {
        Boolean value = getBooleanValue(key);
        if (value == null) {
            throw new RuntimeException("Missing " + key + " property in " + confLocation);
        }
        return value;
    }

    private File requireDirectoryValue(String key) {
        String filename = getStringValue(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
        return dir;
    }
}
