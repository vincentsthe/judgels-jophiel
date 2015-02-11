package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import play.mvc.Http;

import javax.persistence.NoResultException;
import java.util.List;

public final class UserServiceImpl implements UserService {

    private UserDao userDao;
    private EmailDao emailDao;

    public UserServiceImpl(UserDao userDao, EmailDao emailDao) {
        this.userDao = userDao;
        this.emailDao = emailDao;
    }

    @Override
    public List<User> findAllUser(String filterString) {
        List<UserModel> userModels = userDao.findAll(filterString);
        ImmutableList.Builder<User> userBuilder = ImmutableList.builder();

        for (UserModel userRecord : userModels) {
            EmailModel emailRecord  = emailDao.findByUserJid(userRecord.jid);
            userBuilder.add(new User(userRecord.id, userRecord.jid, userRecord.username, userRecord.name, emailRecord.email));
        }

        return userBuilder.build();
    }

    @Override
    public User findUserById(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public User findUserByJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        return createUserFromModels(userModel, emailModel);
    }

    @Override
    public boolean existsByJid(String userJid) {
        return (userDao.findByJid(userJid) != null);
    }

    @Override
    public void createUser(String username, String name, String email, String password) {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.persist(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        EmailModel emailModel = new EmailModel(email);
        emailModel.userJid = userModel.jid;

        emailDao.persist(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, String password) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        userModel.username = username;
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        emailModel.email = email;

        emailDao.edit(emailModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        emailDao.remove(emailModel);
        userDao.remove(userModel);
    }

    @Override
    public Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> userUserJid = userDao.findUserJidByFilter(filterString);
        List<String> emailUserJid = emailDao.findUserJidByFilter(filterString);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJid);
        setBuilder.addAll(emailUserJid);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalPage = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalPage > 0) {
            List<String> sortedUserJid;
            if (orderBy.equals("email")) {
                sortedUserJid = emailDao.sortUserJid(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJid = userDao.sortUserJid(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.findBySetOfUserJid(sortedUserJid, pageIndex * pageSize, pageSize);
            List<EmailModel> emailModels = emailDao.findBySetOfUserJid(sortedUserJid, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                EmailModel emailModel = emailModels.get(i);
                listBuilder.add(new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email));
            }
        }

        return new Page<>(listBuilder.build(), totalPage, pageIndex, pageSize);
    }

    @Override
    public boolean login(String usernameOrEmail, String password) {
        try {
            UserModel userModel = userDao.findByUsername(usernameOrEmail);

            if (userModel == null) {
                EmailModel emailModel = emailDao.findByEmail(usernameOrEmail);
                if (emailModel != null) {
                    userModel = userDao.findByJid(emailModel.userJid);
                }
            }

            if ((userModel != null) && (userModel.password.equals(JudgelsUtils.hashSHA256(password)))) {
                Http.Context.current().session().put("userJid", userModel.jid);
                return true;
            } else {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        }
    }

    private User createUserFromModels(UserModel userModel, EmailModel emailModel) {
            return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email);
    }
}
