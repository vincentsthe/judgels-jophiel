package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.centerLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.*;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.activationView;
import org.iatoki.judgels.jophiel.views.html.editProfileView;
import org.iatoki.judgels.jophiel.views.html.serviceEditProfileView;
import org.iatoki.judgels.jophiel.views.html.user.*;
import org.iatoki.judgels.jophiel.views.html.viewProfileView;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Transactional
public final class UserProfileController extends BaseController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final UserActivityService userActivityService;

    public UserProfileController(UserService userService, UserProfileService userProfileService, UserActivityService userActivityService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.userActivityService = userActivityService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result profile() {
        return serviceProfile(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postProfile() {
        return postServiceProfile(null);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    public Result viewProfile(String username) {
        if (userService.existByUsername(username)) {
            User user = userService.findUserByUsername(username);

            LazyHtml content = new LazyHtml(viewProfileView.render(user));
            if (IdentityUtils.getUserJid() != null) {
                content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
                content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
                ControllerUtils.getInstance().appendSidebarLayout(content);
                ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                      new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username))
                ));
            } else {
                content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
                content.appendLayout(c -> centerLayout.render(c));
            }
            ControllerUtils.getInstance().appendTemplateLayout(content, "Profile");

            ControllerUtils.getInstance().addActivityLog(userActivityService, "View user profile " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postAvatar() {
        return postServiceAvatar(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result serviceProfile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);

        User user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        UserProfileForm userProfileForm = new UserProfileForm();
        userProfileForm.name = user.getName();

        form = form.fill(userProfileForm);

        ControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update profile <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showProfile(form, form2, continueUrl);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postServiceProfile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class).bindFromRequest();

        if (form.hasErrors()) {
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            Logger.error(form.errors().toString());
            return showProfile(form, form2, continueUrl);
        } else {
            UserProfileForm userProfileForm = form.get();

            if (!"".equals(userProfileForm.password)) {
                if (!userProfileForm.password.equals(userProfileForm.confirmPassword)) {
                    Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                    form.reject("profile.error.passwordsDidntMatch");
                    return showProfile(form, form2, continueUrl);
                }
                userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name, userProfileForm.password);
            } else {
                userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name);
            }

            ControllerUtils.getInstance().addActivityLog(userActivityService, "Update profile.");
            if (continueUrl == null) {
                return redirect(routes.UserProfileController.profile());
            } else {
                return redirect(routes.UserProfileController.serviceProfile(continueUrl));
            }
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = (2 << 20))
    public Result postServiceAvatar(String continueUrl) {
        if (request().body().isMaxSizeExceeded()) {
            Form<UserProfileForm> form = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            form2.reject("profile.error.overSizeLimit");
            return showProfile(form, form2, continueUrl);
        } else {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

            if (avatar != null) {
                String contentType = avatar.getContentType();
                if (!((contentType.equals("image/png")) || (contentType.equals("image/jpg")) || (contentType.equals("image/jpeg")))) {
                    Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                    Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                    form2.reject("error.profile.not_picture");
                    return showProfile(form, form2, continueUrl);
                } else {
                    try {
                        String profilePictureName = userProfileService.updateProfilePicture(IdentityUtils.getUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()));
                        String profilePictureUrl = userProfileService.getAvatarImageUrlString(profilePictureName);
                        try {
                            new URL(profilePictureUrl);
                            session("avatar", profilePictureUrl.toString());
                        } catch (MalformedURLException e) {
                            session("avatar", org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(profilePictureName).absoluteURL(request()));
                        }

                        ControllerUtils.getInstance().addActivityLog(userActivityService, "Update avatar.");

                        if (continueUrl == null) {
                            return redirect(org.iatoki.judgels.jophiel.controllers.routes.UserProfileController.profile());
                        } else {
                            return redirect(routes.UserProfileController.serviceProfile(continueUrl));
                        }
                    } catch (IOException e) {
                        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                        form2.reject("profile.error.cantUpload");
                        return showProfile(form, form2, continueUrl);
                    }
                }
            } else {
                Form<UserProfileForm> form = Form.form(UserProfileForm.class);
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form2.reject("profile.error.not_picture");
                return showProfile(form, form2, continueUrl);
            }
        }
    }

    private Result showProfile(Form<UserProfileForm> form, Form<UserProfilePictureForm> form2, String continueUrl) {
        LazyHtml content;
        if (continueUrl == null) {
            content = new LazyHtml(editProfileView.render(form, form2));
        } else {
            content = new LazyHtml(serviceEditProfileView.render(form, form2, continueUrl));
        }
        content.appendLayout(c -> headingLayout.render(Messages.get("profile.profile"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        if (continueUrl == null) {
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.profile())
            ));
        } else {
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.serviceProfile(continueUrl))
            ));
        }
        ControllerUtils.getInstance().appendTemplateLayout(content, "Profile");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
