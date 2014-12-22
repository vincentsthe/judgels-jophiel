package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.IdTokenModel;

public interface IdTokenDao extends Dao<Long, IdTokenModel> {

    IdTokenModel findByCode(String code);

}
