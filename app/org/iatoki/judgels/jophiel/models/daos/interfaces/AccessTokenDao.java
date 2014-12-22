package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;

public interface AccessTokenDao extends Dao<Long, AccessTokenModel> {

    AccessTokenModel findByCode(String code);

}
