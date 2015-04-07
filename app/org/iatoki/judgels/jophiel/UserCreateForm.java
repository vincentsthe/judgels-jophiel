package org.iatoki.judgels.jophiel;

import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

public final class UserCreateForm {

    public UserCreateForm() {

    }

    public UserCreateForm(User user) {
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.roles = StringUtils.join(user.getRoles(), ",");
    }

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
    public String roles;
}
