
package com.thangamfrm.webtestautomation.core;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebTestAutomation {

    private static Logger LOG = LoggerFactory.getLogger(WebTestAutomation.class);

    public static final String WEBDRIVER_MODE_LOCAL = "local";
    public static final String WEBDRIVER_MODE_REMOTE = "remote";
    public static final String WEBDRIVER_MODE_SAUCE = "sauce";
    public static final int WAIT_FOR_DYNAMIC_CONTENT = 5; // seconds

    protected WebDriver driver;

    public WebTestAutomation() {
        openFireFoxBrowser();
    }

    protected void openFireFoxBrowser() {
        LOG.info("Opening FireFox Browser!");
        try {
            DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
            desiredCapabilities.setJavascriptEnabled(true);
            desiredCapabilities.setCapability(FirefoxDriver.PROFILE, new FirefoxProfile());
            desiredCapabilities.setBrowserName("firefox");

            if (isRemoteWebDriver()) {
                LOG.info("WebDriver Mode - Remote");
                driver = new RemoteWebDriver(getRemoteWebDriverURL(), desiredCapabilities);
            } else if (isSauceLabsWebDriver()) {
                LOG.info("WebDriver Mode - Sauce");
                driver = new RemoteWebDriver(getSauceLabsWebDriverURL(), desiredCapabilities);
            } else {
                 LOG.info("WebDriver Mode - Local");
                driver = new FirefoxDriver(desiredCapabilities);
            }
        } catch (Exception e) {
            String msg = "Unable to open Firefox browser!";
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
        LOG.info("Firefox Browser is ready!");
    }

    protected boolean isRemoteWebDriver() {
        return getProperty(WebTestAutomationProperties.WEBDRIVER_MODE_PROPERTY).equalsIgnoreCase(WEBDRIVER_MODE_REMOTE) ? 
            Boolean.TRUE : Boolean.FALSE;
    }

    protected boolean isSauceLabsWebDriver() {
        return getProperty(WebTestAutomationProperties.WEBDRIVER_MODE_PROPERTY).equalsIgnoreCase(WEBDRIVER_MODE_SAUCE) ? 
                Boolean.TRUE : Boolean.FALSE;
    }

    protected URL getRemoteWebDriverURL() throws MalformedURLException {
        String host = getProperty(WebTestAutomationProperties.REMOTE_WEBDRIVER_HOST_PROPERTY);
        String port = getProperty(WebTestAutomationProperties.REMOTE_WEBDRIVER_PORT_PROPERTY);
        LOG.info(String.format("Remote WebDriver Host:Port - %s:%s", host, port));
        if (StringUtils.isBlank(host)) {
            throw new WebTestAutomationException(String.format("Invalid parameters! Remote WebDriver host:port - %s:%s",
                    host, port));
        }
        return new URL(String.format("http://%s:%s/wd/hub", host, port));
    }

    protected URL getSauceLabsWebDriverURL() throws MalformedURLException {
        String host = getProperty(WebTestAutomationProperties.REMOTE_WEBDRIVER_HOST_PROPERTY);
        String port = getProperty(WebTestAutomationProperties.REMOTE_WEBDRIVER_PORT_PROPERTY);
        String userName = getProperty(WebTestAutomationProperties.SAUCELABS_WEBDRIVER_USERNAME_PROPERTY);
        String key = getProperty(WebTestAutomationProperties.SAUCELABS_WEBDRIVER_KEY_PROPERTY);
        LOG.info(String.format("SauceLabs WebDriver Host:Port - %s:%s , User: %s", host, port, userName));
        if (StringUtils.isBlank(host)) {
            throw new WebTestAutomationException(String.format("Invalid parameters! Sauce WebDriver host:port - %s:%s",
                    host, port));
        }
        return new URL(String.format("http://%s:%s@%s:%s/wd/hub", userName, key, host, port));
    }

    public String getProperty(String property) {
        return WebTestAutomationProperties.getInstance().getProperty(property);
    }

    public void gotoUrl(String url) {
        LOG.info("Navigating to URL: {}", url);
        try {
            driver.get(url);
        } catch (Exception e) {
            String msg = String.format("Unable to navigate to URL: %s", url);
            LOG.error(msg ,e);
            throw new WebTestAutomationException(msg);
        }
    }

    public void clickLink(String linkText) {
        try {
            LOG.info("Click link: {}", linkText);
            driver.findElement(By.linkText(linkText)).click();
        } catch (Exception e) {
            String msg = String.format("Unable to click link: %s", linkText);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public boolean isLinkPresent(String linkText) {
        LOG.info("Check for link: {}", linkText);
        try {
            driver.findElement(By.linkText(linkText));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLabelPresent(String label) {
        try {
            getInputField(label);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickButton(String buttonText) {
        WebElement buttonElement = getButtonElement(buttonText);
        try {
            LOG.info("Click button: {}", buttonText);
            buttonElement.click();
        } catch (Exception e) {
            String msg = String.format("Unable to click on button: %s", buttonText);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public String getPageTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            String msg = "Unable to get page title!";
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    protected WebElement getInputField(String label) {
        LOG.info("Get input field Label: {}", label);
        WebElement element = null;
        
        try {
            String xpath = String.format("//label[starts-with(.,'%s')]", label);
            element = driver.findElement(By.xpath(xpath));
        } catch (Exception e) {
            String xpath = String.format("//label[text()[contains(.,'%s')]]", label);
            element = driver.findElement(By.xpath(xpath));
        }
        
        if (element == null) {
        	throw new WebTestAutomationException(String.format("Unable to get input field with label: %s", label));
        }
      
        String forAttr = element.getAttribute("for");
        if (StringUtils.isBlank(forAttr)) {
            throw new WebTestAutomationException(String.format("Label element does NOT contain 'for' attribute! Label: ",
                    label));
        }
        
        try {
            return driver.findElement(By.id(forAttr));
        } catch (NoSuchElementException nsee) {
            throw new WebTestAutomationException(String.format(
                    "Unable to get input field! label: %s and id/name attribute: %s", label, forAttr));
        }
        
    }

    public void setInputField(String label, String text) {
        WebElement inputFieldElement = getInputField(label);
        try {
            inputFieldElement.sendKeys(text);
        } catch (Exception e) {
            String msg = String.format("Input field: %s , Unable to set text: %s", label, text);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public String getInputFieldText(String label) {
        WebElement inputFieldElement = getInputField(label);
        try {
        	return inputFieldElement.getAttribute("value").trim();
        } catch (Exception e) {
            String msg = String.format("Unable to get input field text with label: %s", label);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public boolean isChoiceChecked(String label) {
        WebElement choiceElement = getInputField(label);
        try {
            return choiceElement.isSelected();
        } catch (Exception e) {
            String msg = String.format("Unable to check whether choice is selected? label: %s", label);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
        
    }

    public void checkChoice(String label) {
        WebElement choiceElement = getInputField(label);
        try {
            if (!choiceElement.isSelected()) {
                choiceElement.click();
            } else {
                LOG.info("Choice with label: '{}' Already checked!", label);
            }
        } catch (Exception e) {
            String msg = String.format("Unable to check choice! Label: %s", label);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public void uncheckChoice(String label) {
        WebElement choiceElement = getInputField(label);
        try {
            if (choiceElement.isSelected()) {
                choiceElement.click();
            } else {
                LOG.info("Choice with label: '{}' Already unchecked!", label);
            }
        } catch (Exception e) {
            String msg = String.format("Unable to uncheckChoice! Label: %s", label);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public boolean isTextPresent(String text) {
        try {
            return isTextPresent(getBodyElement(), text);
        } catch (Exception e) {
            String msg = String.format("Unable to verify presence of text: %s", text);
            LOG.error(msg, e);
            throw new WebTestAutomationException(msg);
        }
    }

    public boolean isTextPresent(WebElement element, String text) {
        return element.getText().contains(text);
    }

    public boolean isButtonPresent(String buttonText) {
        return getButtonElement(buttonText) != null ? true : false;
    }

    public WebElement getButtonElement(String buttonText) {
        try {
            List<WebElement> buttonElements = driver.findElements(By.tagName("button"));
            for (WebElement element : buttonElements) {
                if (element.getAttribute("title").equals(buttonText)
                        || element.getAttribute("value").equals(buttonText) || element.getText().equals(buttonText)) {
                    return element;
                }
            }
        } catch (NoSuchElementException nsee) {
            // ignore!
        }

        try {
            return driver.findElement(By.xpath(String.format("//input[@value = '%s']", buttonText)));
        } catch (NoSuchElementException nsee) {
            throw new WebTestAutomationException(String.format("Unable to get button element! Text: %s", buttonText));
        }
    }

    public void captureScreenshot(String fileName) {
        try {
            File screenshotDir = new File(getProperty(WebTestAutomationProperties.SCREENSHOT_DIRECTORY_PROPERTY));
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            FileOutputStream out =
                    new FileOutputStream(String.format("%s%s%s.png", screenshotDir, File.separator, fileName));
            if (isRemoteWebDriver()) {
                out.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            } else {
                out.write(((TakesScreenshot) new Augmenter().augment(driver)).getScreenshotAs(OutputType.BYTES));
            }
            out.close();
            LOG.info("Captured screenshot to file: {}", fileName);
        } catch (Exception e) {
            LOG.error("Unable to capture screenshot to file: {}", fileName, e);
        }
    }

    public WebElement getBodyElement() {
        try {
            return driver.findElement(By.tagName("body"));
        } catch (Exception e) {
            throw new WebTestAutomationException("Body element not found!");
        }
    }

    public boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException nsee) {
            return false;
        }
    }

    public WebElement getElement(By by) {
        return driver.findElement(by);
    }

    public void closeBrowser() {
        try {
            if (isRemoteWebDriver()) {
                driver.close();
            } else {
                driver.quit();
            }
        } catch (Exception e) {
            // ignore
            LOG.error("Unable to close the browser!");
        }
    }

}
