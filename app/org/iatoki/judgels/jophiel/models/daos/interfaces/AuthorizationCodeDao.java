package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;

public interface AuthorizationCodeDao extends Dao<Long, AuthorizationCodeModel> {

    AuthorizationCodeModel findByCode(String code);

    boolean checkIfAuthorized(String clientJid, String userJid, String scopes);

}
