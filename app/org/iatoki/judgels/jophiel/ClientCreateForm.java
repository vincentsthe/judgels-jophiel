package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

import java.util.List;

public final class ClientCreateForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String applicationType;

    @Constraints.Required
    public String redirectURIs;

    @Constraints.Required
    public List<String> scopes;

}
