package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;

import java.util.Collection;
import java.util.List;

public interface EmailDao extends Dao<Long, EmailModel> {

    EmailModel findByUserJid(String userJid);

    List<String> findUserJidByFilter(String filterString);

    List<String> sortUserJid(Collection<String> userJids, String sortBy, String order);

    List<EmailModel> findBySetOfUserJid(Collection<String> userJidSet, long first, long max);

    EmailModel findByEmail(String email);

}
