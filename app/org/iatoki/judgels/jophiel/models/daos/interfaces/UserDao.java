package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.jophiel.models.domains.UserModel;

import java.util.Collection;
import java.util.List;

public interface UserDao extends JudgelsDao<UserModel> {

    List<UserModel> findAll(String filterString);

    List<String> findUserJidByFilter(String filterString);

    List<String> sortUserJid(Collection<String> userJids, String sortBy, String order);

    List<UserModel> findBySetOfUserJid(Collection<String> userJids, long first, long max);

    UserModel findByUsername(String username);

}
