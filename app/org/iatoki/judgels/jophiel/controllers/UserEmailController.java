package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.centerLayout;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmailService;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.activationView;
import play.db.jpa.Transactional;
import play.mvc.Result;

@Transactional
public final class UserEmailController extends BaseController {

    private final UserService userService;
    private final UserEmailService userEmailService;

    public UserEmailController(UserService userService, UserEmailService userEmailService) {
        this.userService = userService;
        this.userEmailService = userEmailService;
    }

    public Result verifyEmail(String emailCode) {
        if (userEmailService.activateEmail(emailCode)) {
            LazyHtml content = new LazyHtml(activationView.render());
            content.appendLayout(c -> centerLayout.render(c));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Verify Email");
            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result resendEmailVerification(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        if (userEmailService.isEmailNotVerified(user.getJid())) {
            String code = userEmailService.getEmailCodeOfUnverifiedEmail(user.getJid());
            userEmailService.sendActivationEmail(user.getName(), user.getEmail(), org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(code).absoluteURL(request(), request().secure()));

            return redirect(routes.UserController.viewUnverifiedUsers());
        } else {
            return forbidden();
        }
    }
}
