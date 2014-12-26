package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class LoginForm {
    @Constraints.Required
    public String usernameOrEmail;

    @Constraints.Required
    public String password;

}
