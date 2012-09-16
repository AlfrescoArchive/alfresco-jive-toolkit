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


import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.cmis.client.auth.JiveToolkitCmisAuthenticationProvider;
import org.alfresco.jive.cmis.manager.RemoteContainer;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.alfresco.jive.community.impl.ConnectorConstants;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.aaa.SystemUser;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * TODO: Alfresco calls taking 1.5 seconds on average. Doing a count on the data
 * also takes 1.5 seconds, so calls are taking like 3 seconds. We can pull a
 * count in the same query perhaps to decrease amount of calls being made,
 * increasing performance.
 * 
 * @author corey.ferguson
 */
public class AlfrescoNavigationDAOImpl extends JiveJdbcDaoSupport
    implements AlfrescoNavigationDAO
{
    Logger                            log                  = Logger.getLogger(AlfrescoNavigationDAOImpl.class);
    
    private final SessionFactory      sessionFactory;

    private UserManager userManager;
    
    protected Cache<Long,Session> 	  userSessionCache;
    
    public static final int           PAGINATE_MAX_ITEMS   = 10;
    private OperationContext          paginateContext;

    private static final String       QUERY_SUBFOLDERS     = "select f.cmis:name as name, " + "f.cmis:objectId as id "
                                                             + "from cmis:folder as f " + "where in_folder('?')";

    private static final String       QUERY_SEARCH_FOLDERS = "select f.cmis:name as name, " + "f.cmis:objectId as id "
                                                             + "from cmis:folder as f " + "where name like '%?%'";

    
    private static final String       QUERY_SEARCH_FILES   = "select d.cmis:objectId as id, " + "d.cmis:name as name "
                                                             + "from cmis:document d " + "where name like '%?%' ";
    /*
    private static final String       QUERY_JIVE_ID        = "select dv.internaldocid from jivedocumentprop dp, jivedocversion dv" +
    														" where dp.name = 'cmisObject' and dp.propvalue = ? and" 
    								 						+ " dp.internaldocid=dv.internaldocid and dp.versionid=dv.versionid and dv.state='published'";
      */  
    public AlfrescoNavigationDAOImpl()
    {
        sessionFactory = SessionFactoryImpl.newInstance();
    }
      
    @Override
    public Session getSession()
    {
    	Session session = null;
    	long userID = getAuthentication().getUserID();
    	session = userSessionCache.get(userID);
    	User user = null;
		
    	try {
			user = userManager.getUser(userID);
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if(session == null) {
    		session = createUserSession(user);
    		userSessionCache.put(userID, session);
    	}
    	
    	return session;
    }
  
    
    protected Session createUserSession(User user) throws CmisConnectionException
    {
        long startTime = System.currentTimeMillis();

        HashMap<String, String> parameters = new HashMap<String, String>();
        
        String sessionUser = JiveGlobals.getJiveProperty(ConnectorConstants.ALFRESCO_USER);
        String sessionPassword = JiveGlobals.getJiveProperty(ConnectorConstants.ALFRESCO_PASSWORD);
        // User credentials.
        if(sessionUser == null || sessionPassword == null) {
        	throw new CmisConnectionException("Connection failed. " + ConnectorConstants.ALFRESCO_USER + " or " +
        			ConnectorConstants.ALFRESCO_PASSWORD + " are not set.");
        }
        parameters.put(SessionParameter.USER, sessionUser);
        parameters.put(SessionParameter.PASSWORD, sessionPassword);

        // Connection settings. "http://localhost:8080/alfresco/service/cmis"
        String cmsBaseURL = JiveGlobals.getJiveProperty(ConnectorConstants.ALFRESCO_URL);
        if(cmsBaseURL == null) {
        	throw new CmisConnectionException("Connection failed. " + ConnectorConstants.ALFRESCO_URL + " is not set.");
        }
        parameters.put(SessionParameter.ATOMPUB_URL, cmsBaseURL + ConnectorConstants.ALFRESCO_ENDPOINT); // URL
        
     // parameters.put(SessionParameter.ATOMPUB_URL,
        // "http://localhost:8090/alfresco/service/cmis");
        // parameter.put(SessionParameter.REPOSITORY_ID, "myRepository"); //
        // Only necessary if there is more than one repository.
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        
     // set the custom AuthenticationProvider - see http://incubator.apache.org/chemistry/opencmis-client-bindings.html
        parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, "org.alfresco.cmis.client.auth.JiveToolkitCmisAuthenticationProvider");
        
     // set the masquerade user id (same as whatever we login with HTTP Basic Auth above - this is adequate for unit testing)
        parameters.put(JiveToolkitCmisAuthenticationProvider.SESSION_KEY_USER_ID, getMasqueradedUser(user));
        
        
        String sharedPassword = JiveGlobals.getJiveProperty(ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD);
        if(sharedPassword == null) {
        	throw new CmisConnectionException("Connection failed. " + ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD + " is not set.");
        }
        // set the encryption password for the masqueraded user id
        parameters.put(JiveToolkitCmisAuthenticationProvider.SESSION_KEY_SHARED_PASSWORD, sharedPassword);
        
     // set the object factory to the Alfresco extension - see http://code.google.com/a/apache-extras.org/p/alfresco-opencmis-extension/
        parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        
        
        // Session locale.
        parameters.put(SessionParameter.LOCALE_ISO3166_COUNTRY, "");
        parameters.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "en");
        parameters.put(SessionParameter.LOCALE_VARIANT, "US");

        // This supposes only one repository is available at the URL.
        Session session;
		try {
			Repository soleRepository = sessionFactory.getRepositories(parameters).get(0);

			log.info("Opening session to Alfresco repository for .... " + soleRepository.getName());
			
			session = soleRepository.createSession();
			log.info("Session opened .... ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Session opened .... " + e.getLocalizedMessage());
			throw new CmisConnectionException("Connection failed", e);
		}

        // Default Pagination
        paginateContext = new OperationContextImpl();
        paginateContext.setMaxItemsPerPage(PAGINATE_MAX_ITEMS);
        paginateContext.setOrderBy("cmis:baseTypeId ASC");

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        log.info("Creating connection to Alfresco repository took " + processingTime + " milliseconds.");
        
        return session;
    }
    
    private String getMasqueradedUser(User user) {
    	
    	if(SystemUser.SYSTEM_USER_ID == user.getID()) {
    		return JiveGlobals.getJiveProperty(ConnectorConstants.ALFRESCO_USER, "admin");
    	}
    	
    	return user.getUsername();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getDirectories(RemoteContainer remoteContainer, Long firstRecord)
    {
        List<Object> returnResults = new ArrayList<Object>();

        Session session = getSession();
        
        long startTime = System.currentTimeMillis();

        if (firstRecord == null)
        {
            firstRecord = 0L;
        }

        // Get parent folder
        Folder parentFolder;
        if (remoteContainer == null || remoteContainer.getId() == null)
        {
            parentFolder = session.getRootFolder();
        }
        else
        {
            parentFolder = (Folder)session.getObject(remoteContainer);
        }

        log.info("Getting list of directories for parent folder " + parentFolder.getName());

        String query = QUERY_SUBFOLDERS.replaceFirst("\\?", parentFolder.getId());

        ItemIterable<QueryResult> results = session.query(query.toString(), false, paginateContext);
        ItemIterable<QueryResult> paginatedResults = results.skipTo(firstRecord).getPage();

        List<RemoteContainer> remoteContainers = new ArrayList<RemoteContainer>();
        for (QueryResult result : paginatedResults)
        {
            RemoteContainer folder = new RemoteContainer((String)result.getPropertyValueById("cmis:objectId"), (String)result.getPropertyValueById("cmis:name"));

            remoteContainers.add(folder);
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        log.info("Getting list of directories took " + processingTime + " milliseconds.");

        returnResults.add(results.getTotalNumItems());
        returnResults.add(remoteContainers);

        return returnResults;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> searchDirectories(String searchString, Long firstRecord)
    {
        List<Object> returnedResults = new ArrayList<Object>();

        Session session = getSession();
        
        long startTime = System.currentTimeMillis();
        log.info("Searching for directories for the given searchString: " + searchString);

        if (firstRecord == null)
        {
            firstRecord = 0L;
        }

        String query = QUERY_SEARCH_FOLDERS.replaceFirst("\\?", searchString);

        ItemIterable<QueryResult> results = session.query(query, false, paginateContext);
        ItemIterable<QueryResult> paginatedResults = results.skipTo(firstRecord).getPage();

        List<RemoteContainer> remoteContainers = new ArrayList<RemoteContainer>();
        for (QueryResult result : paginatedResults)
        {
            RemoteContainer folder = new RemoteContainer((String)result.getPropertyValueById("cmis:objectId"), (String)result.getPropertyValueById("cmis:name"));

            remoteContainers.add(folder);

            log.info("Found folder: " + folder);
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        log.info("Searching for directories took " + processingTime + " milliseconds.");

        returnedResults.add(results.getTotalNumItems());
        returnedResults.add(remoteContainers);

        return returnedResults;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getFiles(RemoteContainer remoteContainer, Long firstRecord)
    {

        List<Object> returnedResults = new ArrayList<Object>();
        Session session = getSession();
        
        long startTime = System.currentTimeMillis();

        if (firstRecord == null)
        {
            firstRecord = 0L;
        }

        // Get parent folder
        Folder parentFolder;
        if (remoteContainer == null || remoteContainer.getId() == null)
        {
            parentFolder = session.getRootFolder();
        }
        else
        {
            parentFolder = (Folder)session.getObject(remoteContainer);
        }

        ItemIterable<CmisObject> results = parentFolder.getChildren(paginateContext);
        ItemIterable<CmisObject> paginatedResults = results.skipTo(firstRecord).getPage();

        List<RemoteContainer> remoteContainers = new ArrayList<RemoteContainer>();
        for (CmisObject child : paginatedResults)
        {
            RemoteContainer container = new RemoteContainer(child.getId(), child.getName());

            if ("cmis:folder".equals(child.getPropertyValue("cmis:baseTypeId")))
            {
                container.setFile(false);
                remoteContainers.add(container);
            }
            else if ("cmis:document".equals(child.getPropertyValue("cmis:baseTypeId")))
            {
                container.setFile(true);
                remoteContainers.add(container);
            }
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        log.info("Getting list of directories took " + processingTime + " milliseconds.");

        returnedResults.add(results.getTotalNumItems());
        returnedResults.add(remoteContainers);

        return returnedResults;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> searchFiles(String searchString, Long firstRecord)
    {
        List<Object> returnedResults = new ArrayList<Object>();
        Session session = getSession();

        long startTime = System.currentTimeMillis();
        log.info("Searching for files for the given searchString: " + searchString);

        if (firstRecord == null)
        {
            firstRecord = 0L;
        }

        String query = QUERY_SEARCH_FILES.replaceFirst("\\?", searchString);

        ItemIterable<QueryResult> results = session.query(query, false, paginateContext);
        ItemIterable<QueryResult> paginatedResults = results.skipTo(firstRecord).getPage();

        List<RemoteContainer> remoteContainers = new ArrayList<RemoteContainer>();
        for (QueryResult result : paginatedResults)
        {
            RemoteContainer file = new RemoteContainer((String)result.getPropertyValueById("cmis:objectId"), (String)result.getPropertyValueById("cmis:name"));

            file.setFile(true);
            remoteContainers.add(file);
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        log.info("Searching for directories took " + processingTime + " milliseconds.");

        returnedResults.add(results.getTotalNumItems());
        returnedResults.add(remoteContainers);

        return returnedResults;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Document createDocument(RemoteContainer container, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException
    {

        Document document = null;
        Session session = getSession();
        try
        {

            Folder folder = (Folder)session.getObject(container);

            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.OBJECT_TYPE_ID,
            ObjectType.DOCUMENT_BASETYPE_ID + ",P:jive:socialized");
            //properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

            properties.put(PropertyIds.NAME, fileName);

            // Content
            ContentStream cs = new ContentStreamImpl(fileName, BigInteger.valueOf(size), contentType, data);

            document = folder.createDocument(properties, cs, VersioningState.MAJOR);
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (IOException ex)
            {
                // Do nothing
            }
        }
        return document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document updateDocument(RemoteDocument doc, String fileName, String contentType, long size, InputStream data)
        throws CmisConstraintException
    {
        Document document = null;
        Session session = getSession();
        
        try
        {
            document = (Document)session.getObject(doc);

            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            
            AlfrescoDocument alfDoc = (AlfrescoDocument)document;
            
            if(!alfDoc.hasAspect("P:jive:socialized")) {
            	alfDoc.addAspect("P:jive:socialized");  
            }
            
            properties.put(PropertyIds.NAME, fileName);
            document.updateProperties(properties);
            // Content
            ContentStream cs = new ContentStreamImpl(fileName, BigInteger.valueOf(size), contentType, data);
            document.setContentStream(cs, true);            
            
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (IOException ex)
            {
                // Do nothing
            }
        }
        return document;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Document updateDocument(RemoteDocument doc, String fileName)
        throws CmisConstraintException
    {
        Document document = null;
        Session session = getSession();
        try
        {
            document = (Document)session.getObject(doc);

            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            AlfrescoDocument alfDoc = (AlfrescoDocument)document;
            
            if(!alfDoc.hasAspect("P:jive:socialized")) {
            	alfDoc.addAspect("P:jive:socialized");  
            }

            properties.put(PropertyIds.NAME, fileName);
            document.updateProperties(properties);
        }
        finally
        {            
                // Do nothing            
        }
        return document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document deleteDocument(RemoteDocument doc) throws CmisConstraintException
    {
        Document document = null;
        Session session = getSession();
        try
        {
            document = (Document)session.getObject(doc);
            AlfrescoDocument alfDoc = (AlfrescoDocument) document; 
                        
            if(alfDoc.hasAspect("P:jive:socialized")) {
            	log.info("P:jive:socialized aspect found \n Removing ...");
            	  alfDoc.removeAspect("P:jive:socialized");
           	} else {
           		log.info("No P:jive:socialized aspect found");
           	}           
            
        }
        finally
        {            
                // Do nothing            
        }
        return document;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getDocumentContent(String docId) throws CmisConnectionException, CmisConstraintException
    {

    	Session session = getSession();
        CmisObject object = session.getObject(session.createObjectId(docId));
        
        Document document = (Document)object;
        
        InputStream stream = document.getContentStream().getStream();

        return stream;
    }

    @Override
    public RemoteDocument getDocumentMetadata(String docId) throws CmisConnectionException, CmisConstraintException
    {
    	RemoteDocument remoteDocument = null;

    	Session session = getSession();
    	
        CmisObject object = session.getObject(session.createObjectId(docId));
        Document document = (Document)object;
        String filename = document.getContentStreamFileName();        
        String mimeType = document.getContentStreamMimeType();
        long size = document.getContentStreamLength();
        remoteDocument = new RemoteDocument(docId, filename, filename, mimeType, intValue(size));
        return remoteDocument;
    }
    
    
    private int intValue(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(" Cannot cast long " + l + "to int.");
        }
        return (int) l;
    }

    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteContainer getRemoteObject(String remoteObjectId) {
		Session session = getSession();
		CmisObject cmisObject = session.getObject(remoteObjectId);
		
		RemoteContainer remoteObject = new RemoteContainer(cmisObject.getId(), cmisObject.getName());
		if ("cmis:folder".equals(cmisObject.getPropertyValue("cmis:baseTypeId"))) {
			remoteObject.setFile(false);
		}
		else if ("cmis:document".equals(cmisObject.getPropertyValue("cmis:baseTypeId"))) {
			remoteObject.setFile(true);
		}
		
		// TODO Auto-generated method stub
		return remoteObject;
	}


	public static AuthToken getAuthToken() {
        return JiveApplication.getEffectiveContext().getAuthenticationProvider().getAuthToken();
    }

	public static JiveAuthentication getAuthentication() {
        return JiveApplication.getEffectiveContext().getAuthenticationProvider().getAuthentication();
    }
	
	/**
	 * @param usersRepCache the usersRepCache to set
	 */
	public void setUserSessionCache(
			Cache<Long, Session> userSessionCache) {
		this.userSessionCache = userSessionCache;
	}

	public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
	
}
