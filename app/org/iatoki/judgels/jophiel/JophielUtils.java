package org.iatoki.judgels.jophiel;

import org.apache.commons.lang3.StringUtils;
import play.mvc.Http;

import java.util.Arrays;
import java.util.List;

public final class JophielUtils {

    private JophielUtils() {
        // prevent instantiation
    }

    public static void saveRoleInSession(List<String> roles) {
        Http.Context.current().session().put("role", StringUtils.join(roles, ","));
    }

    public static boolean hasRole(String role) {
        return Arrays.asList(Http.Context.current().session().get("role").split(",")).contains(role);
    }
}
