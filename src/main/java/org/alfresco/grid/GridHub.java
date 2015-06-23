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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.web.Hub;

/**
 * Creates the configuration for a {@link Hub} an starts it.
 *
 * @author Tuna Aksoy, Michael Suzuki
 * @since 2.2
 */
public class GridHub implements Runnable
{
    /** Logger */
    private final Log logger = LogFactory.getLog(GridHub.class);
    private static int THIRD_PROPERTY = 3;

    private Hub hub;

    public GridHub(final int ... port)
    {
        String[] hubProperties = GridProperties.getHubProperties();
        if(port.length > 0)
        {
            //Replace default port from array with port passed in args. 
            hubProperties[THIRD_PROPERTY] = String.valueOf(port[0]);
        }
        
        GridHubConfiguration gridHubConfiguration = GridHubConfiguration.build(hubProperties);
        hub = new Hub(gridHubConfiguration);
    }

    public String getUrl()
    {
        return hub.getUrl().toExternalForm();
    }

    /**
     * Start selenium grid server.
     */
    public void run()
    {
        try
        {
            logger.info("Starting the grid hub on " + hub.getUrl());
            hub.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start the grid", e);
        }
    }

    public void stop()
    {
        try
        {
            hub.stop();
        } 
        catch (Exception e)
        {
            logger.error("Problem closing grid hub", e);
        }
        Thread.interrupted();
    }

    /**
     * Verify the status of hub.
     * @return true if running
     */
    public boolean isAlive()
    {
        if (200 == checkPageResponse())
        {
            return true;
        }
        return false;
    }

    /**
     * HttpClient getter call to verify grid response to
     * http get request.
     * @return int 200 for success.
     */
    public int checkPageResponse()
    {
        int code = 0;
        HttpClient client = HttpClientBuilder.create().build();
        String url = hub.getUrl().toString();
        HttpGet request = new HttpGet(url);
        try
        {
            HttpResponse response = client.execute(request);
            code = response.getStatusLine().getStatusCode();
        }
        catch (ClientProtocolException e)
        {
            logger.debug("Client protocol exception");
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
        // add request header
        return code;
    }
}