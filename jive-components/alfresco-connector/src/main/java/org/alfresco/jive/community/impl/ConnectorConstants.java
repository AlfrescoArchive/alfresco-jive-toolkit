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

package org.alfresco.jive.community.impl;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: ConnectorConstants.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class ConnectorConstants
{

    public static final int    MANAGED_TYPE              = 2;

    public static final String REMOTE_CONTAINER_PROPERTY = "cmisContainer";
    public static final String REMOTE_OBJECT_PROPERTY    = "cmisObject";
	
	public static final String USERNAME_ENCRYPTION_PASSWORD = "jivetoolkit.shared.password";
	public static final String USERNAME_ENCRYPTION_PASSWORD_DEFAULT = "CHANGEME!";
	
	public static final String ALFRESCO_USER = "jivetoolkit.alfresco.user";
	public static final String ALFRESCO_PASSWORD = "jivetoolkit.alfresco.password";	
	
	// Example: http://localhost:8080/alfresco 
	public static final String ALFRESCO_URL = "jivetoolkit.alfresco.url";
	
	public static final String ALFRESCO_ENDPOINT = "/jiveservice/cmis";
	

}
