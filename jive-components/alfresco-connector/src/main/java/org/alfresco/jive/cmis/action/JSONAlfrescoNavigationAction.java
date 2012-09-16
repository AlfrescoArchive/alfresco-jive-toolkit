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

package org.alfresco.jive.cmis.action;


import java.util.ArrayList;
import java.util.List;

import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.cmis.manager.RemoteContainer;

import com.jivesoftware.community.action.JiveActionSupport;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: JSONAlfrescoNavigationAction.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class JSONAlfrescoNavigationAction
    extends JiveActionSupport
{
    private static final long         serialVersionUID = 6449935236093427895L;

    // /////////////////////
    // AUTOWIRED MEMBERS //
    // /////////////////////
    private AlfrescoNavigationManager alfrescoNavigationManager;

    // /////////
    // INPUT //
    // /////////

    // The content (file/directory) the user clicked on
    private String                    remoteContainerId;

    // Index of first record to start with (pagination)
    private Long                      firstRecord;

    private String                    searchString;

    private boolean                   fetchDirectories;
    private boolean                   fetchFiles;

    // //////////
    // OUTPUT //
    // //////////

    private long                      count;

    private List<RemoteContainer>     directories      = new ArrayList<RemoteContainer>();
    private List<RemoteContainer>     files            = new ArrayList<RemoteContainer>();


    // //////////////
    // PROCESSING //
    // //////////////

    public String execute()
    {
        log.info("Entering JSONAlfrescoNavigationAction.execute()...");

        if ("null".equals(remoteContainerId))
        {
            remoteContainerId = null;
        }

        if (fetchDirectories && !fetchFiles)
        {
            if (searchString == null)
            {
                log.info("Fetching directory listing for " + remoteContainerId);
                count = alfrescoNavigationManager.fillDirectories(directories, remoteContainerId, firstRecord);
            }
            else
            {
                log.info("Searching directories for " + searchString);
                count = alfrescoNavigationManager.searchDirectories(directories, searchString, firstRecord);
            }
        }

        if (fetchFiles)
        {
            if (searchString == null)
            {
                count = alfrescoNavigationManager.getFiles(remoteContainerId, firstRecord, directories, files);
            }
            else
            {
                count = alfrescoNavigationManager.searchFiles(searchString, firstRecord, directories, files);
            }
        }

        log.info("Exiting JSONAlfrescoNavigationAction.execute()");
        return SUCCESS;
    }


    // /////////////////////
    // AUTOWIRED SETTERS //
    // /////////////////////

    public void setAlfrescoNavigationManager(AlfrescoNavigationManager alfrescoNavigationManager)
    {
        this.alfrescoNavigationManager = alfrescoNavigationManager;
    }


    // /////////////////////
    // GETTERS / SETTERS //
    // /////////////////////

    public String getRemoteContainerId()
    {
        return remoteContainerId;
    }


    public void setRemoteContainerId(String remoteContainerId)
    {
        this.remoteContainerId = remoteContainerId;
    }


    public List<RemoteContainer> getDirectories()
    {
        return directories;
    }


    public void setDirectories(List<RemoteContainer> directories)
    {
        this.directories = directories;
    }


    public Long getFirstRecord()
    {
        return firstRecord;
    }


    public void setFirstRecord(Long firstRecord)
    {
        this.firstRecord = firstRecord;
    }


    public String getSearchString()
    {
        return searchString;
    }


    public void setSearchString(String searchString)
    {
        this.searchString = searchString;
    }


    public boolean isFetchDirectories()
    {
        return fetchDirectories;
    }


    public void setFetchDirectories(boolean fetchDirectories)
    {
        this.fetchDirectories = fetchDirectories;
    }


    public boolean isFetchFiles()
    {
        return fetchFiles;
    }


    public void setFetchFiles(boolean fetchFiles)
    {
        this.fetchFiles = fetchFiles;
    }


    public List<RemoteContainer> getFiles()
    {
        return files;
    }


    public void setFiles(List<RemoteContainer> files)
    {
        this.files = files;
    }


    public long getCount()
    {
        return count;
    }


    public void setCount(long count)
    {
        this.count = count;
    }
}
