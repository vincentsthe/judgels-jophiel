package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RefreshTokenModel.class)
public abstract class RefreshTokenModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<RefreshTokenModel, Long> id;
	public static volatile SingularAttribute<RefreshTokenModel, String> code;
	public static volatile SingularAttribute<RefreshTokenModel, String> userJid;
	public static volatile SingularAttribute<RefreshTokenModel, String> clientJid;
	public static volatile SingularAttribute<RefreshTokenModel, String> token;
	public static volatile SingularAttribute<RefreshTokenModel, Boolean> redeemed;
	public static volatile SingularAttribute<RefreshTokenModel, String> scopes;
}

