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

package org.alfresco.repo.jive;

/**
 * This class provides a global flag for determining whether the current request was initiated from Jive or not.
 * This is used by the Jive Toolkit model to prevent Alfresco from calling Jive back when Jive itself initiated
 * an update to a document.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveRequest.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public final class JiveRequest
{
    private static final ThreadLocal<Boolean> jiveRequest = 
        new ThreadLocal<Boolean>()
        {
            @Override
            protected Boolean initialValue()
            {
                return(Boolean.FALSE);
            }
        };
    
    public static void setJiveRequest(final boolean isJiveRequest)
    {
        jiveRequest.set(isJiveRequest);
    }
    
    public static void reset()
    {
        jiveRequest.remove();
    }
    
    public static boolean isJiveRequest()
    {
        return(jiveRequest.get());
    }

}
