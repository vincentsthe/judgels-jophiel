package org.iatoki.judgels.jophiel.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.messageView;
import org.iatoki.judgels.commons.views.html.layouts.noSidebarLayout;
import org.iatoki.judgels.jophiel.JophielUtils;
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
import org.iatoki.judgels.jophiel.views.html.loginView;
import org.iatoki.judgels.jophiel.views.html.registerView;
import org.iatoki.judgels.jophiel.views.html.user.createView;
import org.iatoki.judgels.jophiel.views.html.user.listView;
import org.iatoki.judgels.jophiel.views.html.user.profileView;
import org.iatoki.judgels.jophiel.views.html.user.updateView;
import org.iatoki.judgels.jophiel.views.html.user.viewView;
import play.Logger;
import play.Play;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

@Transactional
public final class UserController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result index() {
        return list(0, "id", "asc", "");
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result create() {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class);

        return showCreate(form);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postCreate() {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreate(form);
        } else {
            UserUpsertForm userUpsertForm = form.get();

            userService.createUser(userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password, Arrays.asList(userUpsertForm.roles.split(",")));

            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result view(long userId) {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + userId + ": " + user.getName(), new InternalLink(Messages.get("commons.update"), routes.UserController.update(userId)), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.view"), routes.UserController.view(userId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result update(long userId) {
        User user = userService.findUserById(userId);
        UserUpsertForm userUpsertForm = new UserUpsertForm(user);
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).fill(userUpsertForm);

        return showUpdate(form, user);
    }

    @RequireCSRFCheck
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postUpdate(long userId) {
        User user = userService.findUserById(userId);
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showUpdate(form, user);
        } else {
            UserUpsertForm userUpsertForm = form.get();

            userService.updateUser(userId, userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password, Arrays.asList(userUpsertForm.roles.split(",")));

            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result delete(long userId) {
        userService.deleteUser(userId);

        return redirect(routes.UserController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result list(long page, String orderBy, String orderDir, String filterString) {
        Page<User> currentPage = userService.pageUsers(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.create()), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result register() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<RegisterForm> form = Form.form(RegisterForm.class);
            return showRegister(form);
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postRegister() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<RegisterForm> form = Form.form(RegisterForm.class).bindFromRequest();

            if (form.hasErrors()) {
                return showRegister(form);
            } else {
                RegisterForm registerData = form.get();
                String emailCode = userService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password);
                Email email = new Email();
                email.setSubject(Play.application().configuration().getString("application.title") + " User Registration");
                email.setFrom(Play.application().configuration().getString("email.name") + " <" + Play.application().configuration().getString("email.email") + ">");
                email.addTo(registerData.name + " <" + registerData.email + ">");
                email.setBodyHtml("<h1>Thanks for the registration</h1>Please activate your account on this <a href='" + org.iatoki.judgels.jophiel.controllers.routes.UserController.verifyEmail(emailCode).absoluteURL(request()) +"'>link</a>");
                MailerPlugin.send(email);

                LazyHtml content = new LazyHtml(messageView.render("Thanks for the registration. An email to activate your account has been sent to " + registerData.email));
                content.appendLayout(c -> noSidebarLayout.render(c));
                content.appendLayout(c -> headerFooterLayout.render(c));
                content.appendLayout(c -> baseLayout.render("TODO", c));
                return lazyOk(content);
            }
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @AddCSRFToken
    public Result login() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class);
            return showLogin(form);
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    @RequireCSRFCheck
    public Result postLogin() {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
            if (form.hasErrors()) {
                Logger.error(form.errors().toString());
                return showLogin(form);
            } else {
                LoginForm loginData = form.get();
                if (userService.login(loginData.usernameOrEmail, loginData.password)) {
                    return redirect(routes.UserController.profile());
                } else {
                    form.reject("Username or email not found or password do not match");
                    return showLogin(form);
                }
            }
        } else {
            return redirect(routes.UserController.profile());
        }
    }

    public Result verifyEmail(String emailCode) {
        if (userService.activateEmail(emailCode)) {
            LazyHtml content = new LazyHtml(messageView.render("Your account has been activated."));
            content.appendLayout(c -> noSidebarLayout.render(c));
            content.appendLayout(c -> headerFooterLayout.render(c));
            content.appendLayout(c -> baseLayout.render("TODO", c));
            return lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result profile() {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        form.fill(new UserProfileForm(user));

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
                Logger.error("Password do not match.");
                return showProfile(form, form2);
            }
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postAvatar() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

        if (avatar != null) {
            String contentType = avatar.getContentType();
            if (!((contentType.equals("image/png")) || (contentType.equals("image/jpg")) || (contentType.equals("image/jpeg")))) {
                flash("failed", Messages.get("views.not_picture"));
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
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
        for (Http.Cookie cookie : request().cookies()) {
            if ("JO".equals(cookie.name().substring(0, 2))) {
                response().discardCookie(cookie.name());
            }
        }
        session().clear();
        return redirect(routes.UserController.login());
    }

    private Result showLogin(Form<LoginForm> form) {
        LazyHtml content = new LazyHtml(loginView.render(form));
        content.appendLayout(c -> noSidebarLayout.render(c));
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
        return lazyOk(content);
    }

    private Result showProfile(Form<UserProfileForm> form, Form<UserProfilePictureForm> form2) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();

        if (JophielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));
        }

        LazyHtml content = new LazyHtml(profileView.render(form, form2));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.profile"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.profile"), routes.UserController.profile())
        ), c));
        content.appendLayout(c -> leftSidebarLayout.render(
                        IdentityUtils.getUsername(),
                        IdentityUtils.getUserRealName(),
                        org.iatoki.judgels.jophiel.controllers.routes.UserController.profile().absoluteURL(request()),
                        org.iatoki.judgels.jophiel.controllers.routes.UserController.logout().absoluteURL(request()),
                        internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
        return lazyOk(content);
    }

    private Result showCreate(Form<UserUpsertForm> form) {
        LazyHtml content = new LazyHtml(createView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.create"), routes.UserController.create())
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showUpdate(Form<UserUpsertForm> form, User user) {
        LazyHtml content = new LazyHtml(updateView.render(form, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + user.getUsername(), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.update"), routes.UserController.update(user.getId()))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showRegister(Form<RegisterForm> form) {
        LazyHtml content = new LazyHtml(registerView.render(form));
        content.appendLayout(c -> noSidebarLayout.render(c));
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));

        return lazyOk(content);
    }

    private void appendTemplateLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        internalLinkBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));

        content.appendLayout(c -> leftSidebarLayout.render(
                        IdentityUtils.getUsername(),
                        IdentityUtils.getUserRealName(),
                        org.iatoki.judgels.jophiel.controllers.routes.UserController.profile().absoluteURL(request()),
                        org.iatoki.judgels.jophiel.controllers.routes.UserController.logout().absoluteURL(request()),
                        internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));

    }

    private Result lazyOk(LazyHtml content) {
        return getResult(content, Http.Status.OK);
    }

    private Result getResult(LazyHtml content, int statusCode) {
        switch (statusCode) {
            case Http.Status.OK:
                return ok(content.render(0));
            case Http.Status.NOT_FOUND:
                return notFound(content.render(0));
            default:
                return badRequest(content.render(0));
        }
    }

}
