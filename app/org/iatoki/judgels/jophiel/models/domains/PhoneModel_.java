package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PhoneModel.class)
public abstract class PhoneModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<PhoneModel, Long> id;
	public static volatile SingularAttribute<PhoneModel, String> userJid;
	public static volatile SingularAttribute<PhoneModel, String> phoneNumber;
	public static volatile SingularAttribute<PhoneModel, Boolean> phoneNumberVerified;
}

