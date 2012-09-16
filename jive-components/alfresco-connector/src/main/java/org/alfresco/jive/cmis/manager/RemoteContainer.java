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
 * @version $Id: RemoteContainer.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class RemoteContainer
    implements ObjectId
{

    private String  id;
    private String  name;

    private boolean isFile = false;


    public RemoteContainer(String id, String name)
    {
        super();
        this.id = id;
        this.name = name;
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
        return "RemoteContainer: { id: " + id + ", name: " + name + "}";
    }


    public boolean isFile()
    {
        return isFile;
    }


    public void setFile(boolean isFile)
    {
        this.isFile = isFile;
    }
}
