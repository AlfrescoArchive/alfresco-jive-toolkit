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

package org.alfresco.jive.community.web.struts.mapping;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.dispatcher.mapper.ActionMapping;

public class DocURLMapping extends
		com.jivesoftware.community.web.struts.mapping.DocURLMapping {

	public void process(String uri, ActionMapping mapping) {
        String[] uriElements = uri.split("/");
        Map<String,String> params = mapping.getParams();
        if (null == params) {
            params = new HashMap<String,String>();
        }

        if (uriElements.length <= 2) {
            mapping.setName("content");
            params.put("filterID", "contentstatus[published]~objecttype~objecttype[document]");
        }

        if (uriElements.length > 2) {
            // document (documentID) is always the second element
            if (uriElements[2].endsWith(".pdf")) {
                params.put("document", uriElements[2].replace(".pdf", ""));
            }
            else {
                params.put("document", uriElements[2]);
            }
        }
        if (uriElements.length == 3) {
            if (uriElements[2].endsWith(".pdf")) {
                mapping.setName("document-pdf");
            }
            else {
                mapping.setName("document");
            }
        }
        else if (uriElements.length == 4) {
            if (uriElements[3].equals("collaborate")) {
                mapping.setName("doc-collaborate");
                mapping.setMethod("input");
            }
            else if (uriElements[3].equals("edit")) {
                mapping.setName("doc-edit");
                mapping.setMethod("input");
            }
            else if (uriElements[3].equals("upload")) {
                mapping.setName("doc-upload-edit");
                mapping.setMethod("input");
            }
            else if (uriElements[3].equals("managed")) {
                mapping.setName("doc-managed-upload-edit");
                mapping.setMethod("input");
            }
            else if (uriElements[3].equals("diff")) {
                mapping.setName("doc-diff");
            }
            else if (uriElements[3].equals("restore")) {
                mapping.setName("doc-restore");
            }
            else if (uriElements[3].equals("deleteVersion")) {
                mapping.setName("doc-version-delete");
            }
            else if (uriElements[3].equals("version")) {
                mapping.setName("doc-version");
            }
            else if (uriElements[3].equals("delete")) {
                mapping.setName("doc-delete");
            }
        }
        else if (uriElements.length > 4 && uriElements[3].equals("edit")) {
            mapping.setName("doc-edit");
            mapping.setMethod("input");
            if ("version".equals(uriElements[4]) && uriElements.length > 5) {
                params.put("version", uriElements[5]);                
            }
        }
        else if (uriElements.length > 4) {
            mapping.setName("document");
            if ("version".equals(uriElements[3])) {
                params.put("version", uriElements[4]);
            }
        }

        mapping.setNamespace("");
        mapping.setParams(params);
    }
}
