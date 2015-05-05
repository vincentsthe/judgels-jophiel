package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.mvc.Http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public final class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;

    public UserServiceImpl(UserDao userDao, UserEmailDao userEmailDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
    }

    @Override
    public boolean existByUsername(String username) {
        return userDao.existByUsername(username);
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        return userDao.existsByJid(userJid);
    }

    @Override
    public List<User> findAllUserByTerm(String term) {
        List<UserModel> userModels = userDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<User> userBuilder = ImmutableList.builder();

        for (UserModel userRecord : userModels) {
            UserEmailModel emailRecord  = userEmailDao.findByUserJid(userRecord.jid);
            userBuilder.add(new User(userRecord.id, userRecord.jid, userRecord.username, userRecord.name, emailRecord.email, getAvatarImageUrl(userRecord.profilePictureImageName), Arrays.asList(userRecord.roles.split(","))));
        }

        return userBuilder.build();
    }

    @Override
    public Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> userUserJid = userDao.findUserJidsByFilter(filterString);
        List<String> emailUserJid = userEmailDao.findUserJidsByFilter(filterString);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJid);
        setBuilder.addAll(emailUserJid);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalRow = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalRow > 0) {
            List<String> sortedUserJids;
            if (orderBy.equals("email")) {
                sortedUserJids = userEmailDao.sortUserJidsByEmail(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJids = userDao.sortUserJidsByUserAttribute(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.findBySetOfUserJids(sortedUserJids, pageIndex * pageSize, pageSize);
            List<UserEmailModel> userEmailModels = userEmailDao.findBySetOfUserJids(sortedUserJids, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                UserEmailModel userEmailModel = userEmailModels.get(i);
                listBuilder.add(new User(userModel.id, userModel.jid, userModel.username, userModel.name, userEmailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(","))));
            }
        }

        return new Page<>(listBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<User> pageUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> unverifiedEmailUserJids = userEmailDao.findUserJidsWithUnverifiedEmail();
        List<String> userUserJids = userDao.findUserJidsByFilter(filterString);
        List<String> emailUserJids = userEmailDao.findUserJidsByFilter(filterString);

        userUserJids.retainAll(unverifiedEmailUserJids);
        emailUserJids.retainAll(unverifiedEmailUserJids);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJids);
        setBuilder.addAll(emailUserJids);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalRow = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalRow > 0) {
            List<String> sortedUserJid;
            if (orderBy.equals("email")) {
                sortedUserJid = userEmailDao.sortUserJidsByEmail(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJid = userDao.sortUserJidsByUserAttribute(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.findBySetOfUserJids(sortedUserJid, pageIndex * pageSize, pageSize);
            List<UserEmailModel> emailModels = userEmailDao.findBySetOfUserJids(sortedUserJid, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                UserEmailModel emailModel = emailModels.get(i);
                listBuilder.add(new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(","))));
            }
        }

        return new Page<>(listBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel != null) {
            UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

            return createUserFromModels(userModel, emailModel);
        } else {
            throw new UserNotFoundException("User not found.");
        }
    }

    @Override
    public User findUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public User findPublicUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        return createPublicUserFromModels(userModel);
    }

    @Override
    public User findUserByUsername(String username) {
        UserModel userModel = userDao.findByUsername(username);
        UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public void createUser(String username, String name, String email, String password, List<String> roles) {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UserEmailModel emailModel = new UserEmailModel(email, true);
        emailModel.userJid = userModel.jid;

        userEmailDao.persist(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, List<String> roles) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel != null) {
            UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

            userModel.username = username;
            userModel.name = name;
            userModel.roles = StringUtils.join(roles, ",");

            userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            emailModel.email = email;

            userEmailDao.edit(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            throw new UserNotFoundException("User not found.");
        }
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, String password, List<String> roles) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel != null) {
            UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

            userModel.username = username;
            userModel.name = name;
            userModel.password = JudgelsUtils.hashSHA256(password);
            userModel.roles = StringUtils.join(roles, ",");

            userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            emailModel.email = email;

            userEmailDao.edit(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            throw new UserNotFoundException("User not found.");
        }
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        UserEmailModel emailModel = userEmailDao.findByUserJid(userModel.jid);

        userEmailDao.remove(emailModel);
        userDao.remove(userModel);
    }

    private User createPublicUserFromModels(UserModel userModel) {
        return new User(userModel.jid, userModel.username, userModel.name, getAvatarImageUrl(userModel.profilePictureImageName));
    }

    private User createUserFromModels(UserModel userModel, UserEmailModel emailModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    private URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
