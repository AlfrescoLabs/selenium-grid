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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to provide the properties for the grid hub/node
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public final class GridProperties
{
    /** Constants */
    private static Log logger = LogFactory.getLog(GridProperties.class);
    private static final String CMD_PARAMETER_PREFIX = "-";
    private static final String GRID = "grid";
    private static final String GRID_ROLE_HUB = "hub";
    private static final String GRID_ROLE_NODE = "node";
    private static final String SEPARATOR = ".";
    private static final String PROPERTY_FOLDER = GRID + "/";
    private static final String PROPERTY_PREFIX = GRID + SEPARATOR;
    private static final String PROPERTY_SUFFIX = SEPARATOR + "properties";
    private static final String PROPERTY_LOCAL_SUFFIX = SEPARATOR + "local" + PROPERTY_SUFFIX;
    
    private GridProperties(){};
    /**
     * Reads the properties file(s) for the hub configuration and returns it as an {@link String} array
     *
     * @return An array of {@link String} property keys and values
     * @throws RuntimeException Throws an {@link IOException} if the properties file cannot be found or
     * An {@link Exception} if property key has an invalid format
     */
    public static String[] getHubProperties()
    {
        try
        {
            return getProperties(GRID_ROLE_HUB);
        } 
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load hub properties", e);
        }
    }

    /**
     * Reads the properties file(s) for the node configuration and returns it as an {@link String} array
     *
     * @param port int value of port to use
     * @return An array of {@link String} property keys and values
     * @throws RuntimeException Throws an {@link IOException} if the properties file cannot be found or
     * An {@link Exception} if property key has an invalid format
     */
    public static String[] getNodeProperties(int ... port) 
    {
        try
        {
            return getProperties(GRID_ROLE_NODE, port);
        } 
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load node properties", e);
        }
    }

    /**
     * Helper method to reduce the code duplication.
     * Reads the properties file(s) for the hub/node configuration and returns it as an {@link String} array
     *
     * @param role {@link String} The role for the grid. Can be hub or node.
     * @return An array of {@link String} property keys and values
     * @throws Exception Throws an {@link IOException} if the properties file cannot be found or
     * An {@link Exception} if property key has an invalid format
     */
    private static String[] getProperties(String role, int ...gridPort)
    {
        if(role == null || role.isEmpty())
        {
            throw new IllegalArgumentException("Role for the grid is required");
        }
        List<String> propertyKeysAndValues = new ArrayList<String>();
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Load the properties
        String propertyPath = PROPERTY_FOLDER + role + PROPERTY_SUFFIX;
        try
        {
            properties.load(loader.getResourceAsStream(propertyPath));
        } 
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Unable to load main property file %s",propertyPath), e);
        }
        // Load the local properties to override the existing ones
        String localPropertyPath = PROPERTY_FOLDER + role + PROPERTY_LOCAL_SUFFIX;
        InputStream localProperty = loader.getResourceAsStream(localPropertyPath);
        if(localProperty != null)
        {
            try
            {
                properties.load(localProperty);
            } 
            catch (IOException e)
            {
                logger.error(String.format("Unable to load local property file: %s ", localPropertyPath),e);
            }
        }
        if(gridPort.length > 0)
        {
            properties.setProperty("hub.url.port", String.valueOf(gridPort[0]));
        }

        Set<Object> keys = properties.keySet();
        for (Object object : keys)
        {
            String key = (String) object;
            if (key.startsWith(PROPERTY_PREFIX))
            {
                String parameter = key.split(PROPERTY_PREFIX)[1];
                if (StringUtils.isBlank(parameter))
                {
                    throw new RuntimeException("The property '" + key + "' does not have a valid format.");
                }
                String propertyKey = CMD_PARAMETER_PREFIX + parameter;
                String propertyValue = StrSubstitutor.replace(properties.getProperty(key), properties);
                propertyKeysAndValues.add(propertyKey);
                propertyKeysAndValues.add(propertyValue);
            }
        }
        return propertyKeysAndValues.toArray(new String[propertyKeysAndValues.size()]);
    }
}