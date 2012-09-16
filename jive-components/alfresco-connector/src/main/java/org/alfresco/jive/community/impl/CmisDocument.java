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


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.jive.cmis.dao.CmisDocumentDAO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.Document;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.ApprovalManager;
import com.jivesoftware.community.BinaryBody;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.event.DocumentEvent;
import com.jivesoftware.community.impl.BinaryBodyProvider;
import com.jivesoftware.community.impl.DbDocument;
import com.jivesoftware.community.impl.QueryCacheManager;
import com.jivesoftware.community.impl.VersionManagerProvider;
import com.jivesoftware.community.impl.dao.DocumentBean;
import com.jivesoftware.community.impl.dao.DocumentDAO;
import com.jivesoftware.community.lifecycle.JiveApplication;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisDocument.java 28125 2011-05-31 17:33:00Z vpokotylo $
 *
 */
public class CmisDocument
    extends DbDocument
    implements Document
{

    private static final Logger    log = LogManager.getLogger(CmisDocument.class);

    private CmisBinaryBodyProvider cmisBinaryBodyProvider;


    public CmisDocument(DocumentBean bean, QueryCacheManager queryCacheManager, BinaryBodyProvider binaryBodyProvider,
            VersionManagerProvider versionManagerProvider, ApprovalManager approvalManager, EventDispatcher dispatcher)
    {
        super(bean, queryCacheManager, binaryBodyProvider, versionManagerProvider, approvalManager, dispatcher);
        // TODO Auto-generated constructor stub
        this.cmisBinaryBodyProvider = (CmisBinaryBodyProvider)binaryBodyProvider;
    }


    public CmisDocument(User author, DocumentType documentType, String documentID, String title, org.w3c.dom.Document body,
            QueryCacheManager queryCacheManager, BinaryBodyProvider binaryBodyProvider,
            VersionManagerProvider versionManagerProvider, ApprovalManager approvalManager, EventDispatcher dispatcher)
        throws DuplicateIDException
    {
        super(author, documentType, documentID, title, body, queryCacheManager, binaryBodyProvider, versionManagerProvider, approvalManager, dispatcher);

        this.cmisBinaryBodyProvider = (CmisBinaryBodyProvider)binaryBodyProvider;
        // TODO Auto-generated constructor stub
    }


    public BinaryBody setBinaryBody(String name, String contentType, InputStream data)
        throws IllegalStateException,
            BinaryBodyException
    {

        CmisBinaryBody body = cmisBinaryBodyProvider.getBinaryBody(this, bean.getID(), name, contentType, data);

        this.getProperties().put(ConnectorConstants.REMOTE_OBJECT_PROPERTY, body.getCmisObjectId());

        // String remoteContainerID =
        // this.getProperties().get(ConnectorConstants.REMOTE_CONTAINER_PROPERTY);
        // body.createCmisBinaryBody(data, remoteContainerID);

        bean.setBinaryBody(body.getBean());
        bean.setBodyText(null);
        // getRenderCacheManager().clearContentCache(this,
        // JiveContentObject.Field.Body);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Type", "binaryBodyModify");
        events.add(createDocumentEvent(params, DocumentEvent.Type.MODIFIED));
        bean.setVersionableFieldChanged(true);

        return body;
    }
	
	public BinaryBody setLinkedBinaryBody(String objectId, String name, String contentType, int size) throws BinaryBodyException, DocumentAlreadyExistsException {
		
		DocumentBean docBean = null;
		try {
			docBean = getCmisDocumentDAO().getByDocumentCmisID(objectId);
		} catch (DAOException e) {
			log.error(e.getMessage(), e);
		}

		if (docBean != null && docBean.getDocID() != bean.getDocID()) {
			throw new DocumentAlreadyExistsException("Published document with this CMIS ID already exists");
		}
		
		this.getProperties().put(ConnectorConstants.REMOTE_OBJECT_PROPERTY, objectId);

        CmisBinaryBody body = cmisBinaryBodyProvider.getBinaryBody(this, bean.getID(),	name, contentType, size);
		
		bean.setBinaryBody(body.getBean());
		bean.setBodyText(null);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Type", "binaryBodyModify");
		events.add(createDocumentEvent(params, DocumentEvent.Type.MODIFIED));
		bean.setVersionableFieldChanged(true);

		return body;
	}

    public BinaryBody getBinaryBody()
    {
        if (isTextBody())
        {
            return null;
        }

        try
        {
            return cmisBinaryBodyProvider.getBinaryBody(this, bean.getBinaryBody());
        }
        catch (DocumentObjectNotFoundException e)
        {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }
    }
    
    private static JiveContext jiveContext() {
        return JiveApplication.getContext();
    }

    public CmisDocumentDAO getCmisDocumentDAO() {
        return (CmisDocumentDAO) jiveContext().getSpringBean("cmisDocumentDAO");
    }

    private DocumentEvent createDocumentEvent(Map<String, Object> params, DocumentEvent.Type eventType)
    {
        return new DocumentEvent(eventType, this, getJiveContainer(), params);
    }


    public void setCmisBinaryBodyProvider(CmisBinaryBodyProvider cmisBinaryBodyProvider)
    {
        this.cmisBinaryBodyProvider = cmisBinaryBodyProvider;
    }
}
