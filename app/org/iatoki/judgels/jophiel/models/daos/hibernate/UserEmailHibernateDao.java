package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserEmailDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel_;
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

public final class UserEmailHibernateDao extends AbstractHibernateDao<Long, UserEmailModel> implements UserEmailDao {

    public UserEmailHibernateDao() {
        super(UserEmailModel.class);
    }

    @Override
    public boolean isExistByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserEmailModel_.email), email));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean isExistNotVerifiedByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserEmailModel_.userJid), userJid), cb.equal(root.get(UserEmailModel_.emailVerified), false)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public UserEmailModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);

        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findUserJidsByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserEmailModel_.email), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query.select(root.get(UserEmailModel_.userJid)).where(condition);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> findUserJidsWithUnverifiedEmail() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(root.get(UserEmailModel_.userJid)).where(cb.equal(root.get(UserEmailModel_.emailVerified), false));
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> sortUserJidsByEmail(Collection<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        Predicate condition = root.get(UserEmailModel_.userJid).in(userJids);

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query.select(root.get(UserEmailModel_.userJid)).where(condition).orderBy(orderBy);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserEmailModel> findBySetOfUserJids(Collection<String> userJids, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get(UserEmailModel_.id));
        selection.add(root.get(UserEmailModel_.userJid));
        selection.add(root.get(UserEmailModel_.email));

        Predicate condition = root.get(UserEmailModel_.userJid).in(userJids);

        CriteriaBuilder.Case<Long> orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get(UserEmailModel_.userJid), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
            .multiselect(selection)
            .where(condition)
            .orderBy(order);

        List<UserEmailModel> list = JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();

        return list;
    }

    @Override
    public UserEmailModel findByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.email), email));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public boolean isExistByCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserEmailModel_.emailCode), emailCode));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public UserEmailModel findByCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.emailCode), emailCode));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
