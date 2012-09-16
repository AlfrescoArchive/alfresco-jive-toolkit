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

import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.community.impl.dao.DocumentBean;

public interface CmisDocumentDAO {
	/**
     * Retrieve a document's bean by documentID and version. If no document is found null will be
     * returned.
     *
     * @param documentID the document ID of the document to return
     * @param versionID the version ID of the document to return
     * @return a bean containing the document
     * @throws DAOException if an error occurs retrieving the document
     */
    DocumentBean getByDocumentCmisID(String cmisID) throws DAOException;
}
