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

package org.alfresco.repo.jive.action;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.jive.CallFailedException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.jive.AlreadySocializedException;
import org.alfresco.repo.jive.FileNotFoundException;
import org.alfresco.repo.jive.InvalidCommunityException;
import org.alfresco.repo.jive.JiveService;
import org.alfresco.repo.jive.NotAFileException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jared Ottley <jared.ottley@alfresco.com>
 * 
 */
public class SocializeAction extends ActionExecuterAbstractBase {

	private static Log logger = LogFactory.getLog(SocializeAction.class);

	private static final String PARAM_COMMUNITY_ID = "communityId";
	private static final String PARAM_COMMUNITY_ID_DISPLAY_LABEL = "community";

	private JiveService jiveService;

	public void setJiveService(JiveService jiveService) {
		this.jiveService = jiveService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl
	 * (org.alfresco.service.cmr.action.Action,
	 * org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

		try {
			Long communityId = Long.valueOf(action.getParameterValue(
					PARAM_COMMUNITY_ID).toString());

			socializeDocument(actionedUponNodeRef, communityId);
		} catch (NumberFormatException nfe) {
			throw new ActionServiceException("Community ID not valid: "
					+ action.getParameterValue(PARAM_COMMUNITY_ID));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#
	 * addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

		paramList.add(new ParameterDefinitionImpl(PARAM_COMMUNITY_ID,
				DataTypeDefinition.TEXT, true,
				this.getParamDisplayLabel(PARAM_COMMUNITY_ID_DISPLAY_LABEL),
				false, CommunitiesConstraint.NAME));

	}

	// Wrapper for JiveService socializeDocuments -- ActionServiceException
	// handling
	private void socializeDocument(NodeRef nodeRef, Long communityId) {
		List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
		nodeRefs.add(nodeRef);

		logger
				.debug("Socialize: " + nodeRef + ", Community ID: "
						+ communityId);

		try {
			if (communityId != null) {
				jiveService.socializeDocuments(nodeRefs, communityId);
			} else {
				throw new ActionServiceException("Community ID not provided.");
			}
		} catch (FileNotFoundException fnfe) {
			throw new ActionServiceException("File Not Found: "
					+ fnfe.getMessage());
		} catch (NotAFileException nafe) {
			throw new ActionServiceException("Not a File: " + nafe.getMessage());
		} catch (InvalidCommunityException ice) {
			throw new ActionServiceException("Invalid Community: "
					+ ice.getMessage());
		} catch (AlreadySocializedException ase) {
			throw new ActionServiceException("Document already Socialized: "
					+ ase.getMessage());
		} catch (CallFailedException cfe) {
			throw new ActionServiceException("Jive Unreachable: "
					+ cfe.getMessage());
		}
	}
}
