package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.UserForgotPasswordModel;

public interface UserForgotPasswordDao extends Dao<Long, UserForgotPasswordModel> {

    boolean isExistByCode(String forgotPasswordCode);

    UserForgotPasswordModel findByCode(String forgotPasswordCode);

}
