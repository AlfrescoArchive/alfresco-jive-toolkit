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


import org.apache.chemistry.opencmis.client.api.ObjectId;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: RemoteDocument.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class RemoteDocument
    implements ObjectId
{

    private String  id;
    private String  name;

    private String fileName; 
    private String mimeType;
    
    private int size;
    private boolean isFile = true;


    public RemoteDocument(String id, String name)
    {
        super();
        this.id = id;
        this.name = name;
    }

    public RemoteDocument(String id, String name, String fileName, String mimeType, int size)
    {
        super();
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.setSize(size);
    }

    @Override
    public String getId()
    {
        return id;
    }


    public void setId(String id)
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String toString()
    {
        return "RemoteDocument: { id: " + id + ", name: " + name + "}";
    }


    public boolean isFile()
    {
        return isFile;
    }


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getMimeType() {
		return mimeType;
	}


	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
   
}
