package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.domains.UserActivityModel;

public final class UserActivityHibernateDao extends AbstractHibernateDao<Long, UserActivityModel> implements UserActivityDao {

    public UserActivityHibernateDao() {
        super(UserActivityModel.class);
    }

}
