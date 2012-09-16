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

package org.alfresco.jive.cmis.dao;


import java.io.InputStream;
import java.util.List;

import org.alfresco.jive.cmis.manager.RemoteContainer;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

import com.jivesoftware.base.database.dao.DAOException;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: AlfrescoNavigationDAO.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public interface AlfrescoNavigationDAO
{
    /**
     * Gets a directory listing for the given {@link RemoteContainer} id. The
     * first item in the returned {@link List} is a count of the total number of
     * direcotires for the given container. The second entry is a {@link List}
     * of {@link RemoteContainer} results.
     * 
     * @param remoteContainer <code>null</code> for root container
     * @param firstRecord index of first record (for pagination).
     * <code>null</code> or 0 to start with first record.
     * @return an empty {@link List} if no sub directories exist
     */
    public List<Object> getDirectories(RemoteContainer remoteContainer, Long firstRecord);


    /**
     * Search for a {@link List} of {@link RemoteContainer}s for the given
     * searchString. The first item in the returned {@link List} is a count of
     * the total number of directories found for the given search criteria. The
     * second entry is a {@link List} of {@link RemoteContainer} results.
     * 
     * @param searchString
     * @param firstRecord
     * @return
     */
    public List<Object> searchDirectories(String searchString, Long firstRecord);


    /**
     * Gets all the files (not folders) under the given {@link RemoteContainer}.
     * The first item in the returned {@link List} is a count of the total
     * number of files for the given container. The second entry is a
     * {@link List} of {@link RemoteContainer} results.
     * 
     * @param remoteContainer <code>null</code> for root container
     * @param firstRecord index of first record (for pagination)
     * @return an empty {@link List} if no files exist
     */
    public List<Object> getFiles(RemoteContainer remoteContainer, Long firstRecord);


    /**
     * Search for a {@link List} of {@link RemoteContainer}s for the given
     * searchString. The first item in the returned {@link List} is a count of
     * the total number of files found for the given search criteria. The second
     * entry is a {@link List} of {@link RemoteContainer} results.
     * 
     * @param searchString
     * @param firstRecord
     * @return
     */
    public List<Object> searchFiles(String searchString, Long firstRecord);


    /**
     * @param container
     * @param fileName
     * @param contentType
     * @param size
     * @param data
     * @return
     * @throws CmisConstraintException
     */
    public Document createDocument(RemoteContainer container, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException;


    /**
     * @param docId
     * @return
     * @throws CmisConstraintException
     */
    public InputStream getDocumentContent(String docId)
        throws CmisConstraintException;
	
	/**
	 * Gets a single {@link RemoteContainer} object by it's remoteObjectId
	 * 
	 * @param remoteObjectId
	 * 
	 * @return <code>null</code> if object with specified id doesn't exist
	 */
	public RemoteContainer getRemoteObject(String remoteObjectId);


	/**
	 * @param doc
	 * @param fileName
	 * @param contentType
	 * @param size
	 * @param data
	 * @return
	 * @throws CmisConstraintException
	 */
	public Document updateDocument(RemoteDocument doc, String fileName, String contentType, long size, InputStream data)
			throws CmisConstraintException;


	/**
	 * @param docId
	 * @return
	 * @throws CmisConstraintException
	 */
	public RemoteDocument getDocumentMetadata(String docId)
			throws CmisConstraintException;
	
	
	/**
	 * @param doc
	 * @param fileName
	 * @return
	 * @throws CmisConstraintException
	 */
	public Document updateDocument(RemoteDocument doc, String fileName)
			throws CmisConstraintException;


	/**
	 * @return
	 * @throws CmisConnectionException
	 */
	public Session getSession() throws CmisConnectionException;


	/**
	 * @param doc
	 * @return
	 * @throws CmisConstraintException
	 */
	public Document deleteDocument(RemoteDocument doc) throws CmisConstraintException;
	
}
