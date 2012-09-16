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

import java.sql.SQLException;

import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.DbDocumentDAO;
import com.jivesoftware.community.impl.dao.DocumentBean;
import org.springframework.dao.EmptyResultDataAccessException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DbCmisDocumentDAO extends DbDocumentDAO implements CmisDocumentDAO {

	private static final Logger log = LogManager.getLogger(DbDocumentDAO.class);
	
	private static final String       QUERY_JIVE_ID        = "select max(dv.internaldocid) from jivedocumentprop dp, jivedocversion dv" +
									" where dp.name = 'cmisObject' and dp.propvalue = ? and" 
		+ " dp.internaldocid=dv.internaldocid and dp.versionid=dv.versionid";
	
	@Override
	public DocumentBean getByDocumentCmisID(String documentCmisID)
			throws DAOException {
		
		long documentId = -1;
		try {
			documentId = getSimpleJdbcTemplate().queryForLong(QUERY_JIVE_ID, documentCmisID);
						
		} catch (EmptyResultDataAccessException e) {
            return null;
        }
		
		return getByID(documentId);
	}

}
