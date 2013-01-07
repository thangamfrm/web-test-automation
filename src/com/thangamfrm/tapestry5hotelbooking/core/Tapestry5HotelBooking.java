package com.thangamfrm.tapestry5hotelbooking.core;

import org.openqa.selenium.By;

import com.thangamfrm.webtestautomation.core.WebTestAutomation;
import com.thangamfrm.webtestautomation.core.WebTestAutomationProperties;

public class Tapestry5HotelBooking extends WebTestAutomation {

    public Tapestry5HotelBooking() {
        super();
        gotoUrl(getProperty(WebTestAutomationProperties.APPLICATION_UNDER_TEST_URL_PROPERTY));
    }

    public boolean isError() {
        return isElementPresent(By.className("t-error"));
    }

}
