package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;

import java.util.Collection;
import java.util.List;

public interface UserEmailDao extends Dao<Long, UserEmailModel> {

    boolean isExistByEmail(String email);

    boolean isExistNotVerifiedByUserJid(String userJid);

    UserEmailModel findByUserJid(String userJid);

    List<String> findUserJidsByFilter(String filterString);

    List<String> findUserJidsWithUnverifiedEmail();

    List<String> sortUserJidsByEmail(Collection<String> userJids, String sortBy, String order);

    List<UserEmailModel> findBySetOfUserJids(Collection<String> userJidSet, long first, long max);

    UserEmailModel findByEmail(String email);

    boolean isExistByCode(String emailCode);

    UserEmailModel findByCode(String emailCode);

}
