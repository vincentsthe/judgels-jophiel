package org.iatoki.judgels.jophiel;

public final class EmailNotVerifiedException extends Exception {

    public EmailNotVerifiedException() {
    }

    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailNotVerifiedException(Throwable cause) {
        super(cause);
    }

    public EmailNotVerifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
