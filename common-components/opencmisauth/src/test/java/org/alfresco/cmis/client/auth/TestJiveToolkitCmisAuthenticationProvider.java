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

package org.alfresco.cmis.client.auth;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;


/**
 * This unit test exercises the <code>JiveToolkitCmisAuthenticationProvider</code>.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: TestJiveToolkitCmisAuthenticationProvider.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class TestJiveToolkitCmisAuthenticationProvider
{
    private final static String JIVETOOLKIT_CMIS_PATH_SUFFIX = "/jiveservice/cmis";

    
    private final Session     session;
    private final PrintWriter out;
    
    
    public TestJiveToolkitCmisAuthenticationProvider()
        throws Exception
    {
        // Remember: System.out is not natively Unicode capable - hence why we wrap it in a PrintWriter
        out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
        
        
        final Map<String, String> cmisSessionParameters = new HashMap<String, String>();
        
        // Poor man's emulation of Spring's nice override mechanism, but without requiring Spring
        final Properties  props = new Properties();
        final InputStream is1   = TestJiveToolkitCmisAuthenticationProvider.class.getResourceAsStream("/org/alfresco/cmis/client/auth/TestJiveToolkitCmisAuthenticationProvider.properties");
        final InputStream is2   = TestJiveToolkitCmisAuthenticationProvider.class.getResourceAsStream("/org/alfresco/cmis/client/auth/TestJiveToolkitCmisAuthenticationProvider-override.properties");
        
        if (is2 != null)
        {
            Properties original = new Properties();
            Properties override = new Properties();
            
            original.load(is1);
            override.load(is2);
            props.putAll(original);
            props.putAll(override);
        }
        else
        {
            props.load(is1);
        }
        
        String cmisServerURL = props.getProperty("jivetoolkit.alfresco.url").trim() + JIVETOOLKIT_CMIS_PATH_SUFFIX;
        
        // server coordinates
        cmisSessionParameters.put(SessionParameter.ATOMPUB_URL, cmisServerURL);
        
        // user credentials
        cmisSessionParameters.put(SessionParameter.USER,     props.getProperty("jivetoolkit.alfresco.userName"));
        cmisSessionParameters.put(SessionParameter.PASSWORD, props.getProperty("jivetoolkit.alfresco.password"));
        
        // binding type
        cmisSessionParameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        
        // set the object factory to the Alfresco extension - see http://code.google.com/a/apache-extras.org/p/alfresco-opencmis-extension/
        cmisSessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        
        // set the custom AuthenticationProvider - see http://incubator.apache.org/chemistry/opencmis-client-bindings.html
        cmisSessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, "org.alfresco.cmis.client.auth.JiveToolkitCmisAuthenticationProvider");
        
        // set the masquerade user id (same as whatever we login with HTTP Basic Auth above - this is adequate for unit testing)
        cmisSessionParameters.put(JiveToolkitCmisAuthenticationProvider.SESSION_KEY_USER_ID, props.getProperty("jivetoolkit.alfresco.userName"));
        
        // set the encryption password for the masqueraded user id
        cmisSessionParameters.put(JiveToolkitCmisAuthenticationProvider.SESSION_KEY_SHARED_PASSWORD, props.getProperty("jivetoolkit.shared.password"));

        long start = System.nanoTime();
        SessionFactory factory = SessionFactoryImpl.newInstance();
        long end1 = System.nanoTime();
        this.session = factory.getRepositories(cmisSessionParameters).get(0).createSession();   // Note: this involves an RPC to the CMIS server
        long end2 = System.nanoTime();
        
        println("Time to instantiate OpenCMIS Session Factory: " + String.valueOf((double)(end1 - start) / 1000 / 1000) + "ms.");
        println("Time to create OpenCMIS Session: " + String.valueOf((double)(end2 - end1) / 1000 / 1000) + "ms.");
    }
    
    
    @Test
    public void testListSubFoldersViaGetDescendants()
    {
        println("----------------------------------------");
        
        Folder                             rootFolder = session.getRootFolder();
        List<Tree<FileableCmisObject>>     children   = rootFolder.getDescendants(1);
        Iterator<Tree<FileableCmisObject>> iter       = children.iterator();
    
        while (iter.hasNext())
        {
            Tree<FileableCmisObject> tree = iter.next();
            FileableCmisObject       item = tree.getItem();
            
            println(item.getType().getDisplayName() + ": " + item.getName() + " (" + item.getId() + ")");
        }
    }
    

    @Test
    public void testListSubFoldersViaQuery()
    {
        println("----------------------------------------");
        
        Folder                    rootFolder = session.getRootFolder();
        ItemIterable<QueryResult> children   = session.query("SELECT * FROM cmis:folder WHERE IN_FOLDER('" + rootFolder.getId() + "')", false);
        
        Iterator<QueryResult> items = children.iterator();
        
        while (items.hasNext())
        {
            QueryResult item = items.next();
            
            println(item.getPropertyValueById("cmis:objectTypeId") + ": " + item.getPropertyValueById("cmis:name") + " (" + item.getPropertyValueById("cmis:id") + ")");
        }
    }
    
    
    @Test
    public void testListDocumentsViaGetChildren()
    {
        println("----------------------------------------");
        
        Folder                   rootFolder = session.getRootFolder();
        ItemIterable<CmisObject> children   = rootFolder.getChildren();
        Iterator<CmisObject>     iter       = children.iterator();
    
        while (iter.hasNext())
        {
            CmisObject item = iter.next();
            
            println(item.getType().getDisplayName() + ": " + item.getName() + " (" + item.getId() + ")");
        }
    }
    
    
    @Test
    public void testListDocumentsViaQuery()
    {
        println("----------------------------------------");
        
        Folder                    rootFolder = session.getRootFolder();
        ItemIterable<QueryResult> children   = session.query("SELECT * FROM cmis:document WHERE IN_FOLDER('" + rootFolder.getId() + "')", false);
        
        Iterator<QueryResult> items = children.iterator();
        
        while (items.hasNext())
        {
            QueryResult item = items.next();
            
            println(item.getPropertyValueById("cmis:objectTypeId") + ": " + item.getPropertyValueById("cmis:name") + " (" + item.getPropertyValueById("cmis:id") + ")");
        }
    }
    
    
    @Test
    public void testCreateSocializedDocument()
        throws Exception
    {
        println("----------------------------------------");
        
        Folder      rootFolder = session.getRootFolder();
        InputStream is         = null;
       
        try
        {
            final String     fileName     = "2011 Holiday Schedule " + (new Date()).getTime() + ".pdf";  // create a unique filename, to avoid conflicts with previous test runs
            final BigInteger fileSize     = BigInteger.valueOf(42808);                                   // This would normally be calculated from the file directly
            final String     fileMimeType = "application/pdf";                                           // This would normally be calculated from the file directly

            is = TestJiveToolkitCmisAuthenticationProvider.class.getResourceAsStream("/org/alfresco/cmis/client/auth/2011 Holiday Schedule.pdf");
              
            Map<String, Object> properties = new HashMap<String, Object>();
              
            properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID + ",P:jive:socialized"); // note: uses Alfresco extension to set jive:socialized aspect - see http://code.google.com/a/apache-extras.org/p/alfresco-opencmis-extension/
            properties.put(PropertyIds.NAME,           fileName);
             
            ContentStream cs     = new ContentStreamImpl(fileName, fileSize, fileMimeType, is);
            Document      doc    = rootFolder.createDocument(properties, cs, VersioningState.MAJOR);
            String        cmisId = doc.getId();
              
            println("cmis:id of newly created doc = " + cmisId);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }
    
    
    
    
    private final void println(final String message)
    {
        out.println(message);
        out.flush();
    }
    
    
}
