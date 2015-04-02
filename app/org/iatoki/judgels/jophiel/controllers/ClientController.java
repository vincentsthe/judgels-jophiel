package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientCreateForm;
import org.iatoki.judgels.jophiel.ClientService;
import org.iatoki.judgels.jophiel.ClientUpdateForm;
import org.iatoki.judgels.jophiel.controllers.security.Authenticated;
import org.iatoki.judgels.jophiel.controllers.security.Authorized;
import org.iatoki.judgels.jophiel.controllers.security.HasRole;
import org.iatoki.judgels.jophiel.controllers.security.LoggedIn;
import org.iatoki.judgels.jophiel.views.html.client.createClientView;
import org.iatoki.judgels.jophiel.views.html.client.listClientsView;
import org.iatoki.judgels.jophiel.views.html.client.updateClientView;
import org.iatoki.judgels.jophiel.views.html.client.viewClientView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class ClientController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Transactional
    public Result index() {
        return listClients(0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result createClient() {
        Form<ClientCreateForm> form = Form.form(ClientCreateForm.class);

        return showCreateClient(form);
    }

    @RequireCSRFCheck
    @Transactional
    public Result postCreateClient() {
        Form<ClientCreateForm> form = Form.form(ClientCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateClient(form);
        } else {
            ClientCreateForm clientCreateForm = form.get();
            clientService.createClient(clientCreateForm.name, clientCreateForm.applicationType, clientCreateForm.scopes, Arrays.asList(clientCreateForm.redirectURIs.split(",")));

            return redirect(routes.ClientController.index());
        }
    }

    @Transactional
    public Result viewClient(long clientId) {
        Client client = clientService.findClientById(clientId);
        LazyHtml content = new LazyHtml(viewClientView.render(client));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.client") + " #" + clientId + ": " + client.getName(), new InternalLink(Messages.get("commons.update"), routes.ClientController.updateClient(clientId)), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
                new InternalLink(Messages.get("client.view"), routes.ClientController.viewClient(clientId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Client - View");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result listClients(long page, String orderBy, String orderDir, String filterString) {
        Page<Client> currentPage = clientService.pageClients(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listClientsView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.list"), new InternalLink(Messages.get("commons.create"), routes.ClientController.createClient()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("client.clients"), routes.ClientController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Clients");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    @Transactional
    public Result updateClient(long clientId) {
        Client client = clientService.findClientById(clientId);
        ClientUpdateForm clientUpdateForm = new ClientUpdateForm();

        clientUpdateForm.name = client.getName();
        clientUpdateForm.redirectURIs = StringUtils.join(client.getRedirectURIs(), ",");
        clientUpdateForm.scopes = Lists.newArrayList(client.getScopes());

        Form<ClientUpdateForm> form = Form.form(ClientUpdateForm.class).fill(clientUpdateForm);

        return showUpdateClient(form, clientId, client.getName());
    }

    @Transactional
    public Result postUpdateClient(long clientId) {
        Client client = clientService.findClientById(clientId);
        Form<ClientUpdateForm> form = Form.form(ClientUpdateForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdateClient(form, clientId, client.getName());
        } else {
            ClientUpdateForm clientUpdateForm = form.get();

            clientService.updateClient(clientId, clientUpdateForm.name, clientUpdateForm.scopes, Arrays.asList(clientUpdateForm.redirectURIs.split(",")));

            return redirect(routes.ClientController.index());
        }
    }

    @Transactional
    public Result deleteClient(long clientId) {
        clientService.deleteClient(clientId);

        return redirect(routes.ClientController.index());
    }

    private Result showCreateClient(Form<ClientCreateForm> form) {
        LazyHtml content = new LazyHtml(createClientView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
              new InternalLink(Messages.get("client.create"), routes.ClientController.createClient())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Client - Create");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateClient(Form<ClientUpdateForm> form, long clientId, String clientName) {
        LazyHtml content = new LazyHtml(updateClientView.render(form, clientId));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.client") + " #" + clientId + ": " + clientName, c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("client.clients"), routes.ClientController.index()),
              new InternalLink(Messages.get("client.update"), routes.ClientController.updateClient(clientId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Client - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
