package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

import java.util.List;

public final class ClientUpdateForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String redirectURIs;

    @Constraints.Required
    public List<String> scopes;

}
