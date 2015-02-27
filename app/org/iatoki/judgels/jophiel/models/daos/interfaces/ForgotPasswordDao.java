package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.domains.ForgotPasswordModel;

public interface ForgotPasswordDao extends Dao<Long, ForgotPasswordModel> {

    boolean isExistByCode(String forgotPasswordCode);

    ForgotPasswordModel findByCode(String forgotPasswordCode);

}
