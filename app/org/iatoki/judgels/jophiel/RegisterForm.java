package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class RegisterForm {
    @Constraints.Required
    @Constraints.MinLength(3)
    @Constraints.MaxLength(20)
    @Constraints.Pattern("^[a-zA-Z0-9\\._]+$")
    public String username;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.Email
    public String email;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public String confirmPassword;


}
