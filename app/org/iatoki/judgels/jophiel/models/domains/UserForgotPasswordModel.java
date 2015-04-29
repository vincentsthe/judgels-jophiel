package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_forgot_password")
public final class UserForgotPasswordModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String code;

    public boolean used;

    public UserForgotPasswordModel() {

    }

}
