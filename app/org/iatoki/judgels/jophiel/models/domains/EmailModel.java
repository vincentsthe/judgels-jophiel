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

    public EmailModel() {

    }

    public EmailModel(String email) {
        this.email = email;
        this.emailVerified = false;
    }

    public EmailModel(String userJid, String email) {
        this(email);
        this.userJid = userJid;
    }

    public EmailModel(long id, String email, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return email;
    }

}
