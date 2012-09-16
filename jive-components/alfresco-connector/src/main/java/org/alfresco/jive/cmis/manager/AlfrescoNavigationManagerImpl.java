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

package org.alfresco.jive.cmis.manager;


import java.io.InputStream;
import java.util.List;

import org.alfresco.jive.cmis.dao.AlfrescoNavigationDAO;
import org.alfresco.jive.cmis.dao.CmisDocumentDAO;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.log4j.Logger;

import com.jivesoftware.community.impl.dao.DocumentBean;

/**
 * This class TODO
 *
 * @author 
 * @version $Id: AlfrescoNavigationManagerImpl.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class AlfrescoNavigationManagerImpl
    implements AlfrescoNavigationManager
{
    Logger log = Logger.getLogger(AlfrescoNavigationManagerImpl.class);

    // /////////////////////
    // AUTOWIRED MEMBERS //
    // /////////////////////
    AlfrescoNavigationDAO alfrescoNavigationDAO;
    
    CmisDocumentDAO cmisDocumentDAO;


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void connect() throws CmisConnectionException
    {
        alfrescoNavigationDAO.getSession();        
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public long fillDirectories(List<RemoteContainer> directories, String key, Long firstRecord)
    {
        log.info("Getting directory structure for remoteContainerId: " + key);

        RemoteContainer params = new RemoteContainer(key, null);

        List<Object> results = alfrescoNavigationDAO.getDirectories(params, firstRecord);

        long count = (Long)results.get(0);
        directories.addAll((List<RemoteContainer>)results.get(1));

        return count;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public long searchDirectories(List<RemoteContainer> directories, String searchString, Long firstRecord)
    {

        List<Object> results = alfrescoNavigationDAO.searchDirectories(searchString, firstRecord);

        long count = (Long)results.get(0);
        directories.addAll((List<RemoteContainer>)results.get(1));

        log.info("Number of directories for for given searchString: " + count);

        return count;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getFiles(String key, Long firstRecord, List<RemoteContainer> directories, List<RemoteContainer> files)
    {
        RemoteContainer params = new RemoteContainer(key, null);

        List<Object> results = alfrescoNavigationDAO.getFiles(params, firstRecord);

        long count = (Long)results.get(0);
        @SuppressWarnings("unchecked")
        List<RemoteContainer> remoteFiles = (List<RemoteContainer>)results.get(1);

        for (RemoteContainer remoteFile : remoteFiles)
        {
            if (!remoteFile.isFile())
            {
                directories.add(remoteFile);
            }
            else
            {
                files.add(remoteFile);
            }
        }

        return count;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long searchFiles(String searchString, Long firstRecord, List<RemoteContainer> directories, List<RemoteContainer> files)
    {

        List<Object> results = alfrescoNavigationDAO.searchFiles(searchString, firstRecord);

        long count = (Long)results.get(0);
        List<RemoteContainer> remoteContainers = (List<RemoteContainer>)results.get(1);

        for (RemoteContainer remoteContainer : remoteContainers)
        {
            if (!remoteContainer.isFile())
            {
                directories.add(remoteContainer);
            }
            else
            {
                files.add(remoteContainer);
            }
        }

        return count;
    }


    // /////////////////////
    // AUTOWIRED SETTERS //
    // /////////////////////

    public void setAlfrescoNavigationDAO(AlfrescoNavigationDAO alfrescoNavigationDAO)
    {
        this.alfrescoNavigationDAO = alfrescoNavigationDAO;
    }


    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.jive.cmis.manager.AlfrescoNavigationManager#createDocument
     * (java.io.InputStream)
     */
    @Override
    public String createDocument(RemoteContainer container, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException
    {

        Document document = alfrescoNavigationDAO.createDocument(container, fileName, contentType, size, data);

        return document.getId();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.jive.cmis.manager.AlfrescoNavigationManager#createDocument
     * (java.io.InputStream)
     */
    @Override
    public String updateDocument(RemoteDocument doc, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException
    {

    	Document document = alfrescoNavigationDAO.updateDocument(doc, fileName, contentType, size, data);
        return document.getId();
    }

    @Override
    public String updateDocument(RemoteDocument doc, String fileName)
        throws CmisConstraintException
    {
        Document document = alfrescoNavigationDAO.updateDocument(doc, fileName);
        return document.getId();
    	
    }
    
    @Override
    public String deleteDocument(RemoteDocument doc)
        throws CmisConstraintException
    {
        Document document = alfrescoNavigationDAO.deleteDocument(doc);
        return document.getId();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getDocumentContent(String docId)
        throws CmisConnectionException, CmisConstraintException
    {

        return alfrescoNavigationDAO.getDocumentContent(docId);
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public RemoteDocument getDocumentMetadata(String docId)
			throws CmisConstraintException {

		return alfrescoNavigationDAO.getDocumentMetadata(docId);
	}
        
        
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteContainer getRemoteObject(String remoteObjectId) {
		return alfrescoNavigationDAO.getRemoteObject(remoteObjectId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getJiveId(String docId) {
		DocumentBean documentBean = cmisDocumentDAO.getByDocumentCmisID(docId);
		
		return (documentBean != null) ? documentBean.getDocID() : -1;
	}

	public void setCmisDocumentDAO(CmisDocumentDAO cmisDocumentDAO) {
		this.cmisDocumentDAO = cmisDocumentDAO;
	}

}
