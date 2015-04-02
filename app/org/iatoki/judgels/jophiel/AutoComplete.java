package org.iatoki.judgels.jophiel;

public final class AutoComplete {

    private String id;

    private String value;

    private String label;

    public AutoComplete(String id, String value, String label) {
        this.id = id;
        this.value = value;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
