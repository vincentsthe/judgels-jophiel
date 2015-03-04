package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import play.Configuration;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class JophielProperties {
    private static JophielProperties INSTANCE;

    private File avatarDir;

    private JophielProperties() {

    }

    public File getAvatarDir() {
        return avatarDir;
    }

    public static JophielProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JophielProperties();

            Configuration conf = Play.application().configuration();

            verifyConfiguration(conf);

            String baseDirName = conf.getString("jophiel.baseDataDir").replaceAll("\"", "");

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
        return INSTANCE;
    }

    private static void verifyConfiguration(Configuration configuration) {
        List<String> requiredKeys = ImmutableList.of(
                "jophiel.baseDataDir"
        );

        for (String key : requiredKeys) {
            if (configuration.getString(key) == null) {
                throw new RuntimeException("Missing " + key + " property in conf/application.conf");
            }
        }
    }
}
