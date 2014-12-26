package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarWithoutProfileLayout;
import org.iatoki.judgels.commons.views.html.layouts.noSidebarLayout;
import org.iatoki.judgels.jophiel.RegisterForm;
import org.iatoki.judgels.jophiel.views.html.registerView;
import org.iatoki.judgels.jophiel.views.html.user.createView;
import org.iatoki.judgels.jophiel.views.html.user.listView;
import org.iatoki.judgels.jophiel.views.html.user.updateView;
import org.iatoki.judgels.jophiel.views.html.user.viewView;
import org.iatoki.judgels.jophiel.UserUpsertForm;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class UserController extends Controller {

    private static final long PAGE_SIZE = 20;

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    public Result index() {
        return list(0, "id", "asc", "");
    }

    private Result showCreate(Form<UserUpsertForm> form) {
        LazyHtml content = new LazyHtml(createView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.heading.create"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.heading.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.heading.create"), routes.UserController.create())
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    public Result create() {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class);

        return showCreate(form);
    }

    @Transactional
    public Result postCreate() {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreate(form);
        } else {
            UserUpsertForm userUpsertForm = form.get();

            userService.createUser(userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password);

            return redirect(routes.UserController.index());
        }
    }

    @Transactional
    public Result view(long userId) {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(user.getName(), new InternalLink("user.update", routes.UserController.update(userId)), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink("user", routes.UserController.index()),
                new InternalLink("user.view", routes.UserController.view(userId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showUpdate(Form<UserUpsertForm> form, long userId) {
        LazyHtml content = new LazyHtml(updateView.render(form, userId));
        content.appendLayout(c -> headingLayout.render("user.update", c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink("user", routes.UserController.index()),
                new InternalLink("user.update", routes.UserController.update(userId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    @Transactional
    public Result update(long userId) {
        User user = userService.findUserById(userId);
        UserUpsertForm userUpsertForm = new UserUpsertForm(user);
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).fill(userUpsertForm);

        return showUpdate(form, userId);
    }

    @RequireCSRFCheck
    @Transactional
    public Result postUpdate(long userId) {
        Form<UserUpsertForm> form = Form.form(UserUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdate(form, userId);
        } else {
            UserUpsertForm userUpsertForm = form.get();

            userService.updateUser(userId, userUpsertForm.username, userUpsertForm.name, userUpsertForm.email, userUpsertForm.password);

            return redirect(routes.UserController.index());
        }
    }

    @Transactional
    public Result delete(long userId) {
        userService.deleteUser(userId);

        return redirect(routes.UserController.index());
    }

    @Transactional
    public Result list(long page, String sortBy, String orderBy, String filterString) {
        Page<User> currentPage = userService.pageUser(page, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingWithActionLayout.render("user.list", new InternalLink("user.create", routes.UserController.create()), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink("user", routes.UserController.index())
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

    @AddCSRFToken
    public Result register() {
        Form<RegisterForm> form = Form.form(RegisterForm.class);

        return showRegister(form);
    }

    @RequireCSRFCheck
    @Transactional
    public Result postRegister() {
        Form<RegisterForm> form = Form.form(RegisterForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showRegister(form);
        } else {
            RegisterForm registerData = form.get();

            userService.createUser(registerData.username, registerData.name, registerData.email, registerData.password);

            return TODO;
        }
    }

    public Result logout() {
        session().clear();
        return TODO;
    }

    private void appendTemplateLayout(LazyHtml content) {
        content.appendLayout(c -> leftSidebarWithoutProfileLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                        new InternalLink(Messages.get("client.clients"), routes.ClientController.index())
                ), c)
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
