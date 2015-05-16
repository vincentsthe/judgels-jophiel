package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface UserService {

    boolean existByUsername(String username);

    boolean existsByUserJid(String userJid);

    List<User> findAllUserByTerm(String term);

    Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<User> pageUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByUserJid(String userJid);

    User findPublicUserByUserJid(String userJid);

    User findUserByUsername(String username);

    UserEmail findUserPrimaryEmail(String userJid);

    void createUser(String username, String name, String email, String password, List<String> roles);

    void changeUserPrimaryEmail(String userJid, String email);

    void updateUser(long userId, String username, String name, String email, List<String> roles) throws UserNotFoundException;

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles)  throws UserNotFoundException;

    void deleteUser(long userId);
}
