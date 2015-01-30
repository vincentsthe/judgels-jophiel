package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface UserService {

    List<User> findAllUser(String filterString);

    User findUserById(long userId);

    User findUserByJid(String userJid);

    boolean isUserJidExist(String userJid);

    void createUser(String username, String name, String email, String password);

    void updateUser(long userId, String username, String name, String email, String password);

    void deleteUser(long userId);

    Page<User> pageUser(long page, long pageSize, String sortBy, String order, String filterString);

    boolean login(String usernameOrEmail, String password);
}
