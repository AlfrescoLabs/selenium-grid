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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
/**
 * Selenium Grid bean, the bean holds both the grid and a node and acts as the local grid.
 * All integration tests and Selenium base operation are run via Selenium Grid. 
 * In the local environment it will start the service by starting the local Grid and a node. 
 * The node is registered to the local grid and manages the local browser.  
 * 
 * This setup ensures that the grid is always used to run selenium operation, the build environment
 * uses a dedicated grid farm to manage all the browsers and is overriden by setting param:
 * webdriver.grid.local=false
 * 
 * @author Michael Suzuki
 * @version 2.5
 */
public class Grid
{
    public static int RESPONSE_STATUS_404 = 404;
    public static int RESPONSE_STATUS_200 = 200;
    private Log logger = LogFactory.getLog(Grid.class);
    private GridHub grid;
    private GridNode node;
    private final boolean isGridLocal;
    
    /**
     * Constructor.
     * @param localGrid boolean setup local grid

     */
    public Grid(final boolean localGrid)
    {
        this.isGridLocal = localGrid;
        if(localGrid)
        {
            grid = new GridHub();
            node = new GridNode();
            if(!isAlive())
            {
                grid.run();
                node.start();
            }
        }
    }
    
    public boolean isGridLocal()
    {
        return isGridLocal;
    }
    
    /**
     * Checks if grid is running.
     * @return true if status is 200
     */
    public boolean isAlive()
    {
        try
        {
            return parseGridStatus() == RESPONSE_STATUS_200;
        }
        catch(Exception e)
        { 
            logger.error("Local grid check failed", e);
        }
        return false;
    }
    
    /**
     * Checks if grid is not running.
     * @return true page can not be found
     */
    public boolean isClosed()
    {
        try
        {

            return parseGridStatus() == RESPONSE_STATUS_404;
        }
        catch(Exception e)
        { 
            logger.error("Local grid check failed", e);
        }
        return false;
    }
    
    public void close()
    {
        if(node != null)
        {
            node.stop();
        }
        if(grid != null && grid.isAlive())
        {
            grid.stop();
        }
    }
    
    /**
     * Checks the http header response when accessing the page.
     * @return int http status code
     */
    private int parseGridStatus()
    {
        if(grid != null && StringUtils.isNotEmpty(grid.getUrl())) 
        {
            String url = grid.getUrl();
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            try
            {
                HttpResponse response = client.execute(request);
                return response.getStatusLine().getStatusCode();
            } 
            catch (ClientProtocolException e)
            {
                logger.debug("Client protocol exception", e);
            } 
            catch (IOException ioe)
            {
                logger.debug("IOE exception",ioe);
            }
            finally
            {
                request.releaseConnection();
                client = null;
            }
        }
        return 0;
    }
}
