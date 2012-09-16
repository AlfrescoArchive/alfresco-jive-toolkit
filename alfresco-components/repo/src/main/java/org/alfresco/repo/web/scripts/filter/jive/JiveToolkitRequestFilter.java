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

package org.alfresco.repo.web.scripts.filter.jive;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.repo.jive.JiveRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is a Servlet Filter that detects whether a request came from Jive or not.
 * Important note: this filter must be bound to <b>exactly</b> the same path as the custom
 * 
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveToolkitRequestFilter.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class JiveToolkitRequestFilter
    implements Filter
{
    private final static Log log = LogFactory.getLog(JiveToolkitRequestFilter.class);
    
    
    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy()
    {
        // no-op
    }


    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException,
               ServletException
    {
        try
        {
            if (log.isTraceEnabled())
                log.trace("Request received from Jive, setting jiveRequest flag to true.");
            
            JiveRequest.setJiveRequest(true);
            chain.doFilter(request, response);
        }
        finally
        {
            if (log.isTraceEnabled())
                log.trace("Resetting jiveRequest flag.");
            
            JiveRequest.reset();
        }
    }


    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(final FilterConfig config)
        throws ServletException
    {
        // no-op
    }

}
