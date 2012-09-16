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

package org.alfresco.repo.jive.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.jive.CallFailedException;
import org.alfresco.jive.JiveOpenClient;
import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.jive.SpaceNotFoundException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jive.AbstractJiveService;
import org.alfresco.repo.jive.AlreadySocializedException;
import org.alfresco.repo.jive.FileNotFoundException;
import org.alfresco.repo.jive.InvalidCommunityException;
import org.alfresco.repo.jive.JiveCommunity;
import org.alfresco.repo.jive.NotAFileException;
import org.alfresco.repo.jive.NotSocializedException;

/**
 * This class is the "real" implementation of the <code>JiveService</code>. It
 * makes calls to Jive on behalf of the user logged into Alfresco.
 * 
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveServiceImpl.java 41626 2012-09-14 23:59:00Z wabson $
 * @see org.alfresco.repo.jive.JiveService
 */
public class JiveServiceImpl extends AbstractJiveService {
	private final static Log log = LogFactory.getLog(JiveServiceImpl.class);

	private JiveOpenClient jiveOpenClient = null;

	public void setJiveOpenClient(final JiveOpenClient jiveOpenClient) {
		// PRECONDITIONS
		assert jiveOpenClient != null : "jiveOpenClient must not be null.";

		// Body
		this.jiveOpenClient = jiveOpenClient;
	}

	/**
	 * @see org.alfresco.repo.jive.JiveService#socializeDocuments(java.util.List,
	 *      long)
	 */
	@Override
	public void socializeDocuments(final List<NodeRef> nodeRefs,
			final long communityId) throws FileNotFoundException,
			NotAFileException, InvalidCommunityException,
			AlreadySocializedException, ServiceUnavailableException {
		final String userId = AuthenticationUtil.getFullyAuthenticatedUser();
		final List<NodeRef> socializedNodeRefs = new ArrayList<NodeRef>();

		validateNodeRefsAreSocializable(nodeRefs);

		for (final NodeRef nodeRef : nodeRefs) {
			final FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);

			if (fileInfo != null) {
				final ContentData contentData = fileInfo.getContentData();

				if (contentData != null) {
					try {
						jiveOpenClient.createDocument(userId, communityId,
								nodeRef.toString(), (String) nodeService
										.getProperty(nodeRef,
												ContentModel.PROP_NAME),
								contentData.getSize(), contentData
										.getMimetype());
					} catch (ServiceUnavailableException sue) {
						log.debug(sue.getMessage());
						throw sue;
					}

					socializedNodeRefs.add(nodeRef);
				} else {
					log
							.warn("Unable to obtain ContentData for nodeRef "
									+ String.valueOf(nodeRef)
									+ " during socialize operation. Document will not be socialized.");
				}
			} else {
				log
						.warn("Unable to obtain FileInfo for nodeRef "
								+ String.valueOf(nodeRef)
								+ " during socialize operation. Document will not be socialized.");
			}
		}

		markNodeRefsSocialized(socializedNodeRefs, communityId);
	}

	/**
	 * @see org.alfresco.repo.jive.JiveService#updateDocument(org.alfresco.service.cmr.repository.NodeRef,
	 *      boolean)
	 */
	@Override
	public void updateDocument(final NodeRef nodeRef,
			final boolean contentUpdated) throws FileNotFoundException,
			NotSocializedException, ServiceUnavailableException {
		final String userId = AuthenticationUtil.getFullyAuthenticatedUser();
		validateNodeRefIsSocialized(nodeRef);

		final FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);

		if (fileInfo != null) {
			final ContentData contentData = fileInfo.getContentData();

			if (contentData != null) {
				try {
					jiveOpenClient.updateDocument(userId, nodeRef.toString(),
							(String) nodeService.getProperty(nodeRef,
									ContentModel.PROP_NAME), contentData
									.getSize(), contentData.getMimetype());
				} catch (ServiceUnavailableException sue) {
					log.warn(sue.getMessage());
					throw sue;
				}
			} else {
				log
						.warn("Unable to obtain ContentData for nodeRef "
								+ String.valueOf(nodeRef)
								+ " during update operation. Update will not be sent to Jive.");
			}
		} else {
			log
					.warn("Unable to obtain FileInfo for nodeRef "
							+ String.valueOf(nodeRef)
							+ " during update operation. Update will not be sent to Jive.");
		}
	}

	/**
	 * @see org.alfresco.repo.jive.JiveService#getCommunities()
	 */
	@Override
	public List<JiveCommunity> getCommunities()
			throws ServiceUnavailableException {
		List<JiveCommunity> result = null;
		String userId = AuthenticationUtil.getFullyAuthenticatedUser();
		JSONObject json = null;

		try {
			json = jiveOpenClient.getSpaces(userId);
			result = getCommunitiesFromJson(json);
		} catch (ServiceUnavailableException sue) {
			log.info(sue.getMessage());
			throw sue;
		} catch (final JSONException je) {
			// Any kind of parsing issue with the JSON returned from Jive is
			// treated as a call failure
			throw new CallFailedException(je);
		}

		return (result);
	}

	/**
	 * @see org.alfresco.repo.jive.JiveService#getSubCommunities(long)
	 */
	@Override
	public List<JiveCommunity> getSubCommunities(long communityId)
			throws InvalidCommunityException, ServiceUnavailableException {
		List<JiveCommunity> result = null;
		String userId = AuthenticationUtil.getFullyAuthenticatedUser();
		JSONObject json = null;

		try {
			json = jiveOpenClient.getSubSpaces(userId, communityId);
			result = getCommunitiesFromJson(json);
		} catch (ServiceUnavailableException sue) {
			log.debug(sue.getMessage());
			throw sue;
		}

		catch (final JSONException je) {
			// Any kind of parsing issue with the JSON returned from Jive is
			// treated as a call failure
			throw new CallFailedException(je);
		} catch (final SpaceNotFoundException snfe) {
			// Jive's OpenClient API throws a SpaceNotFoundException when the
			// space itself is invalid or when that
			// space exists but has no children, making it impossible to
			// distinguish between these two cases.
			// So we handle them both the same way - by returning an empty list
			// of children. In the case where the
			// space really doesn't exist, the subsequent "socialize" call (if
			// any) will fail. Note that this isn't
			// as problematic as it sounds, since this situation can only arise
			// if a Jive Community is deleted in
			// between the call to getCommunities / getSubCommunities and the
			// subsequent "socialize" call.
			if (log.isDebugEnabled())
				log
						.debug(
								"The Jive Space with id "
										+ communityId
										+ " either doesn't exist or has no children (no way of knowing which).  Ignoring the exception and moving on.",
								snfe);

			result = new ArrayList<JiveCommunity>();
		}

		return (result);
	}

	/**
	 * Converts the Jive JSON representation of communities into a List of
	 * <code>JiveCommunity</code> objects.
	 * 
	 * @param json
	 *            The JSON to convert <i>(may be null)</i>.
	 * @return The list of <code>JiveCommunity</code> objects read from the JSON
	 *         <i>(will not be null, but may be empty).</i>
	 */
	private List<JiveCommunity> getCommunitiesFromJson(final JSONObject json)
			throws JSONException {
		List<JiveCommunity> result = new ArrayList<JiveCommunity>();

		if (json != null) {
			JSONArray communities = json.getJSONArray("data");

			if (communities != null) {
				for (int i = 0; i < communities.length(); i++) {
					JSONObject community = (JSONObject) communities.get(i);
					long id = community.getLong("id");
					String name = community.getString("name");

					result.add(new JiveCommunity(id, name));
				}
			}
		}

		return (result);
	}

}
