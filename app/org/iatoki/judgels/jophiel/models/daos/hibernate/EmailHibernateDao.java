package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import org.iatoki.judgels.jophiel.models.domains.EmailModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class EmailHibernateDao extends AbstractHibernateDao<Long, EmailModel> implements EmailDao {

    public EmailHibernateDao() {
        super(EmailModel.class);
    }

    @Override
    public EmailModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);

        Root<EmailModel> root = query.from(EmailModel.class);

        query.where(cb.equal(root.get(EmailModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findUserJidByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(EmailModel_.email), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query.select(root.get(EmailModel_.userJid)).where(condition);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> sortUserJid(Collection<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        Predicate condition = root.get(EmailModel_.userJid).in(userJids);

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query.select(root.get(EmailModel_.userJid)).where(condition).orderBy(orderBy);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<EmailModel> findBySetOfUserJid(Collection<String> userJids, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get(EmailModel_.id));
        selection.add(root.get(EmailModel_.userJid));
        selection.add(root.get(EmailModel_.email));

        Predicate condition = root.get(EmailModel_.userJid).in(userJids);

        CriteriaBuilder.Case<Long> orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get(EmailModel_.userJid), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
            .multiselect(selection)
            .where(condition)
            .orderBy(order);

        List<EmailModel> list = JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();

        return list;
    }

    @Override
    public EmailModel findByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        query.where(cb.equal(root.get(EmailModel_.email), email));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean isExistByCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(EmailModel_.emailCode), emailCode));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public EmailModel findByCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<EmailModel> query = cb.createQuery(EmailModel.class);
        Root<EmailModel> root = query.from(EmailModel.class);

        query.where(cb.equal(root.get(EmailModel_.emailCode), emailCode));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
