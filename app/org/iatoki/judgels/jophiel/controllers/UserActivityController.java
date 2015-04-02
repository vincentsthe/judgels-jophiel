package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserActivity;
import org.iatoki.judgels.jophiel.UserActivityFilterForm;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.user.activity.listOwnActivitiesView;
import org.iatoki.judgels.jophiel.views.html.user.activity.listUserActivitiesView;
import org.iatoki.judgels.jophiel.views.html.user.activity.listUsersActivitiesView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

@Transactional
public final class UserActivityController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final UserService userService;
    private final ClientService clientService;

    public UserActivityController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result index() {
        return listUsersActivities(0, "time", "desc", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result listUsersActivities(long page, String orderBy, String orderDir, String filterString) {
        Page<UserActivity> currentPage;

        Form<UserActivityFilterForm> form = Form.form(UserActivityFilterForm.class).bindFromRequest();
        if (form.hasErrors() || form.hasGlobalErrors()) {
            currentPage = userService.pageUsersActivities(page, PAGE_SIZE, orderBy, orderDir, filterString);
        } else {
            UserActivityFilterForm formData = form.get();
            String [] clientNames = formData.clients.split(",");
            ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
            for (String client : clientNames) {
                if ((!"".equals(client)) && (clientService.existByName(client))) {
                    clientNamesSetBuilder.add(client);
                }
            }
            String [] usernames = formData.users.split(",");
            ImmutableSet.Builder<String> usernamesSetBuilder = ImmutableSet.builder();
            for (String user : usernames) {
                if ((!"".equals(user)) && (userService.existByUsername(user))) {
                    usernamesSetBuilder.add(user);
                }
            }

            currentPage = userService.pageUsersActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), usernamesSetBuilder.build());
        }

        LazyHtml content = new LazyHtml(listUsersActivitiesView.render(currentPage, orderBy, orderDir, filterString, form));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.activity.list"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.activities"), routes.UserActivityController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(LoggedIn.class)
    public Result viewOwnActivities() {
        return listOwnActivities(0, "time", "desc", "");
    }

    @Authenticated(LoggedIn.class)
    public Result listOwnActivities(long page, String orderBy, String orderDir, String filterString) {
        String username = IdentityUtils.getUsername();
        Page<UserActivity> currentPage;

        Form<UserActivityFilterForm> form = Form.form(UserActivityFilterForm.class).bindFromRequest();
        if (form.hasErrors() || form.hasGlobalErrors()) {
            currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableSet.of(), username);
        } else {
            UserActivityFilterForm formData = form.get();

            String[] clientNames = formData.clients.split(",");
            ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
            for (String client : clientNames) {
                if ((!"".equals(client)) && (clientService.existByName(client))) {
                    clientNamesSetBuilder.add(client);
                }
            }

            currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);
        }

        LazyHtml content = new LazyHtml(listOwnActivitiesView.render(currentPage, orderBy, orderDir, filterString, form));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.profile()), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewOwnActivities())), c));
        content.appendLayout(c -> headingLayout.render("user.activities", c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("profile.profile"), routes.UserController.profile()),
              new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewOwnActivities())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result viewUserActivities(String username) {
        return listUserActivities(username, 0, "time", "desc", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result listUserActivities(String username, long page, String orderBy, String orderDir, String filterString) {
        if (userService.existByUsername(username)) {
            User user = userService.findUserByUsername(username);
            Page<UserActivity> currentPage;

            Form<UserActivityFilterForm> form = Form.form(UserActivityFilterForm.class).bindFromRequest();
            if (form.hasErrors() || form.hasGlobalErrors()) {
                currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableSet.of(), username);
            } else {
                UserActivityFilterForm formData = form.get();

                String[] clientNames = formData.clients.split(",");
                ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
                for (String client : clientNames) {
                    if ((!"".equals(client)) && (clientService.existByName(client))) {
                        clientNamesSetBuilder.add(client);
                    }
                }

                currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);
            }

            LazyHtml content = new LazyHtml(listUserActivitiesView.render(username, currentPage, orderBy, orderDir, filterString, form));
            content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
            content.appendLayout(c -> headingLayout.render(username, c));
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username)),
                  new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewUserActivities(username))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

}