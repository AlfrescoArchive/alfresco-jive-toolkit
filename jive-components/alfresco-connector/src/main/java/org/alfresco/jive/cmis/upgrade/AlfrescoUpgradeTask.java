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

package org.alfresco.jive.cmis.upgrade;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

public class AlfrescoUpgradeTask implements UpgradeTask {
	
	protected static final Logger log = LogManager.getLogger(AlfrescoUpgradeTask.class);

	public String getName() {
		return "Add Managed Document Type Record";
	}

	public String getDescription() {
		return "Add Managed Document Type Record";
	}

	public String getEstimatedRunTime() {
		return "A few seconds";
	}

	public String getInstructions() {
		return String.format("To perform the necessary database tasks, execute the following statement:\n\n"
						+ UpgradeUtils.processSQLGenFile("schema-custom.xml"));
	}

	public boolean isBackgroundTask() {
		return false;
	}

	public void doTask() throws Exception {				
		UpgradeUtils.executeSQLGenFile("schema-custom.xml", false);
		
	}

}

