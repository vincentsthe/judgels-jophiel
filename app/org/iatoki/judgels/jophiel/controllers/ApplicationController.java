package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.commons.controllers.BaseController;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

@Transactional
public final class ApplicationController extends BaseController {

    public ApplicationController() {
    }

    public Result index() {
        return redirect(routes.UserController.login().absoluteURL(request()));
    }

}
