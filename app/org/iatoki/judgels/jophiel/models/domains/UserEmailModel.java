package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_email")
public final class UserEmailModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    @Column(unique = true)
    public String email;

    public boolean emailVerified;

    public String emailCode;

    public UserEmailModel() {

    }

    public UserEmailModel(String email, String emailCode) {
        this.email = email;
        this.emailVerified = false;
        this.emailCode = emailCode;
    }

    public UserEmailModel(long id, String userJid, String email) {
        this.id = id;
        this.userJid = userJid;
        this.email = email;
    }

    public UserEmailModel(String email, boolean emailVerified) {
        this.email = email;
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return email;
    }

}
