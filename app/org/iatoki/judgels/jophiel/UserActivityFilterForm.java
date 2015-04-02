package org.iatoki.judgels.jophiel;

public final class UserActivityFilterForm {

    public UserActivityFilterForm() {
        this.clients = "";
        this.users = "";
        this.filterString = "";
    }

    public UserActivityFilterForm(String clients, String users, String filterString) {
        this.clients = clients;
        this.users = users;
        this.filterString = filterString;
    }

    public String clients;

    public String users;

    public String filterString;

}
