package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class AddEmailForm {
    @Constraints.Required
    public String email;

}