package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.*;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientCreateForm;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.ClientUpdateForm;
import org.iatoki.judgels.jophiel.views.html.client.createView;
import org.iatoki.judgels.jophiel.views.html.client.listView;
import org.iatoki.judgels.jophiel.views.html.client.updateView;
import org.iatoki.judgels.jophiel.views.html.client.viewView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;

public final class ClientController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Transactional
    public Result index() {
        return list(0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result create() {
        Form<ClientCreateForm> form = Form.form(ClientCreateForm.class);

        return showCreate(form);
    }

    @RequireCSRFCheck
    @Transactional
    public Result postCreate() {
        Form<ClientCreateForm> form = Form.form(ClientCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreate(form);
        } else {
            ClientCreateForm clientCreateForm = form.get();

            clientService.createClient(clientCreateForm.name, clientCreateForm.applicationType, clientCreateForm.scopes, Arrays.asList(clientCreateForm.redirectURIs.split(",")));

            return redirect(routes.ClientController.index());
        }
    }

    @Transactional
    public Result view(long clientId) {
        Client client = clientService.findClientById(clientId);
        LazyHtml content = new LazyHtml(viewView.render(client));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.client") + " #" + clientId + ": " + client.getName(), new InternalLink(Messages.get("commons.update"), routes.ClientController.update(clientId)), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
                new InternalLink(Messages.get("client.view"), routes.ClientController.view(clientId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showUpdate(Form<ClientUpdateForm> form, long clientId, String clientName) {
        LazyHtml content = new LazyHtml(updateView.render(form, clientId));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.client") + " #" + clientId + ": " + clientName, c));

        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
                new InternalLink(Messages.get("client.update"), routes.ClientController.update(clientId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    @Transactional
    public Result update(long clientId) {
        Client client = clientService.findClientById(clientId);
        ClientUpdateForm clientUpdateForm = new ClientUpdateForm();

        clientUpdateForm.name = client.getName();
        clientUpdateForm.redirectURIs = StringUtils.join(client.getRedirectURIs(), ",");
        clientUpdateForm.scopes = Lists.newArrayList(client.getScopes());

        Form<ClientUpdateForm> form = Form.form(ClientUpdateForm.class).fill(clientUpdateForm);

        return showUpdate(form, clientId, client.getName());
    }

    @Transactional
    public Result postUpdate(long clientId) {
        Client client = clientService.findClientById(clientId);
        Form<ClientUpdateForm> form = Form.form(ClientUpdateForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdate(form, clientId, client.getName());
        } else {
            ClientUpdateForm clientUpdateForm = form.get();

            clientService.updateClient(clientId, clientUpdateForm.name, clientUpdateForm.scopes, Arrays.asList(clientUpdateForm.redirectURIs.split(",")));

            return redirect(routes.ClientController.index());
        }
    }

    @Transactional
    public Result delete(long clientId) {
        clientService.deleteClient(clientId);

        return redirect(routes.ClientController.index());
    }

    @Transactional
    public Result list(long page, String orderBy, String orderDir, String filterString) {
        Page<Client> currentPage = clientService.pageClients(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.list"), new InternalLink(Messages.get("commons.create"), routes.ClientController.create()), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreate(Form<ClientCreateForm> form) {
        LazyHtml content = new LazyHtml(createView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.create"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
                new InternalLink(Messages.get("client.create"), routes.ClientController.create())
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showUpdate(Form<ClientUpdateForm> form, long clientId) {
        LazyHtml content = new LazyHtml(updateView.render(form, clientId));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.update"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
                new InternalLink(Messages.get("client.update"), routes.ClientController.update(clientId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private void appendTemplateLayout(LazyHtml content) {
        content.appendLayout(c -> leftSidebarWithoutProfileLayout.render(ImmutableList.of(
                                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                                new InternalLink(Messages.get("client.clients"), routes.ClientController.index())
                        ), c)
        );

        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
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
