package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.jophiel.models.domains.UserModel;

import java.util.Collection;
import java.util.List;

public interface UserDao extends JudgelsDao<UserModel> {

    boolean existByUsername(String username);

    List<String> findUserJidsByUsernames(Collection<String> usernames);

    List<String> findUserJidsByFilter(String filterString);

    List<String> sortUserJids(Collection<String> userJids, String sortBy, String order);

    List<UserModel> findBySetOfUserJids(Collection<String> userJids, long first, long max);

    UserModel findByUsername(String username);

}
