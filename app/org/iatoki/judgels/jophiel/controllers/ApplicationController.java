package org.iatoki.judgels.jophiel.controllers;

import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

@Transactional
public final class ApplicationController extends Controller {

    public ApplicationController() {
    }

    public Result index() {
        return redirect(routes.UserController.login().absoluteURL(request()));
    }

}
