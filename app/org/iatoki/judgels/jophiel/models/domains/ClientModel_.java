package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ClientModel.class)
public abstract class ClientModel_ extends org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ClientModel, String> name;
	public static volatile SingularAttribute<ClientModel, String> secret;
	public static volatile SingularAttribute<ClientModel, String> applicationType;
	public static volatile SingularAttribute<ClientModel, String> scopes;

}

