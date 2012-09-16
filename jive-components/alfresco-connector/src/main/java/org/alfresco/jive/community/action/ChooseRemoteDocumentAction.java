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

package org.alfresco.jive.community.action;


import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.cmis.manager.RemoteContainer;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.action.JiveContainerAware;
import com.jivesoftware.community.objecttype.ContentObjectTypeInfoProvider;
import com.jivesoftware.community.web.struts.SetReferer;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;


/**
 * Pick a container to create a new document or thread in. Will return success
 * if the user has permission to create a document/thread in at least one of the
 * spaces / communities in the system or unauthorized otherwise.
 */
@SetReferer(false)
public class ChooseRemoteDocumentAction
    extends com.jivesoftware.community.action.ChooseContainerAction
    implements Preparable, JiveContainerAware
{

    private static final Logger     log     = LogManager.getLogger(ChooseRemoteDocumentAction.class);

    // document specifics
    protected boolean               managed = false;

    protected String                remoteDocument;

    private AlfrescoNavigationManager alfrescoNavigationManager;

    private ContentObjectTypeInfoProvider managedDocumentContentObjectTypeInfoProvider;
    
    public String execute()
    {

        String response = super.execute();

        if (response.equals(SUCCESS) && isManaged())
        {

            //ContentObjectTypeInfoProvider provider = getJiveContext().getSpringBean("managedDocumentContentObjectTypeInfoProvider");

            successRedirectUrl = managedDocumentContentObjectTypeInfoProvider.getCreateNewFormRelativeURL(getContainer(), upload
                                                                                      && binaryBodyManager.isBinaryBodyEnabled(), tempObjectID, tags, subject);

            if (StringUtils.isEmpty(successRedirectUrl))
            {
                log.error("Content type does not have a success redirect URL defined: " + contentType);
                addActionError("The content type specified is not valid.");
                return ERROR;
            }
        }

        return response;

    }


    @Override
    public String input()
    {
        title = getText("main.documents.manage_file");
        
        try {
			alfrescoNavigationManager.connect();
			
		} catch (CmisConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Content type does not have a success redirect URL defined: " + contentType);
            addActionError(getText("cmis.error.connection"));
			return ERROR;
		}
		
        return INPUT;
    }


    public boolean getCanCreateInContainer(RemoteContainer container)
    {
        return true;
    }


    public boolean isManaged()
    {
        return managed;
    }


    public void setManaged(boolean managed)
    {
        this.managed = managed;
    }


    public String getRemoteDocument()
    {
        return remoteDocument;
    }


    public void setRemoteDocument(String remoteDocument)
    {
        this.remoteDocument = remoteDocument;
    }
    
    public void setAlfrescoNavigationManager(
			AlfrescoNavigationManager alfrescoNavigationManager) {
		this.alfrescoNavigationManager = alfrescoNavigationManager;
	}


	public void setManagedDocumentContentObjectTypeInfoProvider(
			ContentObjectTypeInfoProvider managedDocumentContentObjectTypeInfoProvider) {
		this.managedDocumentContentObjectTypeInfoProvider = managedDocumentContentObjectTypeInfoProvider;
	}
}
