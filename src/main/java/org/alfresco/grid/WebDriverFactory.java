/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.grid;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.po.Browser;
import org.alfresco.po.BrowserPreference;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.beans.factory.FactoryBean;
/**
 * A WebDrone factory that implements spring base bean factory.
 * The factory creates new instances of {@link WebDriver} 
 * based on the browser type that determines which {@link WebDriver} 
 * implementation to use.
 * <p>This factory is able to create instances of the {@link WebDriver}
 * however it is unable to destroy the beans and is up to deveopler to ensure that instance of
 * WebDrone are destroyed once finished.</p>
 * 
 * @author Michaek Suzuki
 * @author Shan Nagarajan
 * @since 1.0
 */
public class WebDriverFactory implements FactoryBean<WebDriver>
{
    private static final String CHROME_SERVER_DRIVER_PATH = "webdriver.chrome.driver";
    private static final String SAFARI_SERVER_DRIVER_PATH = "webdriver.safari.driver";
    private static final String IE_SERVER_DRIVER_PATH = "webdriver.ie.driver";
    private String gridUrl;
    private String chromeServerPath;
    private String safariServerPath;
    private String ieServerPath;
    private String downloadDirectory;
    private String mimeTypes;
    private Map<BrowserPreference, Object> preferences;
    
    public WebDriver getObject(Browser browser)
    {
        switch (browser)
        {
            case FireFox:
                return getFireFox(false);
            case Chrome:
                return getChromeDriver();
            case HtmlUnit:
                return new HtmlUnitDriver(true);
            case IE:
                return getInternetExplorerDriver();
            case Safari:
                return getSafariDriver();
            case RemoteFireFox:
                DesiredCapabilities capabilities = new DesiredCapabilities();
                capabilities.setBrowserName(BrowserType.FIREFOX);
                capabilities.setJavascriptEnabled(true);
                FirefoxProfile profile = createProfile();
                //The below two preferences added to disable the firefox auto update
                profile.setPreference("app.update.auto", false);
                profile.setPreference("app.update.enabled", false);
                profile.setEnableNativeEvents(true);
                capabilities.setCapability(FirefoxDriver.PROFILE, profile);
                return getRemoteDriver(capabilities);
            case RemoteChrome:
                DesiredCapabilities chromeCapabilities = DesiredCapabilities.chrome();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--kiosk");
                chromeCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
                return getRemoteDriver(chromeCapabilities);
            case RemoteIE:
                DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                return getRemoteDriver(ieCapabilities);
            case FireFoxDownloadToDir:
                return getFireFox(true);
            default:
                throw new IllegalArgumentException("Invalid browser specified");
        }
    }
    /**
     * Create a basic fire fox profile.
     * @return {@link FirefoxProfile}
     */
    private FirefoxProfile createProfile(String ... profile)
    {
        FirefoxProfile firefoxProfile = null;
        if(profile.length > 0)
        {
            firefoxProfile = new ProfilesIni().getProfile(profile[0]);
            if(firefoxProfile == null)
            {
                throw new RuntimeException("The following profile: %s can not be found");
            }
        }
        else
        {
            firefoxProfile = new FirefoxProfile();
        }
        //Change default retry timeout of 30 minutes to 2 minutes
        firefoxProfile.setPreference("dom.mms.retrievalRetryIntervals;", "60000,120000");
        //Set it to not store session history, reduced memory foot print.
        firefoxProfile.setPreference("browser.sessionhistory.max_total_viewers;","0");
        return firefoxProfile;
    }
    /**
     * Creates a new instance of an {@link FirefoxDriver} 
     * @param boolean with a specific profile or false for default FireFox
     * @return {@link DesiredCapabilities} type of browser capability 
     * @throws UnsupportedOperationException if grid url is invalid
     */
    private FirefoxDriver getFireFox(boolean customProfile)
    {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        FirefoxProfile firefoxProfile = createProfile();
        //The below two preferences added to disable the firefox auto update
        firefoxProfile.setPreference("app.update.auto", false);
        firefoxProfile.setPreference("app.update.enabled", false);
        firefoxProfile.setEnableNativeEvents(true);
        if(customProfile)
        {
            firefoxProfile.setPreference("browser.download.folderList", 2);
            firefoxProfile.setPreference("browser.download.dir", downloadDirectory);
            firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", mimeTypes);
            
        }
        if(preferences != null && preferences.size() > 0)
        {
            Set<BrowserPreference> preferenceSet = preferences.keySet();
            for (BrowserPreference browserPreference : preferenceSet)
            {
                if(BrowserPreference.Language.equals(browserPreference))
                {
                    firefoxProfile.setPreference(browserPreference.getFireFoxKey(), formatLocale((Locale)preferences.get(browserPreference)));
                }
                else
                {   
                    if (BrowserPreference.DownloadFolderList.equals(browserPreference))
                    {
                        firefoxProfile.setPreference(browserPreference.getFireFoxKey(), Integer.valueOf((String)preferences.get(browserPreference)));
                    } 
                    else
                    {
                        firefoxProfile.setPreference(browserPreference.getFireFoxKey(), (String)preferences.get(browserPreference));
                    }
                }
            }
        }
        capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
        return new FirefoxDriver(capabilities);
    }
    
