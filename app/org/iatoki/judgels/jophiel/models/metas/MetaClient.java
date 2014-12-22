package org.iatoki.judgels.jophiel.models.metas;

import org.iatoki.judgels.commons.models.metas.MetaAbstractJudgelsModel;
import org.iatoki.judgels.jophiel.GrantType;
import org.iatoki.judgels.jophiel.Scope;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.List;
import java.util.Set;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ClientModel.class)
public abstract class MetaClient extends MetaAbstractJudgelsModel {

    public static final String CLIENT_SECRET= "clientSecret";

    public static volatile SingularAttribute<UserModel, String> clientId;
    public static volatile SingularAttribute<UserModel, String> clientSecret;
    public static volatile SingularAttribute<UserModel, GrantType> grantType;
    public static volatile SingularAttribute<UserModel, List<RedirectURIModel>> redirectURIs;
    public static volatile SingularAttribute<UserModel, Set<Scope>> scopes;

}

