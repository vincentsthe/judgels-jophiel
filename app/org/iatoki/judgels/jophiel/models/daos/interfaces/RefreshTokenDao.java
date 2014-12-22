package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;

public interface RefreshTokenDao extends Dao<Long, RefreshTokenModel> {

    RefreshTokenModel findByCode(String code);

    RefreshTokenModel findByToken(String token);

}
