package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.commons.UserActivity;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.*;

import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UserActivityServiceImpl implements UserActivityService {

    private final ClientDao clientDao;
    private final UserDao userDao;
    private final UserActivityDao userActivityDao;

    public UserActivityServiceImpl(ClientDao clientDao, UserDao userDao, UserActivityDao userActivityDao) {
        this.clientDao = clientDao;
        this.userDao = userDao;
        this.userActivityDao = userActivityDao;
    }

    @Override
    public Page<UserActivity> pageUserActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, String username) {
        List<String> clientJids = clientDao.findClientJidsByNames(clientsNames);
        String userJid = userDao.findByUsername(username).jid;

        ImmutableMap.Builder<SingularAttribute<? super UserActivityModel, String>, List<String>> inBuilder = ImmutableMap.builder();
        if (!clientJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.clientJid, clientJids);
        }

        long totalRow = userActivityDao.countByFilters(filterString, ImmutableMap.of(UserModel_.userCreate, userJid), inBuilder.build());
        List<UserActivityModel> userActivityModels = userActivityDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(UserModel_.userCreate, userJid), inBuilder.build(), pageIndex * pageSize, pageSize);

        Map<String, String> clientNameMap = Maps.newHashMap();

        ImmutableList.Builder<UserActivity> userActivityBuilder = ImmutableList.builder();
        for (UserActivityModel userActivityModel : userActivityModels) {
            if (!clientNameMap.containsKey(userActivityModel.clientJid)) {
                if (clientDao.existsByJid(userActivityModel.clientJid)) {
                    ClientModel clientModel = clientDao.findByJid(userActivityModel.clientJid);
                    clientNameMap.put(clientModel.jid, clientModel.name);
                } else {
                    clientNameMap.put(userActivityModel.clientJid, userActivityModel.clientJid);
                }
            }
            String clientName = clientNameMap.get(userActivityModel.clientJid);

            userActivityBuilder.add(createUserActivityFromModel(userActivityModel, username, clientName));
        }

        return new Page<>(userActivityBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<UserActivity> pageUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, Set<String> usernames) {
        List<String> clientJids = clientDao.findClientJidsByNames(clientsNames);
        List<String> userJids = userDao.findUserJidsByUsernames(usernames);

        ImmutableMap.Builder<SingularAttribute<? super UserActivityModel, String>, List<String>> inBuilder = ImmutableMap.builder();
        if (!clientJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.clientJid, clientJids);
        }
        if (!userJids.isEmpty()) {
            inBuilder.put(UserActivityModel_.userCreate, userJids);
        }

        long totalRow = userActivityDao.countByFilters(filterString, ImmutableMap.of(), inBuilder.build());
        List<UserActivityModel> userActivityModels = userActivityDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), inBuilder.build(), pageIndex * pageSize, pageSize);

        Map<String, String> usernameMap = Maps.newHashMap();
        Map<String, String> clientNameMap = Maps.newHashMap();

        ImmutableList.Builder<UserActivity> userActivityBuilder = ImmutableList.builder();
        for (UserActivityModel userActivityModel : userActivityModels) {
            if (!usernameMap.containsKey(userActivityModel.userCreate)) {
                if (userDao.existsByJid(userActivityModel.userCreate)) {
                    UserModel userModel = userDao.findByJid(userActivityModel.userCreate);
                    usernameMap.put(userModel.jid, userModel.username);
                } else {
                    usernameMap.put(userActivityModel.userCreate, userActivityModel.userCreate);
                }
            }
            String username = usernameMap.get(userActivityModel.userCreate);

            if (!clientNameMap.containsKey(userActivityModel.clientJid)) {
                if (clientDao.existsByJid(userActivityModel.clientJid)) {
                    ClientModel clientModel = clientDao.findByJid(userActivityModel.clientJid);
                    clientNameMap.put(clientModel.jid, clientModel.name);
                } else {
                    clientNameMap.put(userActivityModel.clientJid, userActivityModel.clientJid);
                }
            }
            String clientName = clientNameMap.get(userActivityModel.clientJid);

            userActivityBuilder.add(createUserActivityFromModel(userActivityModel, username, clientName));
        }

        return new Page<>(userActivityBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public void createUserActivity(String clientJid, String userJid, long time, String log, String ipAddress) {
        UserActivityModel userActivityModel = new UserActivityModel();
        userActivityModel.clientJid = clientJid;
        userActivityModel.time = time;
        userActivityModel.log = log;

        userActivityDao.persist(userActivityModel, userJid, ipAddress);
    }

    private UserActivity createUserActivityFromModel(UserActivityModel userActivityModel, String username, String clientName) {
        return new UserActivity(userActivityModel.id, userActivityModel.time, userActivityModel.userCreate, username, userActivityModel.clientJid, clientName, userActivityModel.log, userActivityModel.ipCreate);
    }
}
