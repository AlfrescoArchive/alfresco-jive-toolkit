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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.jive.CallFailedException;
import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.repo.jive.JiveCommunity;
import org.alfresco.repo.jive.JiveService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommunitiesConstraint extends BaseParameterConstraint {

	private static Log logger = LogFactory.getLog(CommunitiesConstraint.class);

	public static final String NAME = "communities";

	// int depth = 1;
	String prefix = "/";

	private JiveService jiveService;

	public void setJiveService(JiveService jiveService) {
		this.jiveService = jiveService;
	}

	@Override
	protected Map<String, String> getAllowableValuesImpl() {

		Map<String, String> results = new LinkedHashMap<String, String>();

		try {
			List<JiveCommunity> communities = jiveService.getCommunities();

			for (Iterator<JiveCommunity> iterator = communities.iterator(); iterator
					.hasNext();) {
				JiveCommunity jiveCommunity = iterator.next();

				results.put(new Long(jiveCommunity.getId()).toString(), prefix
						+ jiveCommunity.getName());

				results.putAll(getSubCommunities(jiveCommunity.getId(), prefix,
						jiveCommunity.getName()));

			}
		} catch (ServiceUnavailableException sue) {
			logger.info(sue.getMessage());
		} catch (CallFailedException cfe) {
			logger.info("Unable to Connect to Jive: " + cfe.getMessage());
		}

		return results;
	}

	private Map<String, String> getSubCommunities(Long communityID,
			String prefix, String name)
			throws org.alfresco.jive.ServiceUnavailableException {
		Map<String, String> results = new LinkedHashMap<String, String>();

		List<JiveCommunity> subCommunities = null;

		try {
			subCommunities = jiveService.getSubCommunities(communityID);

		} catch (org.alfresco.jive.ServiceUnavailableException sue) {
			throw sue;
		}

		for (Iterator<JiveCommunity> iterator = subCommunities.iterator(); iterator
				.hasNext();) {
			JiveCommunity jiveSubCommunity = iterator.next();

			results.put(new Long(jiveSubCommunity.getId()).toString(), prefix
					+ name + prefix + jiveSubCommunity.getName());

			results.putAll(getSubCommunities(jiveSubCommunity.getId(), prefix,
					jiveSubCommunity.getName()));

		}

		return results;
	}

	public String repeat(String str, int times) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < times; i++)
			ret.append(str);
		return ret.toString();
	}

}
