package org.iatoki.judgels.jophiel.controllers;

import play.*;
import play.data.DynamicForm;
import play.mvc.*;

import play.twirl.api.Html;
import views.html.*;

import org.iatoki.judgels.commons.views.html.layouts.main;

public class Application extends Controller {

    public static Result index() {
        DynamicForm data = DynamicForm.form().bindFromRequest();

        return ok(index.render("Jophiel"));
    }
}
