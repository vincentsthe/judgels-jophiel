package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class RedirectURIHibernateDao extends AbstractHibernateDao<Long, RedirectURIModel> implements RedirectURIDao {

    @Override
    public List<RedirectURIModel> findByClientJid(String clientJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RedirectURIModel> query = cb.createQuery(RedirectURIModel.class);

        Root<RedirectURIModel> root = query.from(RedirectURIModel.class);

        query.where(cb.equal(root.get(RedirectURIModel_.clientJid), clientJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
