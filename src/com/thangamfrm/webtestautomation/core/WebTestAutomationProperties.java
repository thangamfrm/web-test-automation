
package com.thangamfrm.webtestautomation.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebTestAutomationProperties {

    private static Logger LOG = LoggerFactory.getLogger(WebTestAutomationProperties.class);

    protected static WebTestAutomationProperties instance;
    
    protected static final String PROPERTIES_FILE_PATH = "res/config/webtestautomation.properties";
    protected static final String OVERRIDES_FILE_PATH = "res/config/overrides.properties";

    public static final String BUILD_NUMBER_PROPERTY = "build.number";

    public static final String WEBDRIVER_MODE_PROPERTY = "webdriver.mode";
    public static final String REMOTE_WEBDRIVER_HOST_PROPERTY = "remote.webdriver.host";
    public static final String REMOTE_WEBDRIVER_PORT_PROPERTY = "remote.webdriver.port";
    public static final String SCREENSHOT_DIRECTORY_PROPERTY = "screenshots.dir";

    public static final String APPLICATION_UNDER_TEST_HOST_PROPERTY = "aut.host";
    public static final String APPLICATION_UNDER_TEST_PORT_PROPERTY = "aut.port";
    public static final String APPLICATION_UNDER_TEST_URL_PROPERTY = "aut.url";

    protected Properties properties;

    private WebTestAutomationProperties() {
        try {
            configure();
        } catch (Exception e) {
            throw new WebTestAutomationException("Unable to configure properties!", e);
        }
    }

    public static WebTestAutomationProperties getInstance() {
        if (instance == null) {
            instance = new WebTestAutomationProperties();
        }
        return instance;
    }

    public void configure() throws IOException {

        properties = new Properties();

        loadProperties(PROPERTIES_FILE_PATH);
        loadProperties(OVERRIDES_FILE_PATH);

        setSystemProperty(BUILD_NUMBER_PROPERTY);
        setSystemProperty(WEBDRIVER_MODE_PROPERTY);
        setSystemProperty(REMOTE_WEBDRIVER_HOST_PROPERTY);
        setSystemProperty(REMOTE_WEBDRIVER_PORT_PROPERTY);
        setSystemProperty(APPLICATION_UNDER_TEST_HOST_PROPERTY);
        setSystemProperty(APPLICATION_UNDER_TEST_PORT_PROPERTY);
        setSystemProperty(APPLICATION_UNDER_TEST_URL_PROPERTY);

        if (StringUtils.isBlank(getProperty(BUILD_NUMBER_PROPERTY))) {
            properties.setProperty(BUILD_NUMBER_PROPERTY, new Long(System.currentTimeMillis()).toString());
        }

        LOG.info("Configuration completed! AUT Host: {} , Build: {} , WebDriver Mode: {}", 
                getProperty(APPLICATION_UNDER_TEST_HOST_PROPERTY),
                getProperty(BUILD_NUMBER_PROPERTY),
                getProperty(WEBDRIVER_MODE_PROPERTY));
    }

    private void setSystemProperty(String propertyName) {
        String propertyValue = System.getProperty(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }

    private void loadProperties(String fileName) throws IOException {
        File propertiesFile = new File(fileName.trim());
        if (propertiesFile.exists() && propertiesFile.isFile()) {
            LOG.info("Loading properties file: {}", fileName);
            java.util.Properties overrides = new java.util.Properties();
            FileInputStream in = new FileInputStream(propertiesFile);
            overrides.load(in);
            in.close();
            properties.putAll(overrides);
        } else {
            LOG.info("Ignoring! Properties file does NOT exist: {}", fileName);
        }
    }

    public String getProperty(String property) {
        String value = properties.getProperty(property);
        if (StringUtils.isBlank(value)) {
            return new String();
        }
        StringBuilder sb = new StringBuilder(value);
        while (true) {
            if (sb.indexOf("${") > -1) {
                int start = sb.indexOf("${") + 2;
                int end = sb.indexOf("}");
                String tmpValue = getProperty(sb.substring(start, end));
                sb.replace(start - 2, end + 1, tmpValue);
            } else {
                break;
            }
        }
        return sb.toString().trim();
    }

}
