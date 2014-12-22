package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_redirect_uri")
public class RedirectURIModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String clientJid;

    public String redirectURI;

    public RedirectURIModel() {

    }


}
