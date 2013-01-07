package com.thangamfrm.tapestry5hotelbooking.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thangamfrm.tapestry5hotelbooking.core.Tapestry5HotelBooking;
import com.thangamfrm.webtestautomation.core.WebTestAutomationException;
import com.thangamfrm.webtestautomation.core.WebTestAutomationProperties;

public class TestLogin extends Assert {

    private static Logger LOG = LoggerFactory.getLogger(TestLogin.class);

    private Tapestry5HotelBooking application;

    @BeforeClass
    public void setup() {
        application = new Tapestry5HotelBooking();
    }

    @Test
    public void testAccountRegistration() {
       try {
            String buildNumber = application.getProperty(WebTestAutomationProperties.BUILD_NUMBER_PROPERTY);
            application.setInputField("username:", "tu" + buildNumber);
            application.setInputField("password:", "pass1234");

            assertTrue(application.isButtonPresent("Login"));

            application.clickButton("Login");
        } catch (Exception e) {
            LOG.error(this.getClass().getSimpleName() + " Failed!", e);
            application.captureScreenshot(this.getClass().getSimpleName());
            throw new WebTestAutomationException(e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        application.closeBrowser();
    }

}
