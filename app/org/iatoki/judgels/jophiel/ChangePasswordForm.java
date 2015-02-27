package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class ChangePasswordForm {
    @Constraints.Required
    public String password;

    @Constraints.Required
    public String confirmPassword;

}
