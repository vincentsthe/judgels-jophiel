package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;

public final class ClientHibernateDao extends AbstractJudgelsHibernateDao<ClientModel> implements ClientDao {

    @Override
    public long countByFilter(String filterString) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ClientModel> root = query.from(ClientModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get("name"), "%" + filterString + "%"));
        predicates.add(cb.like(root.get("applicationType"), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query
            .select(cb.count(root))
            .where(condition);

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ClientModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ClientModel> query = cb.createQuery(ClientModel.class);
        Root<ClientModel> root = query.from(ClientModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get("id"));
        selection.add(root.get("jid"));
        selection.add(root.get("name"));
        selection.add(root.get("applicationType"));
        selection.add(root.get("scopes"));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get("name"), "%" + filterString + "%"));
        predicates.add(cb.like(root.get("applicationType"), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
            .multiselect(selection)
            .where(condition)
            .orderBy(orderBy);

        return JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();
    }
}
