package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_email")
public final class EmailModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String email;

    public boolean emailVerified;

    public String emailCode;

    public EmailModel() {

    }

    public EmailModel(String email, String emailCode) {
        this.email = email;
        this.emailVerified = false;
        this.emailCode = emailCode;
    }

    public EmailModel(long id, String userJid, String email) {
        this.id = id;
        this.userJid = userJid;
        this.email = email;
    }

    public EmailModel(String email, boolean emailVerified) {
        this.email = email;
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return email;
    }

}
