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

package org.alfresco.jive.community.action.admin;

import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.license.annotations.RequireModule;
import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.alfresco.jive.community.impl.ConnectorConstants;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import com.jivesoftware.community.license.annotations.RequireModule;

@RequireModule(product = "wiki")
public class DocumentManagementAction extends
		com.jivesoftware.community.action.admin.DocumentManagementAction {

	private static final Logger log = LogManager
			.getLogger(DocumentManagementAction.class);

	private AlfrescoNavigationManager alfrescoNavigationManager;
	private static final String DELETED = "deleted";

	public String doDeleteDocument() {
		DocumentManager docManager = getJiveContext().getDocumentManager();

		try {
			Document doc = docManager.getDocument(getDocumentID());
			
			// remove socialized aspect from Alfresco
			if (isManagedDocument(doc)) {
				String objectId = doc.getProperties().get(
						ConnectorConstants.REMOTE_OBJECT_PROPERTY);
				RemoteDocument remoteDoc = new RemoteDocument(objectId, null);

				alfrescoNavigationManager.deleteDocument(remoteDoc);
			}
			
			docManager.deleteDocument(doc);

		} catch (DocumentObjectNotFoundException e) {
			log.error(e);
			addActionError(e.getMessage());
		} catch(CmisConstraintException e) {
			log.error(e);
			addActionError(e.getMessage());
		}

		return DELETED;
	}

	public boolean isManagedDocument(Document document) {
		return document.getDocumentType().getID() == ConnectorConstants.MANAGED_TYPE;
	}

	public void setAlfrescoNavigationManager(
			AlfrescoNavigationManager alfrescoNavigationManager) {
		this.alfrescoNavigationManager = alfrescoNavigationManager;
	}
}
