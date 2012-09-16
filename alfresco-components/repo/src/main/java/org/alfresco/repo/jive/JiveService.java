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

import java.util.List;

import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * This interface represents the operations that can be executed against Jive.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveService.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public interface JiveService
{
    /**
     * Socializes the given document to Jive (thereby creating it in Jive).
     * 
     * @param nodeRef The <code>NodeRef</code> of the document to socialize <i>(must not be null)</i>.
     *                Note: the node must be a file i.e. of type cm:content or one of its subclasses.
     * @param communityId The id of the Jive Community in which to socialize the document.
     * @throws FileNotFoundException      If any of the nodeRefs cannot be found.
     * @throws NotAFileException          If any of the nodeRefs is not a file.
     * @throws AlreadySocializedException If any of the nodeRefs has already been socialized.
     * @throws InvalidCommunityException  When the communityId is invalid.
     */
    public void socializeDocuments(final List<NodeRef> nodeRefs, final long communityId)
        throws FileNotFoundException,
               NotAFileException,
               InvalidCommunityException,
               AlreadySocializedException,
               ServiceUnavailableException;
        
    
    /**
     * Updates the given socialized document in Jive (note: must already be socialized with Jive).
     * 
     * @param nodeRef The <code>NodeRef</code> of the document to update in Jive <i>(must not be null)</i>.
     * @param contentUpdated A flag indicating whether the content (true) or the properties (false) were updated.
     * @throws FileNotFoundException When the NodeRef does not exist, or is not a file.
     * @throws NotSocializedException When the NodeRef has not previously been socialized to Jive.
     */
    public void updateDocument(final NodeRef nodeRef, final boolean contentUpdated)
        throws FileNotFoundException,
               NotSocializedException,
               ServiceUnavailableException;
    
    
    /**
     * @return A list of the root-level <code>JiveCommunity</code>'s the current user has access to in Jive <i>(will not be null, but may be empty)</i>.
     */
    public List<JiveCommunity> getCommunities()
    	throws ServiceUnavailableException;
    
    
    /**
     * @param communityId The id of the <code>JiveCommunity</code>'s whose children are to be listed.
     * @return A list of the <code>JiveCommunity</code>'s below the Community with the given id that the current user has access to in Jive <i>(will not be null, but may be empty)</i>.
     * @throws InvalidCommunityException When the communityId is invalid.
     */
    public List<JiveCommunity> getSubCommunities(final long communityId)
        throws InvalidCommunityException,
        	   ServiceUnavailableException;
}
