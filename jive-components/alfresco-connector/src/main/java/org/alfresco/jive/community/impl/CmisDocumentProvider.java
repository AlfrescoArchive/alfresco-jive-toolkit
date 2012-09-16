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



import com.jivesoftware.base.User;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.ApprovalManager;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.impl.BinaryBodyProvider;
import com.jivesoftware.community.impl.DbDocument;
import com.jivesoftware.community.impl.DocumentProvider;
import com.jivesoftware.community.impl.QueryCacheManager;
import com.jivesoftware.community.impl.VersionManagerProvider;
import com.jivesoftware.community.impl.dao.DocumentBean;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisDocumentProvider.java 31549 2011-10-28 23:07:23Z cferguson $
 *
 */
public class CmisDocumentProvider
    extends com.jivesoftware.community.impl.DocumentProvider
{

    private BinaryBodyProvider     binaryBodyProvider;
    private CmisBinaryBodyProvider cmisBinaryBodyProvider;
    private QueryCacheManager      queryCacheManager;
    private VersionManagerProvider versionManagerProvider;
    private ApprovalManager        approvalManager;
    private EventDispatcher        eventDispatcher;


    public DbDocument getDocument(DocumentBean bean)
    {
        if (bean.getDocumentTypeID() == ConnectorConstants.MANAGED_TYPE)
        {
            return new CmisDocument(bean, queryCacheManager, cmisBinaryBodyProvider, versionManagerProvider, approvalManager, eventDispatcher);
        }
        return new DbDocument(bean, queryCacheManager, binaryBodyProvider, versionManagerProvider, approvalManager, eventDispatcher);
    }


    public DbDocument getDocument(User user, DocumentType documentType, String documentID, String title, org.w3c.dom.Document body)
        throws DuplicateIDException
    {
        if (documentType.getID() == ConnectorConstants.MANAGED_TYPE)
        {
            return new CmisDocument(user, documentType, documentID, title, body, queryCacheManager, cmisBinaryBodyProvider, versionManagerProvider, approvalManager, eventDispatcher);
        }
        return new DbDocument(user, documentType, documentID, title, body, queryCacheManager, binaryBodyProvider, versionManagerProvider, approvalManager, eventDispatcher);
    }


    public void setBinaryBodyProvider(BinaryBodyProvider binaryBodyProvider)
    {
        this.binaryBodyProvider = binaryBodyProvider;
    }


    public void setQueryCacheManager(QueryCacheManager queryCacheManager)
    {
        this.queryCacheManager = queryCacheManager;
    }


    public void setVersionManagerProvider(VersionManagerProvider versionManagerProvider)
    {
        this.versionManagerProvider = versionManagerProvider;
    }


    public void setApprovalManager(ApprovalManager approvalManager)
    {
        this.approvalManager = approvalManager;
    }


    public EventDispatcher getEventDispatcher()
    {
        return eventDispatcher;
    }


    public void setEventDispatcher(EventDispatcher eventDispatcher)
    {
        this.eventDispatcher = eventDispatcher;
    }


    public void setCmisBinaryBodyProvider(CmisBinaryBodyProvider cmisBinaryBodyProvider)
    {
        this.cmisBinaryBodyProvider = cmisBinaryBodyProvider;
    }

}
