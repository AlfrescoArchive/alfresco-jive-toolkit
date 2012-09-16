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


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.action.JiveContainerAware;
import com.jivesoftware.community.web.struts.SetReferer;
import com.opensymphony.xwork2.Preparable;


/**
 * Pick a container to create a new document or thread in. Will return success
 * if the user has permission to create a document/thread in at least one of the
 * spaces / communities in the system or unauthorized otherwise.
 */
@SuppressWarnings("serial")
@SetReferer(false)
public class ChooseContainerAction
    extends com.jivesoftware.community.action.ChooseContainerAction
    implements Preparable, JiveContainerAware
{

    private static final Logger     log     = LogManager.getLogger(ChooseContainerAction.class);

    public static final java.lang.String REMOTE_CONTAINER_INPUT = "remote-container-input";
    
    // document specifics
    protected boolean               managed = false;
    
    public String input() {
    	
    	if (isUpload() && isManaged()) {    	
    		if (getContainer() == null) {
                title = getText("create.containable.document.managed");
            }            
            else {
                title = getText("create.containable.in.container.document.managed",
                        new String[]{getContainer().getName()});
            }
    	}    	
    	
    	return INPUT;
    }

    public String execute()
    {
        String response = super.execute();

        if (response.equals(SUCCESS) && isUpload() && isManaged())
        {
        	return REMOTE_CONTAINER_INPUT;            
        }

        return response;

    }


    public boolean isManaged()
    {
        return managed;
    }


    public void setManaged(boolean managed)
    {
        this.managed = managed;
    }
   
}
