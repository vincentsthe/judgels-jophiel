package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class AuthorizationCodeHibernateDao extends AbstractHibernateDao<Long, AuthorizationCodeModel> implements AuthorizationCodeDao {

    @Override
    public AuthorizationCodeModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AuthorizationCodeModel> query = cb.createQuery(AuthorizationCodeModel.class);

        Root<AuthorizationCodeModel> root =  query.from(AuthorizationCodeModel.class);

        query.where(cb.equal(root.get("code"), code));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
