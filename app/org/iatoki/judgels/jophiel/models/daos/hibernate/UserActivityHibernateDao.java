package org.iatoki.judgels.jophiel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.domains.UserActivityModel;
import org.iatoki.judgels.jophiel.models.domains.UserActivityModel_;

import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

public final class UserActivityHibernateDao extends AbstractHibernateDao<Long, UserActivityModel> implements UserActivityDao {

    public UserActivityHibernateDao() {
        super(UserActivityModel.class);
    }

    @Override
    protected List<SingularAttribute<UserActivityModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(UserActivityModel_.log);
    }
}
