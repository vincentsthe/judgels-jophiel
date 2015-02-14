package org.iatoki.judgels.jophiel.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.noSidebarLayout;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.LoginForm;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserAutoComplete;
import org.iatoki.judgels.jophiel.UserProfileForm;
import org.iatoki.judgels.jophiel.UserProfilePictureForm;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.authView;
import org.iatoki.judgels.jophiel.views.html.serviceLoginView;
import org.iatoki.judgels.jophiel.views.html.serviceProfileView;
import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class OpenIdConnectController extends Controller {

    private final UserService userService;
    private final ClientService clientService;

    public OpenIdConnectController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @AddCSRFToken
    @Transactional
    public Result login(String continueUrl) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class);
            return showLogin(form, continueUrl);
        } else {
            return redirect(continueUrl);
        }
    }

    @RequireCSRFCheck
    @Transactional
    public Result postLogin(String continueUrl) {
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
            if (form.hasErrors()) {
                Logger.error(form.errors().toString());
                return showLogin(form, continueUrl);
            } else {
                LoginForm loginData = form.get();
                if (userService.login(loginData.usernameOrEmail, loginData.password)) {
                    return redirect(continueUrl);
                } else {
                    form.reject("error.login.usernameOrEmailOrPassword.invalid");
                    return showLogin(form, continueUrl);
                }
            }
        } else {
            return redirect(continueUrl);
        }
    }

    @Transactional
    public Result authRequest() {
        String redirectURI = request().uri().substring(request().uri().indexOf("?") + 1);
        if ((IdentityUtils.getUserJid() == null) || (!userService.existsByJid(IdentityUtils.getUserJid()))) {
            return redirect((routes.OpenIdConnectController.login("http://" + request().host() + request().uri())));
        } else {
            try {
                String randomHash = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
                Cache.set(randomHash, request().uri().substring(request().uri().indexOf("?") + 1));

                AuthenticationRequest req = AuthenticationRequest.parse(redirectURI);
                ClientID clientID = req.getClientID();
                Client client = clientService.findClientByJid(clientID.toString());

                List<String> scopes = req.getScope().toStringList();
                if (clientService.checkIsClientAuthorized(clientID.toString(), scopes)) {
                    return postAuthRequest(randomHash);
                } else {
                    LazyHtml content = new LazyHtml(authView.render(randomHash, client, scopes));
                    content.appendLayout(c -> noSidebarLayout.render(c));
                    content.appendLayout(c -> headerFooterLayout.render(c));
                    content.appendLayout(c -> baseLayout.render("TODO", c));
                    return lazyOk(content);
                }
            } catch (ParseException e) {
                Logger.error("Exception when parsing authentication request.", e);
                return redirect(redirectURI + "?error=invalid_request");
            }
        }
    }

    @Transactional
    public Result postAuthRequest(String hash) {
        Object path = Cache.get(hash);
        if (path != null) {
            try {
                AuthenticationRequest req = AuthenticationRequest.parse(path.toString());
                ClientID clientID = req.getClientID();
                Client client = clientService.findClientByJid(clientID.toString());
                URI redirectURI = req.getRedirectionURI();
                ResponseType responseType = req.getResponseType();
                State state = req.getState();
                Scope scope = req.getScope();
                String nonce = (req.getNonce() != null) ? req.getNonce().toString() : "";

                AuthorizationCode code = clientService.generateAuthorizationCode(client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList());
                String accessToken = clientService.generateAccessToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
                clientService.generateRefreshToken(code.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
                clientService.generateIdToken(code.getValue(), IdentityUtils.getUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken);
                URI result = new AuthenticationSuccessResponse(redirectURI, code, null, null, state).toURI();

                response().setCookie("JOID-" + client.getJid(), clientService.findIdTokenByCode(code.getValue()).getToken(), null, "/", "." + result.getHost(), false, true);
                return redirect(result.toString());
            } catch (ParseException | SerializeException e) {
                Logger.error("Exception when parsing authentication request.", e);
                return redirect(path + "?error=invalid_request");
            }
        } else {
            throw new RuntimeException("This exception should never happened.");
        }
    }

    @Transactional
    public Result token() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String grantType = form.get("grant_type");

        if ("authorization_code".equals(grantType)) {
            return processTokenAuthCodeRequest(form);
        } else if ("refresh_token".equals(grantType)) {
            return processTokenRefreshTokenRequest(form);
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_grant");
            return badRequest(node);
        }
    }

    private Result processTokenAuthCodeRequest(DynamicForm form) {
        String code = form.get("code");
        String redirectUri = form.get("redirect_uri");
        org.iatoki.judgels.jophiel.AuthorizationCode authorizationCode = clientService.findAuthorizationCodeByCode(code);

        if ((authorizationCode.getRedirectURI().equals(redirectUri)) && (!authorizationCode.isExpired())) {
            String scope = form.get("scope");
            String clientId;
            String clientSecret;
            if ((request().getHeader("Authorization") != null) && ("Basic".equals(request().getHeader("Authorization").split(" ")[0]))) {
                String[] userPass = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1])).split(":");
                clientId = userPass[0];
                clientSecret = userPass[1];
            } else {
                clientId = form.get("client_id");
                clientSecret = form.get("client_secret");
            }

            if (clientId != null) {
                Client client = clientService.findClientByJid(clientId);
                if ((client.getSecret().equals(clientSecret)) && (authorizationCode.getClientJid().equals(client.getJid()))) {
                    Set<String> addedSet = Arrays.asList(scope.split(" ")).stream()
                            .filter(s -> (!"".equals(s)) && (!client.getScopes().contains(StringUtils.upperCase(s))))
                            .collect(Collectors.toSet());
                    if (addedSet.isEmpty()) {
                        ObjectNode result = Json.newObject();
                        AccessToken accessToken = clientService.findAccessTokenByCode(code);
                        if (!accessToken.isRedeemed()) {
                            result.put("access_token", accessToken.getToken());
                            if (client.getScopes().contains("OFFLINE_ACCESS")) {
                                RefreshToken refreshToken = clientService.findRefreshTokenByCode(code);
                                result.put("refresh_token", refreshToken.getToken());
                                clientService.redeemRefreshTokenById(refreshToken.getId());
                            }
                            if (client.getScopes().contains("OPENID")) {
                                IdToken idToken = clientService.findIdTokenByCode(code);
                                result.put("id_token", idToken.getToken());
                                clientService.redeemIdTokenById(idToken.getId());
                            }
                            result.put("token_type", "Bearer");
                            result.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId()));
                            return ok(result);
                        } else {
                            ObjectNode node = Json.newObject();
                            node.put("error", "invalid_client");
                            return badRequest(node);
                        }
                    } else {
                        ObjectNode node = Json.newObject();
                        node.put("error", "invalid_scope");
                        return badRequest(node);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "unauthorized_client");
                    return unauthorized(node);
                }
            } else {
                ObjectNode node = Json.newObject();
                node.put("error", "invalid_client");
                return badRequest(node);
            }
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_request");
            return badRequest(node);
        }
    }

    private Result processTokenRefreshTokenRequest(DynamicForm form) {
        String refreshToken = form.get("refresh_token");

        RefreshToken refreshToken1 = clientService.findRefreshTokenByRefreshToken(refreshToken);
        if ((refreshToken1.getToken().equals(refreshToken)) && (refreshToken1.isRedeemed())) {
            String clientId;
            String clientSecret;
            if ((request().getHeader("Authorization") != null) && ("Basic".equals(request().getHeader("Authorization").split(" ")[0]))) {
                String[] userPass = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1])).split(":");
                clientId = userPass[0];
                clientSecret = userPass[1];
            } else {
                clientId = form.get("client_id");
                clientSecret = form.get("client_secret");
            }

            if (clientId != null) {
                Client client = clientService.findClientByJid(clientId);
                if ((client.getSecret().equals(clientSecret)) && (refreshToken1.getClientJid().equals(client.getJid()))) {
                    ObjectNode result = Json.newObject();
                    if (refreshToken1.isRedeemed()) {
                        AccessToken accessToken = clientService.regenerateAccessToken(refreshToken1.getCode(), refreshToken1.getUserJid(), refreshToken1.getClientJid(), Arrays.asList(refreshToken1.getScopes().split(",")));
                        result.put("access_token", accessToken.getToken());
                        if (client.getScopes().contains("OPENID")) {
                            IdToken idToken = clientService.findIdTokenByCode(refreshToken1.getCode());
                            result.put("id_token", idToken.getToken());
                            clientService.redeemIdTokenById(idToken.getId());
                        }
                        result.put("token_type", "Bearer");
                        result.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId()));
                        return ok(result);
                    } else {
                        ObjectNode node = Json.newObject();
                        node.put("error", "invalid_client");
                        return badRequest(node);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "unauthorized_client");
                    return unauthorized(node);
                }
            } else {
                ObjectNode node = Json.newObject();
                node.put("error", "invalid_client");
                return badRequest(node);
            }
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_request");
            return badRequest(node);
        }
    }

    @Transactional
    public Result userInfo() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.checkIsAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);
            User user = userService.findUserByJid(accessToken.getUserJid());
            ObjectNode result = Json.newObject();
            result.put("sub", user.getJid());
            result.put("name", user.getName());
            result.put("preferred_username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("picture", user.getProfilePictureUrl().toString());
            return ok(result);
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_token");
            return unauthorized(node);
        }
    }

    @Transactional
    public Result userAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.checkIsAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);
            User user = userService.findUserByJid(accessToken.getUserJid());
            String term = form.get("term");
            List<User> users = userService.findAllUser(term);
            ImmutableList.Builder<UserAutoComplete> responseBuilder = ImmutableList.builder();

            for (User user1 : users) {
                responseBuilder.add(new UserAutoComplete(user1.getJid(), user1.getJid(), user1.getUsername() + " (" + user1.getName() + ")"));
            }
            return ok(Json.toJson(responseBuilder.build()));
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_token");
            return unauthorized(node);
        }
    }

    public Result checkPreUserAutocompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "GET");    // Only allow POST
        response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");         // Ensure this header is also allowed!
        return ok();
    }

    @Transactional
    public Result verifyUserJid() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.checkIsAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);
            User user = userService.findUserByJid(accessToken.getUserJid());
            String userJid = form.get("userJid");

            if (userService.existsByJid(userJid)) {
                return ok();
            } else {
                return notFound();
            }
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_token");
            return unauthorized(node);
        }
    }

    @Transactional
    public Result getInfoByUserJid() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.checkIsAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);
            User user = userService.findUserByJid(accessToken.getUserJid());
            String userJid = form.get("userJid");
            User response = userService.findUserByJid(userJid);

            return ok(Json.toJson(response));
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_token");
            return unauthorized(node);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result profile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        form = form.fill(new UserProfileForm(user));

        return showProfile(form, form2, continueUrl);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postProfile(String continueUrl) {
        Form<UserProfileForm> form = Form.form(UserProfileForm.class).bindFromRequest();

        if (form.hasErrors()) {
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            Logger.error(form.errors().toString());
            return showProfile(form, form2, continueUrl);
        } else {
            UserProfileForm userProfileForm = form.get();
            if (("".equals(userProfileForm.password) && ("".equals(userProfileForm.confirmPassword)))) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name);
                return redirect(continueUrl);
            } else if (userProfileForm.password.equals(userProfileForm.confirmPassword)) {
                userService.updateProfile(IdentityUtils.getUserJid(), userProfileForm.name, userProfileForm.password);
                return redirect(continueUrl);
            } else {
                Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
                form.reject("error.profile.password.not_match");
                Logger.error("Password do not match.");
                return showProfile(form, form2, continueUrl);
            }
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postAvatar(String continueUrl) {
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
                URL profilePictureUrl = userService.updateProfilePicture(IdentityUtils.getUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()));
                session("avatar", profilePictureUrl.toString());
                return redirect(org.iatoki.judgels.jophiel.controllers.routes.OpenIdConnectController.profile(continueUrl));
            }
        } else {
            Form<UserProfileForm> form = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> form2 = Form.form(UserProfilePictureForm.class);
            return showProfile(form, form2, continueUrl);
        }
    }

    private Result showProfile(Form<UserProfileForm> form, Form<UserProfilePictureForm> form2, String continueUrl) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();

        if (JophielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));
        }

        LazyHtml content = new LazyHtml(serviceProfileView.render(form, form2, continueUrl));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.profile"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.profile"), routes.OpenIdConnectController.profile(continueUrl))
        ), c));
        content.appendLayout(c -> leftSidebarLayout.render(
                        IdentityUtils.getUsername(),
                        IdentityUtils.getUserRealName(),
                        org.iatoki.judgels.jophiel.controllers.routes.OpenIdConnectController.profile(continueUrl).absoluteURL(request()),
                        org.iatoki.judgels.jophiel.controllers.routes.OpenIdConnectController.logout(continueUrl).absoluteURL(request()),
                        internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
        return lazyOk(content);
    }

    public Result logout(String returnUri) {
        List<Client> clients = clientService.findAll();
        for (Client client : clients) {
            for (String uRI : client.getRedirectURIs()) {
                URI uri = URI.create(uRI);
                response().setCookie("JOID-" + client.getJid(), "EXPIRED", 0, "/", "." + uri.getHost(), false, true);
            }
        }
        session().clear();
        return redirect(returnUri);
    }

    private Result showLogin(Form<LoginForm> form, String continueUrl) {
        LazyHtml content = new LazyHtml(serviceLoginView.render(form, continueUrl));
        content.appendLayout(c -> noSidebarLayout.render(c));
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
        return lazyOk(content);
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
