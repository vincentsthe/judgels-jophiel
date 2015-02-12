package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.Page;

import java.io.File;
import java.net.URL;
import java.util.List;

public interface UserService {

    List<User> findAllUser(String filterString);

    User findUserById(long userId);

    User findUserByJid(String userJid);

    boolean existsByJid(String userJid);

    String registerUser(String username, String name, String email, String password);

    void createUser(String username, String name, String email, String password, List<String> roles);

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles);

    void deleteUser(long userId);

    Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    boolean login(String usernameOrEmail, String password);

    void updateProfile(String userJid, String name);

    void updateProfile(String userJid, String name, String password);

    URL updateProfilePicture(String userJid, File imageFile, String imageType);

    boolean activateEmail(String emailCode);
}
