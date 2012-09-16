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

package org.alfresco.jive.community.places;

import java.util.HashMap;
import java.util.Map;

import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.places.PlaceCreateDocumentLink;

public class PlaceCreateManagedDocumentLink extends PlaceCreateDocumentLink {

	

    @Override
    public String getUrl() {    
     	return isUploadable() ? "/choose-container-remote!input.jspa" : "/choose-document-remote!input.jspa";    
    }
    
    @Override
    public Map<String, String> getUrlParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("containerType", String.valueOf(uiComponentContext.getContainer().getObjectType()));
        params.put("container", String.valueOf(uiComponentContext.getContainer().getID()));
        params.put("contentType", String.valueOf(JiveConstants.DOCUMENT));
        params.put("upload", String.valueOf(isUploadable()));
        params.put("managed", String.valueOf(true));
        return params;
    }
}
