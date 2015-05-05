package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserActivityService;
import org.iatoki.judgels.jophiel.UserCreateForm;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.UserUpdateForm;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUnverifiedUsersView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.jophiel.views.html.user.updateUserView;
import org.iatoki.judgels.jophiel.views.html.user.viewUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;

@Transactional
public final class UserController extends BaseController {

    private static final long PAGE_SIZE = 20;
    private final UserService userService;
    private final UserActivityService userActivityService;

    public UserController(UserService userService, UserActivityService userActivityService) {
        this.userService = userService;
        this.userActivityService = userActivityService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result listUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<User> currentPage = userService.pageUsers(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.unverifiedUsers"), routes.UserController.viewUnverifiedUsers())
        ), c));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.createUser()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Open all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result viewUnverifiedUsers() {
        return listUnverifiedUsers(0, "id", "asc", "");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result listUnverifiedUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<User> currentPage = userService.pageUnverifiedUsers(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listUnverifiedUsersView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.unverifiedUsers"), routes.UserController.viewUnverifiedUsers())
        ), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.unverifiedUsers.list"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.unverifiedUsers"), routes.UserController.viewUnverifiedUsers())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Open unverified users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + userId + ": " + user.getName(), new InternalLink(Messages.get("commons.update"), routes.UserController.updateUser(userId)), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(userId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        ControllerUtils.getInstance().addActivityLog(userActivityService, "View user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result createUser() {
        UserCreateForm data = new UserCreateForm();
        data.roles = StringUtils.join(JophielUtils.getDefaultRoles(), ",");
        Form<UserCreateForm> form = Form.form(UserCreateForm.class).fill(data);

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

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

            ControllerUtils.getInstance().addActivityLog(userActivityService, "Create user " + userUpsertForm.username + ".");

            return redirect(routes.UserController.index());
        }
    }

    @AddCSRFToken
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result updateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserUpdateForm userUpdateForm = new UserUpdateForm(user);
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).fill(userUpdateForm);

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(form, user);
    }

    @RequireCSRFCheck
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result postUpdateUser(long userId) throws UserNotFoundException {
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

            ControllerUtils.getInstance().addActivityLog(userActivityService, "Update user " + user.getUsername() + ".");

            return redirect(routes.UserController.index());
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    public Result deleteUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getId());

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Delete user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
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
}
