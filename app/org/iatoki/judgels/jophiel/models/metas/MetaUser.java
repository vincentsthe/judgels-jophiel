package org.iatoki.judgels.jophiel.models.metas;

import org.iatoki.judgels.commons.models.metas.MetaAbstractJudgelsModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserModel.class)
public abstract class MetaUser extends MetaAbstractJudgelsModel {

    public static volatile SingularAttribute<UserModel, String> username;
    public static volatile SingularAttribute<UserModel, String> name;
    public static volatile SingularAttribute<UserModel, String> password;

}

