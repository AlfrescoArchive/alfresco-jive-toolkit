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

import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.BinaryBodyManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.impl.BinaryBodyProvider;
import com.jivesoftware.community.impl.dao.BinaryBodyBean;
import com.jivesoftware.eos.StorageProvider;
import com.jivesoftware.util.StringUtils;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisBinaryBodyProvider.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CmisBinaryBodyProvider
    extends BinaryBodyProvider
{

    private StorageProvider             storageProvider;
    private BinaryBodyManager           binaryBodyManager;
    private Cache<Long, BinaryBodyBean> binaryBodyCache;
    private CmisStorageProvider         cmisStorageProvider;


    public CmisBinaryBody getBinaryBody(Document document, BinaryBodyBean bean)
        throws DocumentObjectNotFoundException
    {

        String objectID = document.getProperties().get(ConnectorConstants.REMOTE_OBJECT_PROPERTY);

        if (StringUtils.isNullOrEmpty(objectID))
        {
            throw new DocumentObjectNotFoundException("Remote object ID is not set");
        }

        return new CmisBinaryBody(bean, storageProvider, binaryBodyManager, binaryBodyCache, objectID, document.getSubject(), cmisStorageProvider);
    }


    public CmisBinaryBody getBinaryBody(Document document, long id, String name, String contentType, InputStream data)
        throws BinaryBodyException
    {

        String remoteContainerID = document.getProperties().get(ConnectorConstants.REMOTE_CONTAINER_PROPERTY);
        String remoteDocumentID = document.getProperties().get(ConnectorConstants.REMOTE_OBJECT_PROPERTY);
        
        if (StringUtils.isNullOrEmpty(remoteContainerID) && StringUtils.isNullOrEmpty(remoteDocumentID))
        {
            throw new BinaryBodyException(BinaryBodyException.GENERAL_ERROR, "Remote Container or Remote Document not selected");
        }

        return new CmisBinaryBody(id, name, contentType, data, storageProvider, binaryBodyManager, binaryBodyCache, remoteContainerID, 
        		remoteDocumentID, document.getSubject(), cmisStorageProvider);
    }

	public CmisBinaryBody getBinaryBody(Document document, long id,	String name, String contentType, int size)
			throws BinaryBodyException {

		
		String remoteDocumentID = document.getProperties().get(
				ConnectorConstants.REMOTE_OBJECT_PROPERTY);

		if (StringUtils.isNullOrEmpty(remoteDocumentID)) {
			throw new BinaryBodyException("Remote document not selected");
		}

		return new CmisBinaryBody(id, name, contentType, size, storageProvider,
				binaryBodyManager, binaryBodyCache, 
				remoteDocumentID, document.getSubject(), cmisStorageProvider);
	}

    public void setStorageProvider(StorageProvider storageProvider)
    {
        this.storageProvider = storageProvider;
    }


    public void setBinaryBodyManager(BinaryBodyManager binaryBodyManager)
    {
        this.binaryBodyManager = binaryBodyManager;
    }


    public void setBinaryBodyCache(Cache<Long, BinaryBodyBean> binaryBodyCache)
    {
        this.binaryBodyCache = binaryBodyCache;
    }


    public void setCmisStorageProvider(CmisStorageProvider cmisStorageProvider)
    {
        this.cmisStorageProvider = cmisStorageProvider;
    }

}
