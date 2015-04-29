package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserEmailModel.class)
public abstract class UserEmailModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<UserEmailModel, Long> id;
	public static volatile SingularAttribute<UserEmailModel, String> userJid;
	public static volatile SingularAttribute<UserEmailModel, String> email;
	public static volatile SingularAttribute<UserEmailModel, Boolean> emailVerified;
    public static volatile SingularAttribute<UserEmailModel, String> emailCode;

}

