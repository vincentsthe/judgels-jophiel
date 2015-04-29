package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_phone")
public final class UserPhoneModel extends AbstractModel {

    @Id
    public long id;

    public String userJid;

    public String phoneNumber;

    public boolean phoneNumberVerified;

    public UserPhoneModel() {

    }

    public UserPhoneModel(long id, String phoneNumber, boolean phoneNumberVerified) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.phoneNumberVerified = phoneNumberVerified;
    }

}
