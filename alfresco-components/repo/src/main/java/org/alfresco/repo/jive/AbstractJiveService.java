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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import org.alfresco.model.jive.JiveModel;
import org.alfresco.repo.jive.FileNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This abstract class provides some helper implementations for <code>JiveService</code>
 * concrete implementations.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: AbstractJiveService.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public abstract class AbstractJiveService
    implements JiveService
{
    private final Log log = LogFactory.getLog(AbstractJiveService.class);
    
    
    protected NodeService       nodeService;
    protected FileFolderService fileFolderService;


    public void setServiceRegistry(final ServiceRegistry serviceRegistry)
    {
        // PRECONDITIONS
        assert serviceRegistry != null : "serviceRegistry must not be null.";
        
        //Body
        this.nodeService       = serviceRegistry.getNodeService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
    }

    
    /**
     * Validates that all of the <code>NodeRef</code>s in the list are eligible for socialization (they exist, are a file, and have not already been socialized).
     * 
     * @param nodeRefs The nodeRefs to validate <i>(must not be null, but may be empty)</i>.
     * @throws IllegalArgumentException   If <code>nodeRefs</code> is null.
     * @throws FileNotFoundException      If any of the nodeRefs cannot be found.
     * @throws NotAFileException          If any of the nodeRefs is not a file.
     * @throws AlreadySocializedException If any of the nodeRefs has already been socialized.
     */
    protected void validateNodeRefsAreSocializable(final List<NodeRef> nodeRefs)
        throws IllegalArgumentException,
               FileNotFoundException,
               NotAFileException,
               AlreadySocializedException
    {
        if (nodeRefs == null)
        {
            throw new IllegalArgumentException("nodeRefs must not be null.");
        }
        
        for (final NodeRef nodeRef : nodeRefs)
        {
            validateNodeRefExistsAndIsFile(nodeRef);
            
            if (nodeService.hasAspect(nodeRef, JiveModel.ASPECT_JIVE_SOCIALIZED))
            {
                throw new AlreadySocializedException(nodeRef);
            }
        }
    }
    
    
    /**
     * Validates that the <code>NodeRef</code> exists, is a file and is already socialized.
     * 
     * @param nodeRef The nodeRef to validate <i>(must not be null)</i>.
     * @throws IllegalArgumentException If <code>nodeRef</code> is null.
     * @throws FileNotFoundException    If any of the nodeRefs cannot be found.
     * @throws NotAFileException        If any of the nodeRefs is not a file.
     * @throws NotSocializedException   If any of the nodeRefs has already been socialized.
     */
    protected void validateNodeRefIsSocialized(final NodeRef nodeRef)
        throws IllegalArgumentException,
               FileNotFoundException,
               NotAFileException,
               NotSocializedException
    {
        validateNodeRefExistsAndIsFile(nodeRef);
        
        if (!nodeService.hasAspect(nodeRef, JiveModel.ASPECT_JIVE_SOCIALIZED))
        {
            throw new NotSocializedException(nodeRef);
        }
    }
    
    
    /**
     * Validates that the <code>NodeRef</code> exists, and is a file.
     * 
     * @param nodeRef The nodeRef to validate <i>(must not be null)</i>.
     * @throws IllegalArgumentException If <code>nodeRef</code> is null.
     * @throws FileNotFoundException    If any of the nodeRefs cannot be found.
     * @throws NotAFileException        If any of the nodeRefs is not a file.
     */
    protected void validateNodeRefExistsAndIsFile(final NodeRef nodeRef)
        throws IllegalArgumentException,
               FileNotFoundException,
               NotAFileException
    {
        if (nodeRef == null)
        {
            throw new IllegalArgumentException("nodeRef must not be null.");
        }
        
        if (!nodeService.exists(nodeRef))
        {
            throw new FileNotFoundException(nodeRef);
        }
        
        FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
        
        if (fileInfo.isFolder() || fileInfo.isLink())
        {
            throw new NotAFileException(nodeRef);
        }
    }
    
    
    /**
     * Marks the given <code>nodeRef</code>s as socialized.
     * 
     * @param nodeRefs The nodeRefs to validate <i>(must not be null, but may be empty)</i>.
     * @param communityId The communityId the nodeRefs are being socialized to.
     */
    protected void markNodeRefsSocialized(final List<NodeRef> nodeRefs, final long communityId)
    {
        for (final NodeRef nodeRef : nodeRefs)
        {
            nodeService.addAspect(nodeRef, JiveModel.ASPECT_JIVE_SOCIALIZED, null);
            
            if (log.isDebugEnabled())
                log.debug("NodeRef " + String.valueOf(nodeRef) + " socialized to Jive Community " + String.valueOf(communityId));
        }
    }

}
