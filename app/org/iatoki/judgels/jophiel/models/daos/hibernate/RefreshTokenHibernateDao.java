package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class RefreshTokenHibernateDao extends AbstractHibernateDao<Long, RefreshTokenModel> implements RefreshTokenDao {

    @Override
    public RefreshTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public RefreshTokenModel findByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.token), token));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
