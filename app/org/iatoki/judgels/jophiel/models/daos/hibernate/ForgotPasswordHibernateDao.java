package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.domains.ForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.domains.ForgotPasswordModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class ForgotPasswordHibernateDao extends AbstractHibernateDao<Long, ForgotPasswordModel> implements ForgotPasswordDao {

    public ForgotPasswordHibernateDao() {
        super(ForgotPasswordModel.class);
    }

    @Override
    public boolean isExistByCode(String forgotPasswordCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ForgotPasswordModel> root = query.from(ForgotPasswordModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ForgotPasswordModel_.code), forgotPasswordCode), cb.equal(root.get(ForgotPasswordModel_.used), false)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ForgotPasswordModel findByCode(String forgotPasswordCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForgotPasswordModel> query = cb.createQuery(ForgotPasswordModel.class);
        Root<ForgotPasswordModel> root = query.from(ForgotPasswordModel.class);

        query.where(cb.and(cb.equal(root.get(ForgotPasswordModel_.code), forgotPasswordCode), cb.equal(root.get(ForgotPasswordModel_.used), false)));

        return JPA.em().createQuery(query).getSingleResult();
    }

}
