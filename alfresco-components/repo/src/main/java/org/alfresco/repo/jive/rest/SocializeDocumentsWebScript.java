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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.jive.DocumentSizeException;
import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.repo.jive.FileNotFoundException;
import org.alfresco.repo.jive.JiveService;
import org.alfresco.repo.jive.JiveServiceException;
import org.alfresco.repo.jive.NotAFileException;


/**
 * This class provides a data Web Script that Share calls when the "Socialize Document"
 * function is requested by the Share user.  This Web Script performs the actual work
 * of socializing the document <insert reference to Karl Marx here>.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: SocializeDocumentsWebScript.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class SocializeDocumentsWebScript
    extends DeclarativeWebScript
{
    private final static Log log = LogFactory.getLog(SocializeDocumentsWebScript.class);
    
    private JiveService jiveService;
    
    private final static String JSON_KEY_NODE_REFS = "nodeRefs";
    
    
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
        if (log.isTraceEnabled()) log.trace("SocializeDocumentsWebScript.executeImpl called");
        
        Map<String, Object> result      = null;
        List<NodeRef>       nodeRefs    = parseNodeRefs(req);
        long                communityId = parseCommunityId(req);
        
        cache.setNeverCache(true);
        
        try
        {
            if (log.isDebugEnabled()) log.debug("Socializing documents " + Arrays.toString(nodeRefs.toArray()) + " to Jive community " + communityId);
            jiveService.socializeDocuments(nodeRefs, communityId);
        }
        catch (final FileNotFoundException fnfe)
        {
            throw new WebScriptException(HttpStatus.SC_NOT_FOUND, fnfe.getMessage(), fnfe);
        }
        catch (final NotAFileException nafe)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, nafe.getMessage(), nafe);
        }
        catch (final JiveServiceException jse)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, jse.getMessage(), jse);
        }
        catch (final ServiceUnavailableException sue)
        {
        	throw new WebScriptException(HttpStatus.SC_SERVICE_UNAVAILABLE, sue.getMessage(), sue);
        }
        catch (final DocumentSizeException dse)
        {
        	throw new WebScriptException(HttpStatus.SC_CONFLICT, dse.getMessage(), dse);
        }
        
        return(result);
    }
    
    
    private final List<NodeRef> parseNodeRefs(final WebScriptRequest req)
    {
        final List<NodeRef> result  = new ArrayList<NodeRef>();
        Content             content = req.getContent();
        String              jsonStr = null;
        JSONObject          json    = null;
        
        try
        {
            if (content == null || content.getSize() == 0)
            {
                throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "No content sent with request.");
            }
            
            jsonStr = content.getContent();
            
            if (jsonStr == null || jsonStr.trim().length() == 0)
            {
                throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "No content sent with request.");
            }
            
            json = new JSONObject(jsonStr);

            if (!json.has(JSON_KEY_NODE_REFS))
            {
                throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Key " + JSON_KEY_NODE_REFS + " is missing from JSON: " + jsonStr);
            }

            JSONArray nodeRefs = json.getJSONArray(JSON_KEY_NODE_REFS);
            
            for (int i = 0; i < nodeRefs.length(); i++)
            {
                NodeRef nodeRef = new NodeRef(nodeRefs.getString(i));
                result.add(nodeRef);
            }
        }
        catch (final IOException ioe)
        {
            throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, ioe.getMessage(), ioe);
        }
        catch (final JSONException je)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Unable to parse JSON: " + jsonStr);
        }
        catch (final WebScriptException wse)
        {
            throw wse;  // Ensure WebScriptExceptions get rethrown verbatim
        }
        catch (final Exception e)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Unable to retrieve nodeRefs from JSON '" + jsonStr + "'.", e);
        }
        
        return(result);
    }


    private final long parseCommunityId(final WebScriptRequest req)
    {
        long   result          = -1;
        String extensionPath   = req.getExtensionPath(); 
        int    indexOfFullStop = extensionPath.indexOf('.');
        
        if (indexOfFullStop >= 0)
        {
            // We have a filename extension (e.g. ".json") on the end of the URL, so strip it off
            extensionPath = extensionPath.substring(0, indexOfFullStop);
        }
        
        String[] extensionPathComponents = extensionPath.split("/");
        
        if (extensionPathComponents == null)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Unable to parse communityId from path '" + extensionPath + "'.");
        }
        
        if (extensionPathComponents.length != 2)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Incorrect number of path parameters - got " + extensionPathComponents.length + ", was expecting 2.");
        }
        
        try
        {
            result = Long.valueOf(extensionPathComponents[0]);
        }
        catch (final NumberFormatException nfe)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Unable to parse communityId from path '" + extensionPath + "'.", nfe);
        }
        
        return(result);
    }
    
    
}
