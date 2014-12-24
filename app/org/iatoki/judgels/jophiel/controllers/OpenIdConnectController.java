package org.iatoki.judgels.jophiel.controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.noSidebarLayout;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.JophielUtilities;
import org.iatoki.judgels.jophiel.LoginForm;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.UserService;
import org.iatoki.judgels.jophiel.views.html.authView;
import org.iatoki.judgels.jophiel.views.html.loginView;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class OpenIdConnectController extends Controller {

    private UserService userService;
    private ClientService clientService;

    public OpenIdConnectController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    private Result showLogin(Form<LoginForm> form, String continueUrl) {
        LazyHtml content = new LazyHtml(loginView.render(form, continueUrl));
        content.appendLayout(c -> noSidebarLayout.render(c));
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
        return lazyOk(content);
    }

    @AddCSRFToken
    public Result login(String continueUrl) {
        Form<LoginForm> form = Form.form(LoginForm.class);
        return showLogin(form, continueUrl);
    }

    @RequireCSRFCheck
    @Transactional
    public Result postLogin(String continueUrl) {
        Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return showLogin(form, continueUrl);
        } else {
            LoginForm loginData = form.get();
            if (userService.login(loginData.usernameOrEmail, loginData.password)) {
                return redirect(continueUrl);
            } else {
                form.reject("Username or email not found or password do not match");
                return showLogin(form, continueUrl);
            }
        }
    }

    @Transactional
    public Result authRequest() {
        if ((session("user") == null) || (!userService.isUserJidExist(session("user")))) {
            return redirect((routes.OpenIdConnectController.login("http://" + request().host() + request().uri())));
        } else {
            try {
                String randomHash = JophielUtilities.hashMD5(UUID.randomUUID().toString());
                Cache.set(randomHash, request().uri().substring(request().uri().indexOf("?") + 1));

                AuthenticationRequest req = AuthenticationRequest.parse(request().uri().substring(request().uri().indexOf("?") + 1));
                ClientID clientID = req.getClientID();
                Client client = clientService.findClientByJid(clientID.toString());

                List<String> scopes = req.getScope().toStringList();

                LazyHtml content = new LazyHtml(authView.render(randomHash, client, scopes));
                content.appendLayout(c -> noSidebarLayout.render(c));
                content.appendLayout(c -> headerFooterLayout.render(c));
                content.appendLayout(c -> baseLayout.render("TODO", c));
                return lazyOk(content);
            } catch (ParseException e) {
                e.printStackTrace();

                return badRequest();
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

                AuthorizationCode code = clientService.generateAuthorizationCode(client.getJid(), redirectURI.toString(), responseType.toString(), scope.toString());
                URI result = new AuthenticationSuccessResponse(redirectURI, code, null, null, state).toURI();

                String accessToken = clientService.generateAccessToken(code.getValue(), session("user"), clientID.toString(), scope.toString());
                String refreshToken = clientService.generateRefreshToken(code.getValue(), session("user"), clientID.toString(), scope.toString());

                clientService.generateIdToken(code.getValue(), session("user"), userService.findUserByJid(session("user")).getUsername(), client.getJid(), nonce, System.currentTimeMillis(), accessToken);

                return redirect(result.toString());
            } catch (ParseException | SerializeException e) {
                e.printStackTrace();

                return badRequest();
            }

        } else {
            return badRequest();
        }
    }

    @Transactional
    public Result token() {
        DynamicForm form = DynamicForm.form().bindFromRequest();

        String grantType = form.get("grant_type");

        if ("authorization_code".equals(grantType)) {
            String code = form.get("code");
            String redirectUri = form.get("redirect_uri");
            org.iatoki.judgels.jophiel.AuthorizationCode authorizationCode = clientService.findAuthorizationCodeByCode(code);

            if ((authorizationCode.getRedirectURI().equals(redirectUri)) && (!authorizationCode.isExpired())) {
                String scope = form.get("scope");
                String clientId;
                String clientSecret;

                if (request().getHeader("Authorization") != null) {
                    String[] userPass = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1])).split(":");
                    clientId = userPass[0];
                    clientSecret = userPass[1];
                } else {
                    clientId = form.get("client_id");
                    clientSecret = form.get("client_secret");
                }

                Client client = clientService.findClientByJid(clientId);

                if (client.getSecret().equals(clientSecret)) {

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
                            return badRequest();
                        }
                    } else {
                        // TODO add scopes
                        return badRequest();
                    }
                } else {
                    // TODO error authentication
                    return notFound();
                }
            } else {
                // TODO code expired
                return notFound();
            }
        } else if ("refresh_token".equals(grantType)) {
            String refreshToken = form.get("refresh_token");

            // TODO support refresh token
            return TODO;
        } else {
            // TODO not supported grant type (implicit, dll)
            return badRequest();
        }
    }

    public Result userInfo() {
        return TODO;
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
