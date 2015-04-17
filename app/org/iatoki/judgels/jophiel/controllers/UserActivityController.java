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
import org.iatoki.judgels.jophiel.commons.UserActivity;
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
import play.mvc.Http;
import play.mvc.Result;

@Transactional
public final class UserActivityController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final ClientService clientService;
    private final UserService userService;

    public UserActivityController(ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.userService = userService;
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result index() {
        return listUsersActivities(0, "time", "desc", "", "", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result listUsersActivities(long page, String orderBy, String orderDir, String filterString, String clientNames, String usernames) {
        String [] clientName = clientNames.split(",");
        ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
        for (String client : clientName) {
            if ((!"".equals(client)) && (clientService.existByName(client))) {
                clientNamesSetBuilder.add(client);
            }
        }
        String [] username = usernames.split(",");
        ImmutableSet.Builder<String> usernamesSetBuilder = ImmutableSet.builder();
        for (String user : username) {
            if ((!"".equals(user)) && (userService.existByUsername(user))) {
                usernamesSetBuilder.add(user);
            }
        }

        Page<UserActivity> currentPage = userService.pageUsersActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), usernamesSetBuilder.build());

        LazyHtml content = new LazyHtml(listUsersActivitiesView.render(currentPage, orderBy, orderDir, filterString, clientNames, usernames));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.activity.list"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.activities"), routes.UserActivityController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        ControllerUtils.getInstance().addActivityLog(userService, "Open all user activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(LoggedIn.class)
    public Result viewOwnActivities() {
        return listOwnActivities(0, "time", "desc", "", "");
    }

    @Authenticated(LoggedIn.class)
    public Result listOwnActivities(long page, String orderBy, String orderDir, String filterString, String clientNames) {
        String username = IdentityUtils.getUsername();

        String[] clientName = clientNames.split(",");
        ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
        for (String client : clientName) {
            if ((!"".equals(client)) && (clientService.existByName(client))) {
                clientNamesSetBuilder.add(client);
            }
        }

        Page<UserActivity> currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);

        LazyHtml content = new LazyHtml(listOwnActivitiesView.render(currentPage, orderBy, orderDir, filterString, clientNames));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.profile()), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewOwnActivities())), c));
        content.appendLayout(c -> headingLayout.render("user.activities", c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("profile.profile"), routes.UserController.profile()),
              new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewOwnActivities())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        ControllerUtils.getInstance().addActivityLog(userService, "Open own activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result viewUserActivities(String username) {
        return listUserActivities(username, 0, "time", "desc", "", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result listUserActivities(String username, long page, String orderBy, String orderDir, String filterString, String clientNames) {
        if (userService.existByUsername(username)) {
            User user = userService.findUserByUsername(username);
            String[] clientName = clientNames.split(",");
            ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
            for (String client : clientName) {
                if ((!"".equals(client)) && (clientService.existByName(client))) {
                    clientNamesSetBuilder.add(client);
                }
            }

            Page<UserActivity> currentPage = userService.pageUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);

            LazyHtml content = new LazyHtml(listUserActivitiesView.render(username, currentPage, orderBy, orderDir, filterString, clientNames));
            content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
            content.appendLayout(c -> headingLayout.render(username, c));
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserController.viewProfile(username)),
                  new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewUserActivities(username))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

            ControllerUtils.getInstance().addActivityLog(userService, "Open user " + user.getUsername() + " activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

}
