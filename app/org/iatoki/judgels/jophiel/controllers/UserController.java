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
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.centerLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.messageView;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.ChangePasswordForm;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.ForgotPasswordForm;
import org.iatoki.judgels.jophiel.LoginForm;
import org.iatoki.judgels.jophiel.RegisterForm;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserCreateForm;
import org.iatoki.judgels.jophiel.UserProfileForm;
import org.iatoki.judgels.jophiel.UserProfilePictureForm;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.UserUpdateForm;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.activationView;
import org.iatoki.judgels.jophiel.views.html.changePasswordView;
import org.iatoki.judgels.jophiel.views.html.editProfileView;
import org.iatoki.judgels.jophiel.views.html.forgotPasswordView;
import org.iatoki.judgels.jophiel.views.html.loginView;
import org.iatoki.judgels.jophiel.views.html.registerView;
import org.iatoki.judgels.jophiel.views.html.serviceAuthView;
import org.iatoki.judgels.jophiel.views.html.serviceEditProfileView;
import org.iatoki.judgels.jophiel.views.html.serviceLoginView;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.jophiel.views.html.user.updateUserView;
import org.iatoki.judgels.jophiel.views.html.user.viewUserView;
import org.iatoki.judgels.jophiel.views.html.viewProfileView;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Transactional
public final class UserController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final ClientService clientService;
    private final UserService userService;

    public UserController(ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result createUser() {
        UserCreateForm data = new UserCreateForm();
        data.roles = "user";
        Form<UserCreateForm> form = Form.form(UserCreateForm.class).fill(data);

        ControllerUtils.getInstance().addActivityLog(userService, "Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateUser(form);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postCreateUser() {
        Form<UserCreateForm> form = Form.form(UserCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateUser(form);
        } else {
            UserCreateForm userUpsertForm = form.get();
            userService.createUser(userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password, Arrays.asList(userUpsertForm.roles.split(",")));

            ControllerUtils.getInstance().addActivityLog(userService, "Create user " + userUpsertForm.username + ".");

            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result listUsers(long page, String orderBy, String orderDir, String filterString) {
        Page<User> currentPage = userService.pageUsers(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.createUser()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        ControllerUtils.getInstance().addActivityLog(userService, "Open all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result viewUser(long userId) {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + userId + ": " + user.getName(), new InternalLink(Messages.get("commons.update"), routes.UserController.updateUser(userId)), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(userId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        ControllerUtils.getInstance().addActivityLog(userService, "View user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result updateUser(long userId) {
        User user = userService.findUserById(userId);
        UserUpdateForm userUpdateForm = new UserUpdateForm(user);
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).fill(userUpdateForm);

        ControllerUtils.getInstance().addActivityLog(userService, "Try to update user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(form, user);
    }

    @RequireCSRFCheck
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postUpdateUser(long userId) {
        User user = userService.findUserById(userId);
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showUpdateUser(form, user);
        } else {
            UserUpdateForm userUpdateForm = form.get();
            if (!"".equals(userUpdateForm.password)) {
                userService.updateUser(user.getId(), userUpdateForm.username, userUpdateForm.name, userUpdateForm.email, userUpdateForm.password, Arrays.asList(userUpdateForm.roles.split(",")));
            } else {
                userService.updateUser(user.getId(), userUpdateForm.username, userUpdateForm.name, userUpdateForm.email, Arrays.asList(userUpdateForm.roles.split(",")));
            }

            ControllerUtils.getInstance().addActivityLog(userService, "Update user " + user.getUsername() + ".");

            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result deleteUser(long userId) {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getId());

        ControllerUtils.getInstance().addActivityLog(userService, "Delete user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    @AddCSRFToken
    public Result register() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<RegisterForm> form = Form.form(RegisterForm.class);
            return showRegister(form);
        } else {
            return redirect(routes.UserController.profile());
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
                } else if (userService.existByEmail(registerData.email)) {
                    form.reject("register.error.emailExists");
                    return showRegister(form);
                } else if (!registerData.password.equals(registerData.confirmPassword)) {
                    form.reject("register.error.passwordsDidntMatch");
                    return showRegister(form);
                } else {
                    try {
                        String emailCode = userService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password);
                        Email email = new Email();
                        email.setSubject(Play.application().configuration().getString("application.sub-title") + " " + Messages.get("registrationEmail.userRegistration"));
                        email.setFrom(Play.application().configuration().getString("email.name") + " <" + Play.application().configuration().getString("email.email") + ">");
                        email.addTo(registerData.name + " <" + registerData.email + ">");
                        email.setBodyHtml("<p>" + Messages.get("registrationEmail.thankYou") + " " + Play.application().configuration().getString("application.sub-title") + ".</p><p>" + Messages.get("registrationEmail.pleaseActivate") + " <a href='" + org.iatoki.judgels.jophiel.controllers.routes.UserController.verifyEmail(emailCode).absoluteURL(request()) + "'>here</a>.</p>");
                        MailerPlugin.send(email);

                        LazyHtml content = new LazyHtml(messageView.render(Messages.get("register.activationEmailSentTo") + " " + registerData.email + "."));
                        content.appendLayout(c -> headingLayout.render(Messages.get("register.successful"), c));
                        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(), c));
                        content.appendLayout(c -> centerLayout.render(c));
                        content.appendLayout(c -> headerFooterLayout.render(c));
                        ControllerUtils.getInstance().appendTemplateLayout(content, "After Register");
                        return ControllerUtils.getInstance().lazyOk(content);
                    } catch (IllegalStateException e){
                        form.reject("register.error.usernameOrEmailExists");
                        return showRegister(form);
                    }
                }
            }
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @AddCSRFToken
    public Result forgotPassword() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<ForgotPasswordForm> form = Form.form(ForgotPasswordForm.class);
            return showForgotPassword(form);
        } else {
            return redirect(routes.UserController.profile());
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
                } else if (!userService.existByEmail(forgotData.email)) {
                    form.reject("forgot_pass.error.emailNotExists");
                    return showForgotPassword(form);
                } else if (!userService.isEmailOwnedByUser(forgotData.email, forgotData.username)) {
                    form.reject("forgot_pass.error.emailIsNotOwnedByUser");
                    return showForgotPassword(form);
                } else {
                    String forgotCode = userService.forgotPassword(forgotData.username, forgotData.email);
                    Email email = new Email();
                    email.setSubject(Play.application().configuration().getString("application.sub-title") + " " + Messages.get("forgotPasswordEmail.forgotPassword"));
                    email.setFrom(Play.application().configuration().getString("email.name") + " <" + Play.application().configuration().getString("email.email") + ">");
                    email.addTo(forgotData.email);
                    email.setBodyHtml("<p>" + Messages.get("forgotPasswordEmail.request") + " " + Play.application().configuration().getString("application.sub-title") + ".</p><p>" + Messages.get("forgotPasswordEmail.changePassword") + " <a href='" + org.iatoki.judgels.jophiel.controllers.routes.UserController.changePassword(forgotCode).absoluteURL(request()) + "'>here</a>.</p>");
                    MailerPlugin.send(email);

                    LazyHtml content = new LazyHtml(messageView.render(Messages.get("forgotPasswordEmail.changePasswordRequestSentTo") + " " + forgotData.email + "."));
                    content.appendLayout(c -> headingLayout.render(Messages.get("forgotPassword.successful"), c));
                    content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(), c));
                    content.appendLayout(c -> centerLayout.render(c));
                    content.appendLayout(c -> headerFooterLayout.render(c));
                    ControllerUtils.getInstance().appendTemplateLayout(content, "After Forgot Password");
                    return ControllerUtils.getInstance().lazyOk(content);
                }
            }
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @AddCSRFToken
    public Result changePassword(String code) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            if (userService.existForgotPassByCode(code)) {
                Form<ChangePasswordForm> form = Form.form(ChangePasswordForm.class);
                return showChangePassword(form, code);
            } else {
                return notFound();
            }
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postChangePassword(String code) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<ChangePasswordForm> form = Form.form(ChangePasswordForm.class).bindFromRequest();
            if (userService.existForgotPassByCode(code)) {
                if (form.hasErrors()) {
                    return showChangePassword(form, code);
                } else {
                    ChangePasswordForm changeData = form.get();
                    if (!changeData.password.equals(changeData.confirmPassword)) {
                        form.reject("change_password.error.passwordsDidntMatch");
                        return showChangePassword(form, code);
                    } else {
                        userService.changePassword(code, changeData.password);

                        LazyHtml content = new LazyHtml(messageView.render(Messages.get("changePassword.success") + "."));
                        content.appendLayout(c -> headingLayout.render(Messages.get("changePassword.successful"), c));
                        content.appendLayout(c -> centerLayout.render(c));
                        content.appendLayout(c -> headerFooterLayout.render(c));
                        ControllerUtils.getInstance().appendTemplateLayout(content, "After Change Password");
                        return ControllerUtils.getInstance().lazyOk(content);
                    }
                }
            } else {
                return notFound();
            }
        } else {
            return redirect(routes.UserController.profile());
        }
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
                return redirect(routes.UserController.profile());
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
                LoginForm loginData = form.get();
                if (userService.login(loginData.usernameOrEmail, loginData.password)) {
                    ControllerUtils.getInstance().addActivityLog(userService, "Logged In.");
                    if (continueUrl == null) {
                        return redirect(routes.UserController.profile());
                    } else {
                        return redirect(continueUrl);
                    }
                } else {
                    form.reject("login.error.usernameOrEmailOrPasswordInvalid");
                    return showLogin(form, continueUrl);
                }
            }
        } else {
            if (continueUrl == null) {
                return redirect(routes.UserController.profile());
            } else {
                return redirect(continueUrl);
            }
        }
    }

    public Result verifyEmail(String emailCode) {
        if (userService.activateEmail(emailCode)) {
            LazyHtml content = new LazyHtml(activationView.render());
            content.appendLayout(c -> centerLayout.render(c));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Verify Email");
            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result profile() {
        return serviceProfile(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postProfile() {
        return postServiceProfile(null);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result viewProfile(String username) {
        if (userService.existByUsername(username)) {
            User user = userService.findUserByUsername(username);

            LazyHtml content = new LazyHtml(viewProfileView.render(user));
            if (IdentityUtils.getUserJid() != null) {
                content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
                content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
                ControllerUtils.getInstance().appendSidebarLayout(content);
                ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                      new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username))
                ));
            } else {
                content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
                content.appendLayout(c -> centerLayout.render(c));
            }
            ControllerUtils.getInstance().appendTemplateLayout(content, "Profile");

            ControllerUtils.getInstance().addActivityLog(userService, "View user profile " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postAvatar() {
        return postServiceAvatar(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result serviceProfile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
        User user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        form = form.fill(new UserProfileForm(user));

        ControllerUtils.getInstance().addActivityLog(userService, "Try to update profile <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showProfile(form, form2, continueUrl);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postServiceProfile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class).bindFromRequest();

        if (form.hasErrors()) {
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            Logger.error(form.errors().toString());
            return showProfile(form, form2, continueUrl);
        } else {
            UserProfileForm userProfileForm = form.get();
            if (("".equals(userProfileForm.password) && ("".equals(userProfileForm.confirmPassword)))) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name);

                ControllerUtils.getInstance().addActivityLog(userService, "Update profile.");
                if (continueUrl == null) {
                    return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
                } else {
                    return redirect(continueUrl);
                }
            } else if (userProfileForm.password.equals(userProfileForm.confirmPassword)) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name, userProfileForm.password);

                ControllerUtils.getInstance().addActivityLog(userService, "Update profile.");
                if (continueUrl == null) {
                    return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
                } else {
                    return redirect(continueUrl);
                }
            } else {
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form.reject("error.profile.password.not_match");
                Logger.error("Password do not match.");
                return showProfile(form, form2, continueUrl);
            }
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postServiceAvatar(String continueUrl) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

        if (avatar != null) {
            String contentType = avatar.getContentType();
            if (!((contentType.equals("image/png")) || (contentType.equals("image/jpg")) || (contentType.equals("image/jpeg")))) {
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form2.reject("error.profile.not_picture");
                return showProfile(form, form2, continueUrl);
            } else if (avatar.getFile().length() > (2 << 20)) {
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form2.reject("profile.error.overSizeLimit");
                return showProfile(form, form2, continueUrl);
            } else {
                URL profilePictureUrl = userService.updateProfilePicture(IdentityUtils.getUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()));
                session("avatar", profilePictureUrl.toString());

                ControllerUtils.getInstance().addActivityLog(userService, "Update avatar.");

                if (continueUrl == null) {
                    return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
                } else {
                    return redirect(routes.UserController.serviceProfile(continueUrl));
                }
            }
        } else {
            Form<UserProfileForm> form = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            return showProfile(form, form2, continueUrl);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result logout() {
        return serviceLogout(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result serviceLogout(String returnUri) {
        try {
            List<Client> clients = clientService.findAll();
            for (Client client : clients) {
                for (String uRI : client.getRedirectURIs()) {
                    URI uri = new URI(uRI);
                    String[] domainParts = uri.getHost().split("\\.");
                    String mainDomain;
                    if (domainParts.length >= 2) {
                        mainDomain = "." + domainParts[domainParts.length - 2] + "." + domainParts[domainParts.length - 1];
                    } else {
                        mainDomain = null;
                    }
                    response().setCookie("JOID-" + client.getJid(), "EXPIRED", 0, "/", mainDomain, false, true);
                }
            }

            ControllerUtils.getInstance().addActivityLog(userService, "Logout <a href=\"\" + \"http://\" + Http.Context.current().request().host() + Http.Context.current().request().uri() + \"\">link</a>.");

            session().clear();
            if (returnUri == null) {
                return redirect(routes.UserController.login());
            } else {
                return redirect(returnUri);
            }
        } catch (URISyntaxException e) {
            // TODO make sure URI is URI at
            throw new RuntimeException(e);
        }
    }

    public Result renderAvatarImage(String imageName) {
        File image = userService.getAvatarImageFile(imageName);
        if (!image.exists()) {
            return notFound();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");
        response().setHeader("Last-Modified", sdf.format(new Date(image.lastModified())));

        if (request().hasHeader("If-Modified-Since")) {
            try {
                Date lastUpdate = sdf.parse(request().getHeader("If-Modified-Since"));
                if (image.lastModified() > lastUpdate.getTime()) {
                    BufferedImage in = ImageIO.read(image);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    String type = FilenameUtils.getExtension(image.getAbsolutePath());

                    ImageIO.write(in, type, baos);
                    return ok(baos.toByteArray()).as("image/" + type);
                } else {
                    return status(304);
                }
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                BufferedImage in = ImageIO.read(image);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String type = FilenameUtils.getExtension(image.getAbsolutePath());

                ImageIO.write(in, type, baos);
                return ok(baos.toByteArray()).as("image/" + type);
            } catch (IOException e) {
                return internalServerError();
            }
        }
    }

    public Result serviceAuthRequest() {
        String redirectURI = request().uri().substring(request().uri().indexOf("?") + 1);
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            return redirect((routes.UserController.serviceLogin("http://" + request().host() + request().uri())));
        } else {
            try {
                String randomHash = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
                Cache.set(randomHash, request().uri().substring(request().uri().indexOf("?") + 1));

                AuthenticationRequest req = AuthenticationRequest.parse(redirectURI);
                ClientID clientID = req.getClientID();
                Client client = clientService.findClientByJid(clientID.toString());

                List<String> scopes = req.getScope().toStringList();
                if (clientService.isClientAuthorized(clientID.toString(), scopes)) {
                    return postServiceAuthRequest(randomHash);
                } else {
                    LazyHtml content = new LazyHtml(serviceAuthView.render(randomHash, client, scopes));
                    content.appendLayout(c -> centerLayout.render(c));
                    ControllerUtils.getInstance().appendTemplateLayout(content, "Auth");

                    ControllerUtils.getInstance().addActivityLog(userService, "Try authorize client " + client.getName() + " <a href=\"\" + \"http://\" + Http.Context.current().request().host() + Http.Context.current().request().uri() + \"\">link</a>.");

                    return ControllerUtils.getInstance().lazyOk(content);
                }
            } catch (com.nimbusds.oauth2.sdk.ParseException e) {
                Logger.error("Exception when parsing authentication request.", e);
                return redirect(redirectURI + "?error=invalid_request");
            }
        }
    }

    @Transactional
    public Result postServiceAuthRequest(String hash) {
        Object path = Cache.get(hash);
        if (path != null) {
            try {
                AuthenticationRequest req = AuthenticationRequest.parse(path.toString());
                ClientID clientID = req.getClientID();
                Client client = clientService.findClientByJid(clientID.toString());
                URI redirectURI = req.getRedirectionURI();
                ResponseType responseType = req.getResponseType();
                State state = req.getState();
                Scope scope = req.getScope();
                String nonce = (req.getNonce() != null) ? req.getNonce().toString() : "";

                AuthorizationCode code = clientService.generateAuthorizationCode(client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList());
                String accessToken = clientService.generateAccessToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
                clientService.generateRefreshToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
                clientService.generateIdToken(code.getValue(), IdentityUtils.getUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken);
                URI result = new AuthenticationSuccessResponse(redirectURI, code, null, null, state).toURI();

                String[] domainParts = result.getHost().split("\\.");
                String mainDomain;
                if (domainParts.length >= 2) {
                    mainDomain = "." + domainParts[domainParts.length - 2] + "." + domainParts[domainParts.length - 1];
                } else {
                    mainDomain = null;
                }

                ControllerUtils.getInstance().addActivityLog(userService, "Authorize client " + client.getName() + ".");

                response().setCookie("JOID-" + client.getJid(), clientService.findIdTokenByCode(code.getValue()).getToken(), (int) (TimeUnit.SECONDS.convert(14, TimeUnit.DAYS)), "/", mainDomain, false, true);
                return redirect(result.toString());
            } catch (com.nimbusds.oauth2.sdk.ParseException | SerializeException e) {
                Logger.error("Exception when parsing authentication request.", e);
                return redirect(path + "?error=invalid_request");
            }
        } else {
            throw new RuntimeException("This exception should never happened.");
        }
    }

    private Result showCreateUser(Form<UserCreateForm> form) {
        LazyHtml content = new LazyHtml(createUserView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.create"), routes.UserController.createUser())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Change Password");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateUser(Form<UserUpdateForm> form, User user) {
        LazyHtml content = new LazyHtml(updateUserView.render(form, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + user.getUsername(), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.update"), routes.UserController.updateUser(user.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");
        return ControllerUtils.getInstance().lazyOk(content);
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

    private Result showProfile(Form<UserProfileForm> form, Form<UserProfilePictureForm> form2, String continueUrl) {
        LazyHtml content;
        if (continueUrl == null) {
            content = new LazyHtml(editProfileView.render(form, form2));
        } else {
            content = new LazyHtml(serviceEditProfileView.render(form, form2, continueUrl));
        }
        content.appendLayout(c -> headingLayout.render(Messages.get("profile.profile"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        if (continueUrl == null) {
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserController.profile())
            ));
        } else {
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserController.serviceProfile(continueUrl))
            ));
        }
        ControllerUtils.getInstance().appendTemplateLayout(content, "Profile");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
