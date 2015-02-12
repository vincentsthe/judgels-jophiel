package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class UserProfileForm {

    public UserProfileForm() {

    }

    public UserProfileForm(User user) {
        this.name = user.getName();
    }

    @Constraints.Required
    public String name;

    public String password;

    public String confirmPassword;

}
