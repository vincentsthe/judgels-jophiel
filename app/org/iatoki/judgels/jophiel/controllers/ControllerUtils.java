package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.controllers.AbstractControllerUtils;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.profileView;
import org.iatoki.judgels.commons.views.html.layouts.menusLayout;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.UserActivityService;
import org.iatoki.judgels.jophiel.UserService;
import play.i18n.Messages;
import play.mvc.Http;

public final class ControllerUtils extends AbstractControllerUtils {

    private static final ControllerUtils INSTANCE = new ControllerUtils();

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.profile()));
        if (JophielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("user.activities"), routes.UserActivityController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));
        }

        LazyHtml sidebarContent = new LazyHtml(profileView.render(
              IdentityUtils.getUsername(),
              IdentityUtils.getUserRealName(),
              org.iatoki.judgels.jophiel.controllers.routes.UserProfileController.profile().absoluteURL(Http.Context.current().request()),
              org.iatoki.judgels.jophiel.controllers.routes.UserAccountController.logout().absoluteURL(Http.Context.current().request())
        ));
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));
        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
    }

    public void addActivityLog(UserActivityService userActivityService, String log) {
        userActivityService.createUserActivity("localhost", IdentityUtils.getUserJid(), System.currentTimeMillis(), log, IdentityUtils.getIpAddress());
    }

    static ControllerUtils getInstance() {
        return INSTANCE;
    }
}
