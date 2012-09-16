/*
 * Copyright 2011-2012 Alfresco Software Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is part of an unsupported extension to Alfresco.
 */

package org.alfresco.repo.jive.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.repo.jive.InvalidCommunityException;
import org.alfresco.repo.jive.JiveCommunity;
import org.alfresco.repo.jive.JiveService;


/**
 * This class provides a data Web Script that Share calls when it needs to
 * retrieve and display the tree of Jive Communities the user has access to.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: GetCommunitiesWebScript.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public final class GetCommunitiesWebScript
    extends DeclarativeWebScript
{
    private final static Log log = LogFactory.getLog(GetCommunitiesWebScript.class);
    
    private JiveService jiveService;
    
    
    public void setJiveService(final JiveService jiveService)
    {
        // PRECONDITIONS
        assert jiveService != null : "jiveService must not be null.";
        
        // Body
        this.jiveService = jiveService;
    }
    

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        if (log.isTraceEnabled()) log.trace("GetCommunitiesWebScript.executeImpl called");
                                            
        final Map<String, Object> result           = new HashMap<String, Object>();
        final String              communityIdAsStr = parseCommunityId(req.getExtensionPath());
        List<JiveCommunity>       subCommunities   = null;
        
        cache.setNeverCache(true);
        
        if (communityIdAsStr != null && communityIdAsStr.trim().length() > 0)
        {
            // We got a community Id in the request, so retrieve its subcommunities
            long communityId = -1;
            
            try
            {
                communityId = Long.parseLong(communityIdAsStr);
            }
            catch (final NumberFormatException nfe)
            {
                throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Could not parse community Id from value '" + communityIdAsStr + "'.", nfe);
            }
            
            try
            {
                if (log.isDebugEnabled()) log.debug("Retrieving sub-communities of Jive community " + communityId);
                result.put("root", false);
                subCommunities = jiveService.getSubCommunities(communityId);
            }
            catch (final InvalidCommunityException ice)
            {
                throw new WebScriptException(HttpStatus.SC_NOT_FOUND, ice.getMessage(), ice);
            }
            catch (final ServiceUnavailableException sue)
            {
            	throw new WebScriptException(HttpStatus.SC_SERVICE_UNAVAILABLE, sue.getMessage(), sue);
            }
        }
        else
        {
            try
            {
	        	// No community id provided, so retrieve the root-level communities
	            if (log.isDebugEnabled()) log.debug("Retrieving root-level communities from Jive.");
	            result.put("root", true);
	            subCommunities = jiveService.getCommunities();
            }
            catch (final ServiceUnavailableException sue)
            {
            	throw new WebScriptException(HttpStatus.SC_SERVICE_UNAVAILABLE, sue.getMessage(), sue);
            }
        }
        
        result.put("subCommunities", subCommunities);
        
        return(result);
    }
    
    
    private final String parseCommunityId(final String extensionPath)
    {
        String result          = extensionPath;
        int    indexOfFullStop = extensionPath.indexOf('.');
        
        if (indexOfFullStop >= 0)
        {
            // We have a filename extension (e.g. ".json") on the end of the URL, so strip it off
            result = extensionPath.substring(0, indexOfFullStop);
        }
        
        return(result);
    }
    
    
}
