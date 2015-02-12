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
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.UserUpsertForm;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.registerView;
import org.iatoki.judgels.jophiel.views.html.user.createView;
import org.iatoki.judgels.jophiel.views.html.user.listView;
import org.iatoki.judgels.jophiel.views.html.user.updateView;
import org.iatoki.judgels.jophiel.views.html.user.viewView;
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

import java.util.Arrays;

@Transactional
public final class ApplicationController extends Controller {

    public ApplicationController() {
    }

    public Result index() {
        return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserController.register().absoluteURL(request()));
    }

}
