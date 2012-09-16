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

package org.alfresco.repo.web.scripts.servlet.jive;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.encryption.CannotDecryptException;
import org.alfresco.util.encryption.Encrypter;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory;


/**
 * Jive Toolkit Authentication for Web Scripts (CMIS, specifically).
 * 
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveToolkitAuthenticatorFactory.java 41626 2012-09-14 23:59:00Z wabson $
 */
public class JiveToolkitAuthenticatorFactory
    extends BasicHttpAuthenticatorFactory
{
    // Logger
    private final static Log log = LogFactory.getLog(JiveToolkitAuthenticatorFactory.class);
    
    private final static String HTTP_HEADER_NAME_USER_ID = "X-AlfrescoJive-UserId";
    

    // Component dependencies
    private AuthenticationService   authenticationService;
    private AuthenticationComponent authenticationComponent;
    private Encrypter               encrypter;

    
    /**
     * @param authenticationService
     */
    @Override
    public void setAuthenticationService(final AuthenticationService authenticationService)
    {
        super.setAuthenticationService(authenticationService);
        this.authenticationService = authenticationService;
    }
    
    public void setAuthenticationComponent(final AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    public void setEncrypter(final Encrypter encrypter)
    {
        this.encrypter = encrypter;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.ServletAuthenticatorFactory#create(org.alfresco.web.scripts.servlet.WebScriptServletRequest, org.alfresco.web.scripts.servlet.WebScriptServletResponse)
     */
    public Authenticator create(final WebScriptServletRequest req, final WebScriptServletResponse res)
    {
        return new JiveToolkitAuthenticator(req, res);
    }
    
    
    /**
     * Jive Toolkit Authentication - uses HTTP Basic Auth for authentication, but then masquerades as
     * another user passed (encrypted) in the X-AlfrescoJive-UserId HTTP header.
     * 
     * @author Peter Monks (pmonks@alfresco.com)
     */
    public class JiveToolkitAuthenticator
        extends BasicHttpAuthenticatorFactory.BasicHttpAuthenticator
    {
        // dependencies
        private WebScriptServletRequest  servletReq;
        private WebScriptServletResponse servletRes;
        
        
        /**
         * Construct
         * 
         * @param req
         * @param res
         */
        public JiveToolkitAuthenticator(final WebScriptServletRequest req, final WebScriptServletResponse res)
        {
            super(req, res);
            
            this.servletReq = req;
            this.servletRes = res;
        }
        
    
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#authenticate(org.alfresco.web.scripts.Description.RequiredAuthentication, boolean)
         */
        @Override
        public boolean authenticate(final RequiredAuthentication required, final boolean isGuest)
        {
            boolean             result = false;
            HttpServletRequest  req    = servletReq.getHttpServletRequest();
            HttpServletResponse res    = servletRes.getHttpServletResponse();
            
            if (log.isDebugEnabled())
                log.debug("Received request: " + requestToString(req));
            
            if (!isGuest)   // We don't allow guest access via this authenticator under any circumstances
            {
                // Retrieve and decrypt our custom auth token
                String encryptedUserId = req.getHeader(HTTP_HEADER_NAME_USER_ID);
                
                if (encryptedUserId != null)
                {
                    try
                    {
                        // Decrypt the user id...
                        String userId = encrypter.decrypt(encryptedUserId);
                        
                        // ..then perform HTTP Basic Authentication
                        result = super.authenticate(required, isGuest);
                        
                        if (result)
                        {
                            if (authenticationService.authenticationExists(userId))
                            {
                                // And then force the authenticated user to be the one Jive sent through, rather than the one in the HTTP Basic Auth credentials
                                authenticationComponent.setCurrentUser(userId);
                            }
                            else // The user id sent from Jive isn't known to Alfresco
                            {
                                if (log.isDebugEnabled())
                                    log.debug("Rejecting attempted access by Jive user " + userId + " - they are unknown to Alfresco.");

                                res.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
                                result = false;
                            }
                        }
                        else
                        {
                            if (log.isDebugEnabled())
                                log.debug("HTTP Basic Auth failed - returning 401 (authenticate) response.");
                        }
                    }
                    catch (final CannotDecryptException cde)  // Unable to decrypt the user id sent from Jive
                    {
                        log.error("Failed to decrypt Jive user id.  The most likely cause is that the encryption password shared between Alfresco and Jive is out of sync.");

                        if (log.isDebugEnabled())
                            log.debug(cde.getMessage(), cde);

                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
                        result = false;
                    }
                        
                }
                else  // The Jive user id was not provided
                {
                    log.error("Jive user id was not provided.");
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
                }
            }
            else  // Guest request - force reauthentication with real credentials
            {
                if (log.isDebugEnabled())
                    log.debug("Rejecting attempted guest user access - guest user cannot access this resource.");

                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
                res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
            }
            
            return(result);
        }
    }
    
    
    /**
     * Debugging method for obtaining the state of a request as a String.
     * 
     * @param request The request to retrieve the state from <i>(may be null)</i>.
     * @return The request state as a human-readable string value <i>(will not be null)</i>.
     */
    private String requestToString(final HttpServletRequest request)
    {
        StringBuffer result = new StringBuffer(128);
        
        if (request != null)
        {
            result.append("\n\tMethod: ");
            result.append(request.getMethod());
            result.append("\n\tURL: ");
            result.append(String.valueOf(request.getRequestURI()));
            result.append("\n\tHeaders: ");
            
            Enumeration<String> headerNames = request.getHeaderNames();
            
            while (headerNames.hasMoreElements())
            {
                String headerName  = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                result.append("\n\t\t");
                result.append(headerName);
                result.append(" : ");
                result.append(headerValue);
            }
        }
        else
        {
            result.append("(null)");
        }
        
        return(result.toString());
    }
    

}