package org.iatoki.judgels.jophiel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.ClientModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;

public final class ClientHibernateDao extends AbstractJudgelsHibernateDao<ClientModel> implements ClientDao {

    public ClientHibernateDao() {
        super(ClientModel.class);
    }

    @Override
    public boolean existByName(String clientName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ClientModel> root = query.from(ClientModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ClientModel_.name), clientName));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<String> findClientJidsByNames(Collection<String> clientNames) {
        if (clientNames.isEmpty()) {
            return ImmutableList.of();
        } else {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<String> query = cb.createQuery(String.class);
            Root<ClientModel> root = query.from(ClientModel.class);

            query.select(root.get(ClientModel_.jid)).where(root.get(ClientModel_.name).in(clientNames));

            return JPA.em().createQuery(query).getResultList();
        }
    }

    @Override
    protected List<SingularAttribute<ClientModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ClientModel_.name, ClientModel_.applicationType);
    }
}
