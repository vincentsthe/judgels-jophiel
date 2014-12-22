package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_client")
public class ClientModel extends AbstractJudgelsModel {

    public String name;

    public String secret;

    public String applicationType;

    public String scopes;

    public ClientModel() {

    }

    public ClientModel(long id, String jid, String name, String applicationType, String scopes) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.applicationType = applicationType;
        this.scopes = scopes;
    }
}
