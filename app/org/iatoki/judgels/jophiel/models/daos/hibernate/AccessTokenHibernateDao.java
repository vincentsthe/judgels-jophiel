package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class AccessTokenHibernateDao extends AbstractHibernateDao<Long, AccessTokenModel> implements AccessTokenDao {

    @Override
    public AccessTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AccessTokenModel> query = cb.createQuery(AccessTokenModel.class);

        Root<AccessTokenModel> root =  query.from(AccessTokenModel.class);

        query.where(cb.equal(root.get("code"), code));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
