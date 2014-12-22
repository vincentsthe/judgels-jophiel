package org.iatoki.judgels.jophiel;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

import java.util.List;

public final class ClientUpdateForm {

    public ClientUpdateForm() {

    }

    public ClientUpdateForm(Client client) {
        this.name = client.getName();
        this.redirectURIs = StringUtils.join(client.getRedirectURIs(), ",");
        this.scopes = Lists.newArrayList(client.getScopes());
    }

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String redirectURIs;

    @Constraints.Required
    public List<String> scopes;

}
