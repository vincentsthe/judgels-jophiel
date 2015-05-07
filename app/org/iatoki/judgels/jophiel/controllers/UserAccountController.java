package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.*;
import org.iatoki.judgels.jophiel.*;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.*;
import org.iatoki.judgels.jophiel.views.html.user.*;
import play.Logger;
import play.Play;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
public final class UserAccountController extends BaseController {

    private final ClientService clientService;
    private final UserService userService;
    private final UserEmailService userEmailService;
    private final UserAccountService userAccountService;
    private final UserActivityService userActivityService;

    public UserAccountController(ClientService clientService, UserService userService, UserEmailService userEmailService, UserAccountService userAccountService, UserActivityService userActivityService) {
        this.clientService = clientService;
        this.userService = userService;
        this.userEmailService = userEmailService;
        this.userAccountService = userAccountService;
        this.userActivityService = userActivityService;
    }

    @AddCSRFToken
    public Result register() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<RegisterForm> form = Form.form(RegisterForm.class);
            return showRegister(form);
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postRegister() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<RegisterForm> form = Form.form(RegisterForm.class).bindFromRequest();

            if (form.hasErrors()) {
                return showRegister(form);
            } else {
                RegisterForm registerData = form.get();
                if (userService.existByUsername(registerData.username)) {
                    form.reject("register.error.usernameExists");
                    return showRegister(form);
                } else if (userEmailService.existByEmail(registerData.email)) {
                    form.reject("register.error.emailExists");
                    return showRegister(form);
                } else if (!registerData.password.equals(registerData.confirmPassword)) {
                    form.reject("register.error.passwordsDidntMatch");
                    return showRegister(form);
                } else {
                    try {
                        String emailCode = userAccountService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password);
                        userEmailService.sendActivationEmail(registerData.name, registerData.email, org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(emailCode).absoluteURL(request(), request().secure()));

                        return redirect(routes.UserAccountController.afterRegister(registerData.email));
                    } catch (IllegalStateException e){
                        form.reject("register.error.usernameOrEmailExists");
                        return showRegister(form);
                    }
                }
            }
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    public Result afterRegister(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("register.activationEmailSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("register.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "After Register");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    public Result forgotPassword() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<ForgotPasswordForm> form = Form.form(ForgotPasswordForm.class);
            return showForgotPassword(form);
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postForgotPassword() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<ForgotPasswordForm> form = Form.form(ForgotPasswordForm.class).bindFromRequest();

            if (form.hasErrors()) {
                return showForgotPassword(form);
            } else {
                ForgotPasswordForm forgotData = form.get();
                if (!userService.existByUsername(forgotData.username)) {
                    form.reject("forgot_pass.error.usernameNotExists");
                    return showForgotPassword(form);
                } else if (!userEmailService.existByEmail(forgotData.email)) {
                    form.reject("forgot_pass.error.emailNotExists");
                    return showForgotPassword(form);
                } else if (!userEmailService.isEmailOwnedByUser(forgotData.email, forgotData.username)) {
                    form.reject("forgot_pass.error.emailIsNotOwnedByUser");
                    return showForgotPassword(form);
                } else {
                    String forgotCode = userAccountService.forgotPassword(forgotData.username, forgotData.email);
                    userEmailService.sendChangePasswordEmail(forgotData.email, org.iatoki.judgels.jophiel.controllers.routes.UserAccountController.changePassword(forgotCode).absoluteURL(request(), request().secure()));

                    return redirect(routes.UserAccountController.afterForgotPassword(forgotData.email));
                }
            }
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    public Result afterForgotPassword(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("forgotPasswordEmail.changePasswordRequestSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("forgotPassword.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "After Forgot Password");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    public Result changePassword(String code) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            if (userAccountService.existForgotPassByCode(code)) {
                Form<ChangePasswordForm> form = Form.form(ChangePasswordForm.class);
                return showChangePassword(form, code);
            } else {
                return notFound();
            }
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postChangePassword(String code) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<ChangePasswordForm> form = Form.form(ChangePasswordForm.class).bindFromRequest();
            if (userAccountService.existForgotPassByCode(code)) {
                if (form.hasErrors()) {
                    return showChangePassword(form, code);
                } else {
                    ChangePasswordForm changeData = form.get();
                    if (!changeData.password.equals(changeData.confirmPassword)) {
                        form.reject("change_password.error.passwordsDidntMatch");
                        return showChangePassword(form, code);
                    } else {
                        userAccountService.changePassword(code, changeData.password);
                        return redirect(routes.UserAccountController.afterChangePassword());
                    }
                }
            } else {
                return notFound();
            }
        } else {
            return redirect(routes.UserProfileController.profile());
        }
    }

    public Result afterChangePassword() {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("changePassword.success") + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("changePassword.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "After Change Password");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    public Result login() {
        return serviceLogin(null);
    }

    @RequireCSRFCheck
    public Result postLogin() {
        return postServiceLogin(null);
    }

    @AddCSRFToken
    public Result serviceLogin(String continueUrl) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class);

            return showLogin(form, continueUrl);
        } else {
            if (continueUrl == null) {
                return redirect(routes.UserProfileController.profile());
            } else {
                return redirect(continueUrl);
            }
        }
    }

    @RequireCSRFCheck
    public Result postServiceLogin(String continueUrl) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
            if (form.hasErrors()) {
                Logger.error(form.errors().toString());
                return showLogin(form, continueUrl);
            } else {
                try {
                    LoginForm loginData = form.get();
                    User user = userAccountService.login(loginData.usernameOrEmail, loginData.password);
                    if (user != null) {
                        // TODO add expiry time and remember me options
                        session("userJid", user.getJid());
                        session("username", user.getUsername());
                        session("name", user.getName());
                        session("avatar", user.getProfilePictureUrl().toString());
                        JophielUtils.saveRoleInSession(user.getRoles());
                        ControllerUtils.getInstance().addActivityLog(userActivityService, "Logged In.");
                        if (continueUrl == null) {
                            return redirect(routes.UserProfileController.profile());
                        } else {
                            return redirect(continueUrl);
                        }
                    } else {
                        form.reject("login.error.usernameOrEmailOrPasswordInvalid");
                        return showLogin(form, continueUrl);
                    }
                } catch (UserNotFoundException e) {
                    form.reject("login.error.usernameOrEmailOrPasswordInvalid");
                    return showLogin(form, continueUrl);
                } catch (EmailNotVerifiedException e) {
                    form.reject("login.error.emailNotVerified");
                    return showLogin(form, continueUrl);
                }
            }
        } else {
            if (continueUrl == null) {
                return redirect(routes.UserProfileController.profile());
            } else {
                return redirect(continueUrl);
            }
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result logout() {
        return serviceLogout(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result serviceLogout(String returnUri) {
        ControllerUtils.getInstance().addActivityLog(userActivityService, "Logout <a href=\"\" + \"http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        session().clear();
        if (returnUri == null) {
            return redirect(routes.UserAccountController.login());
        } else {
            return redirect(returnUri);
        }
    }

    public Result serviceAuthRequest() {
        String redirectURI = request().uri().substring(request().uri().indexOf("?") + 1);
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            return redirect((routes.UserAccountController.serviceLogin("http" + (request().secure() ? "s" : "") + "://" + request().host() + request().uri())));
        } else {
            try {
                String path = request().uri().substring(request().uri().indexOf("?") + 1);

                AuthenticationRequest req = AuthenticationRequest.parse(redirectURI);
                ClientID clientID = req.getClientID();
                if (clientService.clientExistByClientJid(clientID.toString())) {
                    Client client = clientService.findClientByJid(clientID.toString());

                    List<String> scopes = req.getScope().toStringList();
                    if (clientService.isClientAuthorized(clientID.toString(), scopes)) {
                        return postServiceAuthRequest(path);
                    } else {
                        LazyHtml content = new LazyHtml(serviceAuthView.render(path, client, scopes));
                        content.appendLayout(c -> centerLayout.render(c));
                        ControllerUtils.getInstance().appendTemplateLayout(content, "Auth");

                        ControllerUtils.getInstance().addActivityLog(userActivityService, "Try authorize client " + client.getName() + " <a href=\"\" + \"http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

                        return ControllerUtils.getInstance().lazyOk(content);
                    }
                } else {
                    return redirect(redirectURI + "?error=unauthorized_client");
                }
            } catch (com.nimbusds.oauth2.sdk.ParseException e) {
                Logger.error("Exception when parsing authentication request.", e);
                return redirect(redirectURI + "?error=invalid_request");
            }
        }
    }

    @Transactional
    public Result postServiceAuthRequest(String path) {
        try {
            AuthenticationRequest req = AuthenticationRequest.parse(path);
            ClientID clientID = req.getClientID();
            if (clientService.clientExistByClientJid(clientID.toString())) {
                Client client = clientService.findClientByJid(clientID.toString());
                URI redirectURI = req.getRedirectionURI();
                ResponseType responseType = req.getResponseType();
                State state = req.getState();
                Scope scope = req.getScope();
                String nonce = (req.getNonce() != null) ? req.getNonce().toString() : "";

                AuthorizationCode code = clientService.generateAuthorizationCode(client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
                String accessToken = clientService.generateAccessToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
                clientService.generateRefreshToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
                clientService.generateIdToken(code.getValue(), IdentityUtils.getUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
                URI result = new AuthenticationSuccessResponse(redirectURI, code, null, null, state).toURI();

                ControllerUtils.getInstance().addActivityLog(userActivityService, "Authorize client " + client.getName() + ".");

                return redirect(result.toString());
            } else {
                return redirect(path + "?error=unauthorized_client");
            }
        } catch (com.nimbusds.oauth2.sdk.ParseException | SerializeException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }
    }

    private Result showRegister(Form<RegisterForm> form) {
        LazyHtml content = new LazyHtml(registerView.render(form));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Register");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showForgotPassword(Form<ForgotPasswordForm> form) {
        LazyHtml content = new LazyHtml(forgotPasswordView.render(form));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Forgot Password");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showChangePassword(Form<ChangePasswordForm> form, String code) {
        LazyHtml content = new LazyHtml(changePasswordView.render(form, code));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Change Password");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showLogin(Form<LoginForm> form, String continueUrl) {
        LazyHtml content;
        if (continueUrl == null) {
            content = new LazyHtml(loginView.render(form));
        } else {
            content = new LazyHtml(serviceLoginView.render(form, continueUrl));
        }
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Login");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
