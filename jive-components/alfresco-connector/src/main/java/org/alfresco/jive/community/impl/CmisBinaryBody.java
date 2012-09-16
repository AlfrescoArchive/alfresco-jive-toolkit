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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.jive.cmis.manager.RemoteContainer;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.BinaryBodyManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.ImageException;
import com.jivesoftware.community.impl.DbBinaryBody;
import com.jivesoftware.community.impl.StorageUtil;
import com.jivesoftware.community.impl.dao.BinaryBodyBean;
import com.jivesoftware.eos.StorageProvider;
import com.jivesoftware.util.StringUtils;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisBinaryBody.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CmisBinaryBody
    extends DbBinaryBody
{

    private static final Logger log = LogManager.getLogger(CmisBinaryBody.class);

    private BinaryBodyManager   binaryBodyManager;

    private CmisStorageProvider cmisStorageProvider;

    private String              cmisObjectId;

    private String              title;
    
    /**
     * @param bean
     * @param storageProvider
     * @param binaryBodyManager
     * @param binaryBodyCache
     * @throws DocumentObjectNotFoundException
     */
    public CmisBinaryBody(BinaryBodyBean bean, StorageProvider storageProvider, BinaryBodyManager binaryBodyManager,
            Cache<Long, BinaryBodyBean> binaryBodyCache, String cmisObjectId, String title, CmisStorageProvider cmisStorageProvider)
        throws DocumentObjectNotFoundException
    {
        super(bean, storageProvider, binaryBodyManager, binaryBodyCache);
        this.cmisStorageProvider = cmisStorageProvider;
        this.cmisObjectId = cmisObjectId;
        this.title = title;
    }


    /**
     * @param docID
     * @param name
     * @param contentType
     * @param data
     * @param storageProvider
     * @param binaryBodyManager
     * @param binaryBodyCache
     * @throws BinaryBodyException
     */
    public CmisBinaryBody(long docID, String name, String contentType, InputStream data, StorageProvider storageProvider,
            BinaryBodyManager binaryBodyManager, Cache<Long, BinaryBodyBean> binaryBodyCache, String containerCmisObjectId,
            String documentCmisObjectId, String title, CmisStorageProvider cmisStorageProvider)
        throws BinaryBodyException
    {
        super(docID, name, contentType, data, storageProvider, binaryBodyManager, binaryBodyCache);
        this.binaryBodyManager = binaryBodyManager;
        this.cmisStorageProvider = cmisStorageProvider;
        this.title = title;
        createCmisBinaryBody(data, containerCmisObjectId, documentCmisObjectId);
    }

    
    public CmisBinaryBody(long docID, String name, String contentType, int size, StorageProvider storageProvider,
            BinaryBodyManager binaryBodyManager, Cache<Long, BinaryBodyBean> binaryBodyCache, 
            String documentCmisObjectId, String title, CmisStorageProvider cmisStorageProvider)
        throws BinaryBodyException
    {
        super(docID, name, contentType, null, storageProvider, binaryBodyManager, binaryBodyCache);
        this.binaryBodyManager = binaryBodyManager;
        this.cmisStorageProvider = cmisStorageProvider;
        this.title = title;
        getBean().setSize(size);
        createCmisBinaryBody(documentCmisObjectId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { BinaryBodyException.class })
    public void createCmisBinaryBody(InputStream data, String containerCmisObjectId, String documentCmisObjectId)
        throws BinaryBodyException
    {
        // See if the contentType is valid.
        if (!binaryBodyManager.isValidType(getBean().getContentType()))
        {
            throw new BinaryBodyException(BinaryBodyException.BAD_CONTENT_TYPE);
        }

        try
        {
            getBinaryBodyDAO().create(getBean());
            this.cmisObjectId = insert(data, containerCmisObjectId, documentCmisObjectId);
        }
        catch (IOException ioe)
        {
            throw new BinaryBodyException(ImageException.GENERAL_ERROR, ioe);
        }
        catch (DAOException e)
        {
            log.error(e.getMessage(), e);
            throw new BinaryBodyException(BinaryBodyException.TOO_LARGE, e.getMessage());
        }
    }


    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { BinaryBodyException.class })
    public void createCmisBinaryBody(String documentCmisObjectId)
        throws BinaryBodyException
    {
        // See if the contentType is valid.
        if (!binaryBodyManager.isValidType(getBean().getContentType()))
        {
            throw new BinaryBodyException(BinaryBodyException.BAD_CONTENT_TYPE);
        }

        try
        {
            getBinaryBodyDAO().create(getBean());
            this.cmisObjectId = documentCmisObjectId;
        }        
        catch (DAOException e)
        {
            log.error(e.getMessage(), e);
            throw new BinaryBodyException(BinaryBodyException.TOO_LARGE, e.getMessage());
        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { BinaryBodyException.class })
    protected void createBinaryBody(InputStream data)
        throws BinaryBodyException
    {
        // do nothing
    }

    /**
     * Saves the image to storage and updates the db
     * 
     * @param data the inputstream containing the document body data.
     * @throws BinaryBodyException if the body object is missing information
     * @throws IOException if an error occurs writing the body object to storage
     * @throws DAOException if a database error occurs
     */
    protected String insert(InputStream data,  String containerCmisObjectId, String documentCmisObjectId)
        throws DAOException,
            IOException,
            BinaryBodyException
    {
    	// CMIS Object does not exist, first upload to a specified container
    	if(documentCmisObjectId == null) {
    		return insert(data, new RemoteContainer(containerCmisObjectId, null));
    	}
    	
    	// Existing CMIS object, this is a binary update
    	insert(data, new RemoteDocument(documentCmisObjectId, null));
    	return documentCmisObjectId;
    }
    

    /**
     * Saves the image to storage and updates the db
     * 
     * @param data the inputstream containing the document body data.
     * @throws BinaryBodyException if the body object is missing information
     * @throws IOException if an error occurs writing the body object to storage
     * @throws DAOException if a database error occurs
     */
    protected String insert(InputStream data, RemoteContainer container)
        throws DAOException,
            IOException,
            BinaryBodyException
    {
        // stream data to a temp file
        File tempFile = writeToTempFile(data);
        BinaryBodyBean bean = getBean();
        String cmisID = null;

        try
        {

            if (tempFile.length() > binaryBodyManager.getMaxBodySize() * 1024)
            {
                if (!tempFile.delete())
                {
                    log.warn("Unable to delete temp file: " + tempFile.getCanonicalPath());
                }
                throw new BinaryBodyException(BinaryBodyException.TOO_LARGE);
            }

            // update to set the size in the db
            bean.setSize((int)tempFile.length());
            getBinaryBodyDAO().update(bean);

            FileInputStream fis = new FileInputStream(tempFile);
                        
            try
            {
                cmisID = getCmisStorageProvider().put(container, StorageUtil.getStorageKey(this), getTitle(), getContentType(), getSize(), fis);

                if (StringUtils.isNullOrEmpty(cmisID))
                {
                    throw new DAOException(String.format("Unable to store binary body '%s': storage provider returned "
                                                         + "false on the put operation", bean.getID()));
                }
            }
            finally
            {
                IOUtils.closeQuietly(fis);
            }

        }
        finally
        {
            if (tempFile.exists() && !tempFile.delete())
            {
                log.warn("Unable to delete temp file: " + tempFile.getCanonicalPath());
            }
        }

        return cmisID;
    }

    
    /**
     * Saves the image to storage and updates the db
     * 
     * @param data the inputstream containing the document body data.
     * @throws BinaryBodyException if the body object is missing information
     * @throws IOException if an error occurs writing the body object to storage
     * @throws DAOException if a database error occurs
     */
    protected void insert(InputStream data, RemoteDocument document)
        throws DAOException,
            IOException,
            BinaryBodyException
    {
        // stream data to a temp file
        File tempFile = writeToTempFile(data);
        BinaryBodyBean bean = getBean();
        String cmisID = null;

        try
        {

            if (tempFile.length() > binaryBodyManager.getMaxBodySize() * 1024)
            {
                if (!tempFile.delete())
                {
                    log.warn("Unable to delete temp file: " + tempFile.getCanonicalPath());
                }
                throw new BinaryBodyException(BinaryBodyException.TOO_LARGE);
            }

            // update to set the size in the db
            bean.setSize((int)tempFile.length());
            getBinaryBodyDAO().update(bean);

            FileInputStream fis = new FileInputStream(tempFile);
                        
            try
            {
            	cmisID = getCmisStorageProvider().put(document, StorageUtil.getStorageKey(this), getName(), getContentType(), getSize(), fis);

                if (StringUtils.isNullOrEmpty(cmisID))
                {
                    throw new DAOException(String.format("Unable to store binary body '%s': storage provider returned "
                                                         + "false on the put operation", bean.getID()));
                }
            }
            finally
            {
                IOUtils.closeQuietly(fis);
            }

        }
        finally
        {
            if (tempFile.exists() && !tempFile.delete())
            {
                log.warn("Unable to delete temp file: " + tempFile.getCanonicalPath());
            }
        }        
    }

    @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
    public InputStream getData()
        throws IOException
    {
        InputStream is = getCmisStorageProvider().getStream(CmisStorageUtil.getStorageKey(this));
        if (is == null)
        {
            log.error(String.format("Unable to retrieve data for BinaryBody %s", getBean().getID()));
            throw new IOException();
        }

        return is;
    }


    public String getCmisObjectId()
    {
        return cmisObjectId;
    }


    public CmisStorageProvider getCmisStorageProvider()
    {
        return cmisStorageProvider;
    }


    public void setCmisStorageProvider(CmisStorageProvider cmisStorageProvider)
    {
        this.cmisStorageProvider = cmisStorageProvider;
    }


	public String getTitle() {
		return title;
	}
}
