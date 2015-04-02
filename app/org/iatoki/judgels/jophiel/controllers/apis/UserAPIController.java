package org.iatoki.judgels.jophiel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.AutoComplete;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public final class UserAPIController extends Controller {

    private final UserService userService;

    public UserAPIController(UserService userService) {
        this.userService = userService;
    }

    public Result preUserAutocompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "GET");    // Only allow POST
        response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");         // Ensure this header is also allowed!
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result userAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        User user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        String term = form.get("term");
        List<User> users = userService.findAllUserByTerm(term);
        ImmutableList.Builder<AutoComplete> responseBuilder = ImmutableList.builder();

        for (User user1 : users) {
            responseBuilder.add(new AutoComplete(user1.getJid(), user1.getUsername(), user1.getUsername() + " (" + user1.getName() + ")"));
        }
        return ok(Json.toJson(responseBuilder.build()));
    }

}
