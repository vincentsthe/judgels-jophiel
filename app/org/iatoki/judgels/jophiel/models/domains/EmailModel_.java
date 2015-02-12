package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EmailModel.class)
public abstract class EmailModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<EmailModel, Long> id;
	public static volatile SingularAttribute<EmailModel, String> userJid;
	public static volatile SingularAttribute<EmailModel, String> email;
	public static volatile SingularAttribute<EmailModel, Boolean> emailVerified;
    public static volatile SingularAttribute<EmailModel, String> emailCode;

}

