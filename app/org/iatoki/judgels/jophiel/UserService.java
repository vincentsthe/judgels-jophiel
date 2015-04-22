package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.commons.UserActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> findAllUserByTerm(String term);

    boolean isEmailOwnedByUser(String email, String username);

    User findUserById(long userId);

    User findUserByUserJid(String userJid);

    User findPublicUserByUserJid(String userJid);

    User findUserByUsername(String username);

    boolean existByUsername(String username);

    boolean existByEmail(String email);

    boolean existsByUserJid(String userJid);

    String registerUser(String username, String name, String email, String password) throws IllegalStateException;

    void createUser(String username, String name, String email, String password, List<String> roles);

    void updateUser(long userId, String username, String name, String email, List<String> roles);

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles);

    void deleteUser(long userId);

    Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UserActivity> pageUserActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, String username);

    Page<UserActivity> pageUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UserActivity> pageUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, Set<String> usernames);

    void createUserActivity(String clientJid, String userJid, long time, String log, String ipAddress);

    User login(String usernameOrEmail, String password);

    void updateProfile(String userJid, String name);

    void updateProfile(String userJid, String name, String password);

    String updateProfilePicture(String userJid, File imageFile, String imageType) throws IOException;

    boolean activateEmail(String emailCode);

    String forgotPassword(String username, String email);

    boolean existForgotPassByCode(String code);

    void changePassword(String code, String password);

    String getAvatarImageUrlString(String imageName);
}
