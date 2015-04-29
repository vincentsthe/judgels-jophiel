package org.iatoki.judgels.jophiel;

import java.io.File;
import java.io.IOException;

public interface UserProfileService {

    void updateProfile(String userJid, String name);

    String updateProfilePicture(String userJid, File imageFile, String imageType) throws IOException;

    String getAvatarImageUrlString(String imageName);
}
