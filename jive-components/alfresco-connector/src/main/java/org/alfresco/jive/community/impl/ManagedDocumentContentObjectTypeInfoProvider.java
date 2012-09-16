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


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.impl.DocumentContentObjectTypeInfoProvider;
import com.jivesoftware.community.objecttype.ContentObjectTypeInfoProvider;
import com.jivesoftware.util.StringUtils;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: ManagedDocumentContentObjectTypeInfoProvider.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class ManagedDocumentContentObjectTypeInfoProvider
    extends DocumentContentObjectTypeInfoProvider
    implements ContentObjectTypeInfoProvider
{

    private static final Logger log = Logger.getLogger(ManagedDocumentContentObjectTypeInfoProvider.class);


    public String getCreateNewFormRelativeURL(JiveContainer targetContainer, boolean isUpload, String temporaryObjectId,
            String tags, String subject)
    {
        StringBuilder url = new StringBuilder();
        if (isUpload)
        {
            url.append("doc-managed-upload!input.jspa?");
        }
        else
        {
            url.append("doc-managed-link!input.jspa?");
        }
        url.append("container=");
        url.append(targetContainer.getID());
        url.append("&containerType=");
        url.append(targetContainer.getObjectType());

        // tags
        url.append("&tags=");
        String urlEncodedTags = tags;
        if (!StringUtils.isEmpty(tags))
        {
            try
            {
                urlEncodedTags = URLEncoder.encode(tags, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        url.append(!StringUtils.isEmpty(tags) ? urlEncodedTags : "");

        // subject
        url.append("&subject=");
        String urlEncodedSubject = subject;
        if (!StringUtils.isEmpty(subject))
        {
            try
            {
                urlEncodedSubject = URLEncoder.encode(subject, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        url.append(!StringUtils.isEmpty(subject) ? urlEncodedSubject : "");

        return url.toString();
    }
}
