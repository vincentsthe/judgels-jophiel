package org.iatoki.judgels.jophiel.models.metas;

import org.iatoki.judgels.commons.models.metas.MetaAbstractJudgelsModel;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EmailModel.class)
public abstract class MetaEmail extends MetaAbstractJudgelsModel {

    public static final String EMAIL = "email";

    public static volatile SingularAttribute<EmailModel, Long> id;
    public static volatile SingularAttribute<EmailModel, String> userJid;
    public static volatile SingularAttribute<EmailModel, String> email;
    public static volatile SingularAttribute<EmailModel, Boolean> emailVerified;


}

