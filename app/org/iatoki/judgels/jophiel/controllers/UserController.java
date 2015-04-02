package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
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
import org.iatoki.judgels.jophiel.UserProfileForm;
import org.iatoki.judgels.jophiel.UserProfilePictureForm;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.UserUpsertForm;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.activationView;
import org.iatoki.judgels.jophiel.views.html.changePasswordView;
import org.iatoki.judgels.jophiel.views.html.forgotPasswordView;
import org.iatoki.judgels.jophiel.views.html.loginView;
import org.iatoki.judgels.jophiel.views.html.registerView;
import org.iatoki.judgels.jophiel.views.html.editProfileView;
import org.iatoki.judgels.jophiel.views.html.viewProfileView;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.jophiel.views.html.user.updateUserView;
import org.iatoki.judgels.jophiel.views.html.user.viewUserView;
import play.Logger;
import play.Play;
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

@Transactional
public final class UserController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final UserService userService;
    private final ClientService clientService;

    public UserController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
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
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class);

        return showCreateUser(form);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postCreateUser() {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateUser(form);
        } else {
            UserUpsertForm userUpsertForm = form.get();
            userService.createUser(userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password, Arrays.asList(userUpsertForm.roles.split(",")));
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
        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result updateUser(long userId) {
        User user = userService.findUserById(userId);
        UserUpsertForm userUpsertForm = new UserUpsertForm(user);
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).fill(userUpsertForm);

        return showUpdateUser(form, user);
    }

    @RequireCSRFCheck
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postUpdateUser(long userId) {
        User user = userService.findUserById(userId);
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showUpdateUser(form, user);
        } else {
            UserUpsertForm userUpsertForm = form.get();
            userService.updateUser(userId, userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password, Arrays.asList(userUpsertForm.roles.split(",")));
            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result deleteUser(long userId) {
        userService.deleteUser(userId);

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
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class);
            return showLogin(form);
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postLogin() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByUserJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
            if (form.hasErrors()) {
                Logger.error(form.errors().toString());
                return showLogin(form);
            } else {
                LoginForm loginData = form.get();
                if (userService.login(loginData.usernameOrEmail, loginData.password)) {
                    return redirect(routes.UserController.profile());
                } else {
                    form.reject("login.error.usernameOrEmailOrPasswordInvalid");
                    return showLogin(form);
                }
            }
        } else {
            return redirect(routes.UserController.profile());
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
        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
        User user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        form = form.fill(new UserProfileForm(user));

        return showProfile(form, form2);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postProfile() {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class).bindFromRequest();

        if (form.hasErrors()) {
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            Logger.error(form.errors().toString());
            return showProfile(form, form2);
        } else {
            UserProfileForm userProfileForm = form.get();
            if (("".equals(userProfileForm.password) && ("".equals(userProfileForm.confirmPassword)))) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name);
                return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
            } else if (userProfileForm.password.equals(userProfileForm.confirmPassword)) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name, userProfileForm.password);
                return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
            } else {
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form.reject("profile.error.passwordsDidntMatch");
                Logger.error("Password do not match.");
                return showProfile(form, form2);
            }
        }
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

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postAvatar() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

        if (avatar != null) {
            String contentType = avatar.getContentType();
            if (!((contentType.equals("image/png")) || (contentType.equals("image/jpg")) || (contentType.equals("image/jpeg")))) {
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form2.reject("profile.error.avatarNotPicture");
                return showProfile(form, form2);
            } else if (avatar.getFile().length() > (2 << 20)) {
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form2.reject("profile.error.overSizeLimit");
                return showProfile(form, form2);
            } else {
                URL profilePictureUrl = userService.updateProfilePicture(IdentityUtils.getUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()));
                session("avatar", profilePictureUrl.toString());
                return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.profile());
            }
        } else {
            Form<UserProfileForm> form = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            return showProfile(form, form2);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result logout() {
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
            session().clear();
            return redirect(routes.UserController.login());
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

    private Result showCreateUser(Form<UserUpsertForm> form) {
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

    private Result showUpdateUser(Form<UserUpsertForm> form, User user) {
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

    private Result showLogin(Form<LoginForm> form) {
        LazyHtml content = new LazyHtml(loginView.render(form));
        content.appendLayout(c -> centerLayout.render(c));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Login");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showProfile(Form<UserProfileForm> form, Form<UserProfilePictureForm> form2) {
        LazyHtml content = new LazyHtml(editProfileView.render(form, form2));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.profile()), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewOwnActivities())), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("profile.profile"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("profile.profile"), routes.UserController.profile())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Profile");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
