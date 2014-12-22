package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;

import java.util.List;

public interface RedirectURIDao extends Dao<Long, RedirectURIModel> {

    List<RedirectURIModel> findByClientJid(String clientJid);

}
