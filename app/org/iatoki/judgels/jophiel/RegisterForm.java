package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class RegisterForm {
    @Constraints.Required
    @Constraints.MinLength(4)
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
