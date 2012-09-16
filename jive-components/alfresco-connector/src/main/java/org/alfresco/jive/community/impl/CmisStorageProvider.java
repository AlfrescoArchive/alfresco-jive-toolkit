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


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.cmis.manager.RemoteContainer;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.eos.StorageProvider;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: CmisStorageProvider.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CmisStorageProvider
    implements StorageProvider
{

	private static final Logger log = LogManager.getLogger(CmisStorageProvider.class);
	
    private AlfrescoNavigationManager alfrescoNavigationManager;


    @Override
    public boolean containsKey(String arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public boolean delete(String arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public ByteBuffer getBuffer(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Iterable<String> getKeys()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getNamespace()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Map<String, String> getStatistics()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public InputStream getStream(String key) throws CmisConstraintException
    {
        InputStream is = null;

        try
        {
            is = alfrescoNavigationManager.getDocumentContent(key);
        }
        catch (CmisConnectionException e)
        {          
        	log.error("Error reading document content");
            e.printStackTrace();
        }
        catch (CmisConstraintException e)
        {          
        	log.error("Error reading document content");
            e.printStackTrace();
        }
        return is;
    }

    @Override
    public boolean put(String arg0, byte[] arg1)
    {
        // TODO Auto-generated method stub
        return false;
    }


    public String put(RemoteContainer container, String key, String fileName, String contentType, long size, InputStream is)
        throws IOException
    {

        String cmisID = null;
        cmisID = alfrescoNavigationManager.createDocument(container, fileName, contentType, size, is);

        return cmisID;
    }

    public String put(RemoteDocument remoteDocument, String key, String fileName, String contentType, long size, InputStream is)
    throws IOException
    {   
    	String cmisID = null;
        cmisID = alfrescoNavigationManager.updateDocument(remoteDocument, fileName, contentType, size, is);
    	return cmisID;
    }
    

    @Override
    public boolean put(String arg0, InputStream arg1)
    {
        // TODO Auto-generated method stub
        return false;
    }


    public void setAlfrescoNavigationManager(AlfrescoNavigationManager alfrescoNavigationManager)
    {
        this.alfrescoNavigationManager = alfrescoNavigationManager;
    }
}
