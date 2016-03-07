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

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.shared.GridNodeServer;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Builds a {@link RegistrationRequest} for {@link SelfRegisteringRemote} server,
 * starts the remote server and the registration process for the drivers.
 *
 * @author Tuna Aksoy, Michael Suzuki
 * @since 2.2
 */
public class GridNode
{
    /** Logger */
    private final Logger logger = Logger.getLogger(GridNode.class.getName());

    private final SelfRegisteringRemote remote;
    private final GridNodeServer server;
    private final RegistrationRequest registrationRequest;

    /**
     * Constructor with optional array of ports to set.
     * first port value represent the grids port value.
     * second port value represents the nodes port value.
     * @param port local running grid
     */
    public GridNode(int ...port)
    {
        try
        {
            String[] nodeProperties = GridProperties.getNodeProperties(port);
            registrationRequest = RegistrationRequest.build(nodeProperties);
            if(port.length > 0)
            {
                String nodePort = String.valueOf(port[1]);
                if(StringUtils.isNotEmpty(nodePort))
                {
                    Map<String, Object> config = registrationRequest.getConfiguration();
                    config.put("port", nodePort);
                }
            }
            remote = new SelfRegisteringRemote(registrationRequest);
            server = new SeleniumServer(registrationRequest.getConfiguration());
        } 
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start selenium node",e);
        }
    }

    /**
     * Starts selenium node and registers against the grid.
     */
    public void start()
    {
        try
        {
            logger.info("Starting the grid node.");
            remote.setRemoteServer(server);
            remote.startRemoteServer();
            remote.startRegistrationProcess();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start the node", e);
        }
    }

    /**
     * Stops the selenium server
     */
    public void stop()
    {
        logger.info("Stopping the grid node.");
        server.stop();
    }
}