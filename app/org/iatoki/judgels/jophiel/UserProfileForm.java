package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class UserProfileForm {

    @Constraints.Required
    public String name;

    public String password;

    public String confirmPassword;
}
