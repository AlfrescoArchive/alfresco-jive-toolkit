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

package org.alfresco.jive;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import org.alfresco.jive.impl.JiveOpenClientImpl;
import org.alfresco.util.encryption.Encrypter;
import org.alfresco.util.encryption.impl.AES256PasswordBasedEncrypter;


/**
 * This unit test exercises the <code>JiveOpenClientImpl</code>.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: TestJiveOpenClient.java 41626 2012-09-14 23:59:00Z wabson $
 * @see org.alfresco.jive.JiveOpenClient
 * @see org.alfresco.jive.impl.JiveOpenClientImpl
 *
 */
public class TestJiveOpenClient
{
    private final Encrypter encrypter;
    private final String    jiveUrl;
    private final String    jiveUserName;
    private final char[]    jivePassword;
    private final int       jiveTimeout;
    
    private final long   testSpaceId;
    private final String testUserId;
    

    public TestJiveOpenClient()
        throws Exception
    {
        // Poor man's emulation of Spring's nice override mechanism, but without requiring Spring
        final Properties  props = new Properties();
        final InputStream is1   = TestJiveOpenClient.class.getResourceAsStream("/org/alfresco/jive/TestJiveOpenClient.properties");
        final InputStream is2   = TestJiveOpenClient.class.getResourceAsStream("/org/alfresco/jive/TestJiveOpenClient-override.properties");
        
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
        
        encrypter    = new AES256PasswordBasedEncrypter(props.getProperty("jivetoolkit.shared.password").toCharArray());
        jiveUrl      = props.getProperty("jivetoolkit.jive.url");
        jiveUserName = props.getProperty("jivetoolkit.jive.user");
        jivePassword = props.getProperty("jivetoolkit.jive.password").toCharArray();
        jiveTimeout  = Integer.valueOf(props.getProperty("jivetoolkit.jive.timeoutInMs"));
        
        testSpaceId = Long.valueOf(props.getProperty("TestJiveOpenClient.test.spaceid"));
        testUserId  = props.getProperty("TestJiveOpenClient.test.userid");
    }
    
    
    @Test
    public void testGetSpacesForTestUser()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        assertNotNull(joc.getSpaces(testUserId));
    }
    
    
    @Test
    public void testGetSubSpacesForTestUser()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        assertNotNull(joc.getSubSpaces(testUserId, testSpaceId)); 
    }
    
    
    @Test
    public void testGetSubSpacesForTestUserWithInvalidSpaceId()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        try
        {
            assertNotNull(joc.getSubSpaces(testUserId, -1));
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final SpaceNotFoundException snfe)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testInvalidHttpBasicAuthUserName()
        throws Exception
    {
        JiveOpenClientImpl joc = getDefaultJiveOpenClientPreInit();
        joc.setUserName("invalidUserName");
        joc.init();
        
        try
        {
            joc.getSpaces(testUserId);
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testInvalidHttpBasicAuthPassword()
        throws Exception
    {
        JiveOpenClientImpl joc = getDefaultJiveOpenClientPreInit();
        joc.setPassword("invalidpassword".toCharArray());
        joc.init();
        
        try
        {
            joc.getSpaces(testUserId);
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testNullMasqueradedUserId()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        try
        {
            assertNotNull(joc.getSpaces(null));
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testEmptyMasqueradedUserId()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        try
        {
            assertNotNull(joc.getSpaces(""));
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testBlankMasqueradedUserId()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        try
        {
            assertNotNull(joc.getSpaces("         "));
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    

    @Test
    public void testInvalidMasqueradedUserId()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        try
        {
            assertNotNull(joc.getSpaces("This is an invalid user id in Jive!  :-)"));
            
            fail("Call succeeded when it should have failed.");
        }
        catch (final AuthenticationException ae)
        {
            // Success!
        }
    }
    
    
    @Test
    public void testCreateDocument()
        throws Exception
    {
        JiveOpenClient joc       = getDefaultJiveOpenClient();
        long           timestamp = (new Date()).getTime();

        String cmisId    = "####fake cmis id from TestJiveOpenClient JUnit test " + timestamp + "####";
        joc.createDocument(testUserId, testSpaceId, cmisId, "test " + timestamp + ".pdf", 0, "application/pdf");
    }
    

    @Test
    public void testUpdateDocumentRename()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        // Create document first
        long   timestamp = (new Date()).getTime();
        String cmisId    = "####fake cmis id from TestJiveOpenClient JUnit test " + timestamp + "####";
        joc.createDocument(testUserId, testSpaceId, cmisId, "test " + timestamp + ".pdf", 0, "application/pdf");
        
        // Now update it
        joc.updateDocument(testUserId, cmisId, "renamed test " + timestamp + ".pdf", 0, "application/pdf");
    }
    
    
    @Test
    public void testUpdateDocumentChangeMimeType()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        // Create document first
        long   timestamp = (new Date()).getTime();
        String cmisId    = "####fake cmis id from TestJiveOpenClient JUnit test " + timestamp + "####";
        joc.createDocument(testUserId, testSpaceId, cmisId, "test " + timestamp + ".pdf", 0, "application/pdf");
        
        // Now update it
        joc.updateDocument(testUserId, cmisId, "test " + timestamp + ".docx", 0, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
    
    
    @Test
    public void testUpdateDocumentChangeSize()
        throws Exception
    {
        JiveOpenClient joc = getDefaultJiveOpenClient();
        
        // Create document first
        long   timestamp = (new Date()).getTime();
        String cmisId    = "####fake cmis id from TestJiveOpenClient JUnit test " + timestamp + "####";
        joc.createDocument(testUserId, testSpaceId, cmisId, "test " + timestamp + ".pdf", 0, "application/pdf");
        
        // Now update it
        joc.updateDocument(testUserId, cmisId, "test " + timestamp + ".pdf", 1024, "application/pdf");
    }
    
    
    private JiveOpenClientImpl getDefaultJiveOpenClient()
        throws MalformedURLException
    {
        JiveOpenClientImpl result = getDefaultJiveOpenClientPreInit();
        result.init();
        return(result);
    }
    
    private JiveOpenClientImpl getDefaultJiveOpenClientPreInit()
        throws MalformedURLException
    {
        JiveOpenClientImpl result = new JiveOpenClientImpl();
        
        result.setEncrypter(encrypter);
        result.setJiveUrl(jiveUrl);
        result.setUserName(jiveUserName);
        result.setPassword(jivePassword);
        result.setTimeoutInMs(jiveTimeout);
        
        return(result);
    }

}
