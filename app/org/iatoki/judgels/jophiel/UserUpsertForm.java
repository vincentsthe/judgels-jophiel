package org.iatoki.judgels.jophiel;

import play.data.validation.Constraints;

public final class UserUpsertForm {

    public UserUpsertForm() {

    }

    public UserUpsertForm(User user) {
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String email;

    @Constraints.Required
    public String password;

    @Constraints.Required
    public String confirmPassword;

}
