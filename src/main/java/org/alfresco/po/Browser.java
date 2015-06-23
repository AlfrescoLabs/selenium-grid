/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.po;


/**
 * Enum that represents the possible web driver browser implementation.
 * 
 * @author Michael Suzuki
 * @since 1.4
 */
public enum Browser
{
    FireFox,
    FireFoxDownloadToDir,
    Chrome,
    IE,
    HtmlUnit,
    Safari,
    RemoteFireFox,
    RemoteChrome,
    RemoteIE;

    
    /**
     * Get required browser from version.
     * @param value String alfresco version
     * @return {@link Browser} version
     */
    public static Browser fromString(final String value)
    {
        if(value == null || value.trim().isEmpty()) 
        {
            throw new UnsupportedOperationException("Value required");
        }
        for(Browser version: Browser.values())
        {   
            if(value.equalsIgnoreCase(version.name()))
            {
                return version;
            }
        }
        throw new IllegalArgumentException("value did not match existing AlfrescoVersion");
    }
}
