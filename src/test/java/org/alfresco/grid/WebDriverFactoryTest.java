/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.po.Browser;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
@ContextConfiguration(locations = {"classpath:/webdriver-context.xml"})
/**
 * Test to ensure the WebDriverFactory returns the browsers.
 * @author Michael Suzuki
 *
 */
public class WebDriverFactoryTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    ApplicationContext ctx;
    @Autowired
    WebDriverFactory factory;
    WebDriver driver,driver2;
    @AfterMethod(alwaysRun = true)
    void close()
    {
        driver.quit();
    }
    @Test
    public void getFireFox()
    {
        driver = factory.getObject(Browser.FireFox);
        Assert.assertNotNull(driver);
    }
    @Test
    public void getHtmlUnit()
    {
        driver = factory.getObject(Browser.HtmlUnit);
        Assert.assertNotNull(driver);
    }
    @Test
    public void getGrid()
    {
        Assert.assertNotNull(factory.getGridUrl());
    }
    @Test
    public void getRemoteFireFox()
    {
        driver = factory.getObject(Browser.RemoteFireFox);
        Assert.assertNotNull(driver);
    }
}
