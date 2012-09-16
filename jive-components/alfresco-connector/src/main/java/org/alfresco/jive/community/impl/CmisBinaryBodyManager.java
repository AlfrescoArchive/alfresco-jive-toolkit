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

import org.apache.log4j.Logger;

import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.impl.DbBinaryBodyManager;
import com.jivesoftware.community.impl.dao.BinaryBodyBean;
import com.jivesoftware.community.impl.dao.BinaryBodyDAO;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisBinaryBodyManager.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CmisBinaryBodyManager
    extends DbBinaryBodyManager
{

    private static final Logger    log = Logger.getLogger(DbBinaryBodyManager.class);

    private BinaryBodyDAO          binaryBodyDAO;

    private DocumentManager        documentManager;
    private CmisBinaryBodyProvider cmisBinaryBodyProvider;
    private CmisStorageProvider    cmisStorageProvider;


    public CmisBinaryBodyManager(long docTypeID)
    {
        super(docTypeID);
        // TODO Auto-generated constructor stub
    }


    public InputStream getBinaryBodyByID(long binaryBodyID)
    {
        InputStream in = null;

        try
        {
            BinaryBodyBean bean = binaryBodyDAO.getByBodyID(binaryBodyID);

            if (bean != null)
            {
                Document document = documentManager.getDocument(bean.getDocID());

                if (document.getDocumentType().getID() == ConnectorConstants.MANAGED_TYPE)
                {
                    CmisBinaryBody binaryBody = cmisBinaryBodyProvider.getBinaryBody(document, bean);
                    in = cmisStorageProvider.getStream(CmisStorageUtil.getStorageKey(binaryBody));
                    if (in == null)
                    {
                        log.error(String.format("Unable to retrieve data for BinaryBody %s", bean.getID()));
                    }
                }
                else
                {
                    in = super.getBinaryBodyByID(binaryBodyID);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return in;
    }


    public void setBinaryBodyDAO(BinaryBodyDAO binaryBodyDAO)
    {
        super.setBinaryBodyDAO(binaryBodyDAO);
        this.binaryBodyDAO = binaryBodyDAO;
    }


    public void setDocumentManager(DocumentManager documentManager)
    {
        this.documentManager = documentManager;
    }


    public void setCmisBinaryBodyProvider(CmisBinaryBodyProvider cmisBinaryBodyProvider)
    {
        this.cmisBinaryBodyProvider = cmisBinaryBodyProvider;
    }


    public void setCmisStorageProvider(CmisStorageProvider cmisStorageProvider)
    {
        this.cmisStorageProvider = cmisStorageProvider;
    }
}
