package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.JidPrefix;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user")
@JidPrefix("USER")
public final class UserModel extends AbstractJudgelsModel {

    public String username;

    public String name;

    public String password;

    public UserModel() {

    }

    public UserModel(String username, String name) {
        this.username = username;
        this.name = name;
    }

    public UserModel(long id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

}
