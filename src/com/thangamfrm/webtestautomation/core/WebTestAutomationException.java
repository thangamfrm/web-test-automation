package com.thangamfrm.webtestautomation.core;

public class WebTestAutomationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WebTestAutomationException() {
        super();
    }

    public WebTestAutomationException(String cause) {
        super(cause);
    }

    public WebTestAutomationException(String cause, Throwable throwable) {
        super(cause, throwable);
    }
}