    /**
     * Converts locale to string. 
     * @param locale {@link Locale} locale
     * @return String locale in string
     */
    private String formatLocale(Locale locale)
    {
        if(locale == null) { throw new IllegalArgumentException("Locale value is required"); }
        return locale.getCountry().isEmpty() ? locale.getLanguage() : locale.getLanguage() + "-" + locale.getCountry().toLowerCase();
    }
    
    /**
     * Creates a new instance of an {@link WebDriver}
     * @return {@link DesiredCapabilities} type of browser capability
     * @throws UnsupportedOperationException if grid url is invalid
     */
    private WebDriver getRemoteDriver(DesiredCapabilities capability)
    {
        URL url;
        
        if(gridUrl == null || gridUrl.isEmpty())
        {
            throw new UnsupportedOperationException("Grid url is required");
        }
        try
        {
            url = new URL(gridUrl);
        }
        catch (MalformedURLException e)
        {
            throw new UnsupportedOperationException(String.format("A valid grid url is required instead of given url: %s",gridUrl),e);
        }
        capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(url, capability);
        remoteWebDriver.setFileDetector(new LocalFileDetector());
        return remoteWebDriver;
    }
    
    /**
     * Creates new instance of {@link ChromeDriver}.
     * @return {@link WebDriver} instance of {@link ChromeDriver}
     */
    private WebDriver getChromeDriver()
    {
        if(chromeServerPath == null || chromeServerPath.isEmpty())
        {
            throw new RuntimeException("Failed to create ChromeDriver, require a valid chromeServerPath");
        }
        Properties props = System.getProperties();
        if(!props.containsKey(CHROME_SERVER_DRIVER_PATH))
        {
            props.put(CHROME_SERVER_DRIVER_PATH, chromeServerPath);
        }
        ChromeOptions options = new ChromeOptions();
        //Chrome Option to add full Screen Mode.
        options.addArguments("--kiosk");
        return new ChromeDriver(options);
    }
    
    /**
     * Creates new instance of {@link SafariDriver}.
     * @return {@link WebDriver} instance of {@link SafariDriver}
     */
    private WebDriver getSafariDriver()
    {
        if(safariServerPath == null || safariServerPath.isEmpty())
        {
            throw new RuntimeException("Failed to create SafariDriver, require a valid safariPath");
        }
        Properties props = System.getProperties();
        if(!props.containsKey(SAFARI_SERVER_DRIVER_PATH))
        {
            props.put(SAFARI_SERVER_DRIVER_PATH, safariServerPath);
        }
        return new SafariDriver();
    }
    
    /**
     * Creates new instance of {@link InternetExplorerDriver}.
     * @return {@link WebDriver} instance of {@link InternetExplorerDriver}
     */
    private WebDriver getInternetExplorerDriver()
    {
        if(chromeServerPath == null || chromeServerPath.isEmpty())
        {
            throw new RuntimeException("Failed to create InternetExplorerDriver, require a valid ieServerPath");
        }
        Properties props = System.getProperties();
        if(!props.containsKey(IE_SERVER_DRIVER_PATH))
        {
            props.put(IE_SERVER_DRIVER_PATH, ieServerPath);
        }
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true); 
        return new InternetExplorerDriver(capabilities);
    }
    
    public String getGridUrl()
    {
        return gridUrl;
    }

    public void setGridUrl(String gridUrl)
    {
        this.gridUrl = gridUrl;
    }

    public String getChromeServerPath()
    {
        return chromeServerPath;
    }

    public void setChromeServerPath(String chromeServerPath)
    {
        this.chromeServerPath = chromeServerPath;
    }

    public String getIeServerPath()
    {
        return ieServerPath;
    }

    public void setIeServerPath(String ieServerPath)
    {
        this.ieServerPath = ieServerPath;
    }

    public void setDownloadDirectory(String downloadDirectory)
    {
        this.downloadDirectory = downloadDirectory;
    }

    public void setMimeTypes(String mimeTypes)
    {
        this.mimeTypes = mimeTypes;
    }
    /**
     * Get default remote fire fox driver.
     */
    public WebDriver getObject() throws Exception
    {
        return getObject(Browser.RemoteFireFox);
    }
    public Class<?> getObjectType()
    {
        return null;
    }
    public boolean isSingleton()
    {
        return false;
    }
}