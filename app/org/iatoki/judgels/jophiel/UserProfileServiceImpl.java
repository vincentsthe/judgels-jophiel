package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.api.Play;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class UserProfileServiceImpl implements UserProfileService {

    private final UserDao userDao;
    private final FileSystemProvider avatarFileProvider;

    public UserProfileServiceImpl(UserDao userDao, FileSystemProvider avatarFileProvider) {
        this.userDao = userDao;
        this.avatarFileProvider = avatarFileProvider;
        if (!avatarFileProvider.fileExists(ImmutableList.of("avatar-default.png"))) {
            try {
                avatarFileProvider.uploadFile(ImmutableList.of(), Play.getFile("default-assets/avatar-default.png", Play.current()), "avatar-default.png");
                avatarFileProvider.makeFilePublic(ImmutableList.of("avatar-default.png"));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create default avatar.");
            }
        }
    }

    @Override
    public void updateProfile(String userJid, String name) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        Http.Context.current().session().put("name", userModel.name);
    }

    @Override
    public String updateProfilePicture(String userJid, File imageFile, String extension) throws IOException {
        String newImageName = IdentityUtils.getUserJid() + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        List<String> filePath = ImmutableList.of(newImageName);
        avatarFileProvider.uploadFile(ImmutableList.of(), imageFile, newImageName);
        avatarFileProvider.makeFilePublic(filePath);

        UserModel userModel = userDao.findByJid(userJid);
        userModel.profilePictureImageName = newImageName;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        return newImageName;
    }

    @Override
    public String getAvatarImageUrlString(String imageName) {
        return avatarFileProvider.getURL(ImmutableList.of(imageName));
    }

}
