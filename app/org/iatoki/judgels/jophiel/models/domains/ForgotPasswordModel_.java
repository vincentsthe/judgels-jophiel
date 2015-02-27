package org.iatoki.judgels.jophiel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ForgotPasswordModel.class)
public abstract class ForgotPasswordModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<ForgotPasswordModel, Long> id;
	public static volatile SingularAttribute<ForgotPasswordModel, String> userJid;
	public static volatile SingularAttribute<ForgotPasswordModel, String> code;
    public static volatile SingularAttribute<ForgotPasswordModel, Boolean> used;

}

