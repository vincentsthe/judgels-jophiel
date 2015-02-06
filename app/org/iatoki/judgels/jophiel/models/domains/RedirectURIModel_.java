package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RedirectURIModel.class)
public abstract class RedirectURIModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<RedirectURIModel, Long> id;
	public static volatile SingularAttribute<RedirectURIModel, String> clientJid;
	public static volatile SingularAttribute<RedirectURIModel, String> redirectURI;
}

