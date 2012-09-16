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

import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

/**
 * This class TODO
 *
 * @author 
 * @version $Id: AlfrescoNavigationManager.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public interface AlfrescoNavigationManager
{
	
	/**
	 * Gets a single {@link RemoteContainer} object by it's remoteObjectId
	 * 
	 * @param remoteObjectId
	 * 
	 * @return <code>null</code> if object with specified id doesn't exist
	 */
	public RemoteContainer getRemoteObject(String remoteObjectId);

    /**
     * Gets directory listing from Alfresco for the given directory key.
     * 
     * @param directories {@link List} of {@link RemoteContainer} to fill
     * @param key Alfresco identifier for directory. If null, use root directory
     * @param firstRecord index of first record for pagination.
     * <code>null</code> or 0 to start with first record
     * @return A count of all records (without pagination)
     */
    public long fillDirectories(List<RemoteContainer> directories, String key, Long firstRecord);


    /**
     * Search for a {@link List} of directories for the given searchString.
     * 
     * @param searchString
     * @param firstRecord
     * @return A count of all records (without pagination)
     */
    public long searchDirectories(List<RemoteContainer> directories, String searchString, Long firstRecord);


    /**
     * Gets file listing from Alfresco for the given directory key.
     * 
     * @param key Alfresco identifier for directory. If null, use root directory
     * @param firstRecord index of first record for pagination.
     * <code>null</code> or 0 to start with first record
     * @return A count of all records (without pagination)
     */
    public long getFiles(String key, Long firstRecord, List<RemoteContainer> directories, List<RemoteContainer> files);


    /**
     * Search for a {@link List} of files for the given searchString
     * 
     * @param searchString
     * @param firstRecord
     * @return A count of all records (without pagination)
     */
    public long searchFiles(String searchString, Long firstRecord, List<RemoteContainer> directories, List<RemoteContainer> files);


    /**
     * @param data
     * @return
     * @throws CmisConstraintException
     */
    public String createDocument(RemoteContainer container, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException;


    /**
     * @param docId
     * @return
     * @throws CmisConstraintException
     */
    public InputStream getDocumentContent(String docId)
        throws CmisConstraintException;

	/**
	 * @param doc
	 * @param fileName
	 * @param contentType
	 * @param size
	 * @param data
	 * @return
	 * @throws CmisConstraintException
	 */
	public String updateDocument(RemoteDocument doc, String fileName, String contentType, long size, InputStream data)
			throws CmisConstraintException;

	/**
	 * @param docId
	 * @return
	 * @throws CmisConstraintException
	 */
	public RemoteDocument getDocumentMetadata(String docId)
			throws CmisConstraintException;
	
	/**
	 * 
	 * @param docId
	 * @return
	 */
	public long getJiveId(String docId);

	
	/**
	 * @param doc
	 * @param fileName
	 * @return
	 * @throws CmisConstraintException
	 */
	public String updateDocument(RemoteDocument doc, String fileName)
			throws CmisConstraintException;

	/**
	 * @throws CmisConnectionException
	 */
	public void connect() throws CmisConnectionException;

	/**
	 * @param doc
	 * @return
	 * @throws CmisConstraintException
	 */
	public String deleteDocument(RemoteDocument doc) throws CmisConstraintException;
}
