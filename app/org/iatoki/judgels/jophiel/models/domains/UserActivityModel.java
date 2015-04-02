package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_activity")
public final class UserActivityModel extends AbstractModel {

    @Id
    public long id;

    public String clientJid;

    public long time;

    public String log;

}