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

package org.alfresco.util.encryption.impl;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;
import static org.junit.Assert.*;

import org.alfresco.util.encryption.CannotDecryptException;
import org.alfresco.util.encryption.Encrypter;


/**
 * This unit test exercises the </code>AES256PasswordBasedEncrypter</code>.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: TestAES256PasswordBasedEncrypter.java 41626 2012-09-14 23:59:00Z wabson $
 * @see org.alfresco.util.encryption.impl.AES256PasswordBasedEncrypter
 *
 */
public class TestAES256PasswordBasedEncrypter
{
    @Test
    public void testSingleEncrypterInstance()
        throws Exception
    {
        char[]    password  = "opensesame".toCharArray();
        String    original  = "The quick brown fox jumps over the lazy dog.";
        
        Encrypter enc       = new AES256PasswordBasedEncrypter(password);
        String    encrypted = enc.encrypt(original);
        String    decrypted = enc.decrypt(encrypted);
        
        assertEquals(original, decrypted);
    }

    @Test
    public void testSeparateEncrypterInstances()
        throws Exception
    {
        char[]    password  = "opensesame".toCharArray();
        String    original  = "Some other message.";
        
        Encrypter enc1      = new AES256PasswordBasedEncrypter(password);
        Encrypter enc2      = new AES256PasswordBasedEncrypter(password);
        String    encrypted = enc1.encrypt(original);
        String    decrypted = enc2.decrypt(encrypted);
        
        assertEquals(original, decrypted);
    }
    
    @Test
    public void testHardcodedEncryptedText()
        throws Exception
    {
        char[]    password = "opensesame".toCharArray();
        Encrypter enc      = new AES256PasswordBasedEncrypter(password);
        
        String decrypted = enc.decrypt("NWhmZDA3OE53USsrWWtpdUtzQm5oUT09LGhPcTEyZ3RwZTR6TDFZRVRTaVFFV2svSmo2QkZEZmkzaFdmazNlMUhldlRPNzVjU1M3dmVzbzNUUEx6NHpBUko=");
            
        assertEquals("The quick brown fox jumps over the lazy dog.", decrypted);
    }
    
    @Test
    public void testUTF8Message()
        throws Exception
    {
        char[]    password  = "opensesame".toCharArray();
        String    original  = "\u79C1\u306F\u30AC\u30E9\u30B9\u3092\u98DF\u3079\u3089\u308C\u307E\u3059\u3002\u305D\u308C\u306F\u79C1\u3092\u50B7\u3064\u3051\u307E\u305B\u3093\u3002";
        
        Encrypter enc       = new AES256PasswordBasedEncrypter(password);
        String    encrypted = enc.encrypt(original);
        String    decrypted = enc.decrypt(encrypted);
        
        assertEquals(original, decrypted);
    }
    
    @Test
    public void testEmptyMessage()
        throws Exception
    {
        char[]    password  = "opensesame".toCharArray();
        String    original  = "";
        
        Encrypter enc       = new AES256PasswordBasedEncrypter(password);
        String    encrypted = enc.encrypt(original);
        String    decrypted = enc.decrypt(encrypted);
        
        assertEquals(original, decrypted);
    }
    
    @Test
    public void testEmptyPassword()
        throws Exception
    {
        char[]    password  = "".toCharArray();
        String    original  = "The quick brown fox jumps over the lazy dog.";
        
        try
        {
            Encrypter enc       = new AES256PasswordBasedEncrypter(password);
            String    encrypted = enc.encrypt(original);
            String    decrypted = enc.decrypt(encrypted);
            
            assertEquals(original, decrypted);
            
            // If we got this far, something went very very wrong
            fail("Decryption apparently succeeded, even though the password was blank.");
        }
        catch (final InvalidKeySpecException ikse)  // Thrown if assertions are not enabled
        {
            // Success!
        }
        catch(final AssertionError ae)  // Thrown if assertions are enabled (eg. during Maven test)
        {
            // Success!
        }
    }
    
    @Test
    public void testMismatchedPasswords()
        throws Exception
    {
        char[]    password1  = "opensesame".toCharArray();
        char[]    password2  = "notopensesame".toCharArray();
        String    original   = "The quick brown fox jumps over the lazy dog.";

        try
        {
            Encrypter enc1      = new AES256PasswordBasedEncrypter(password1);
            Encrypter enc2      = new AES256PasswordBasedEncrypter(password2);
            String    encrypted = enc1.encrypt(original);
            String    decrypted = enc2.decrypt(encrypted);
            
            assertEquals(original, decrypted);
            
            // If we got this far, something went very very wrong
            fail("Decryption apparently succeeded, even though the passwords were different.");
        }
        catch (final CannotDecryptException cde)
        {
            // Success!
        }
    }
    
    @Test
    public void testMismatchedPasswordsOfSameLength()
        throws Exception
    {
        char[]    password1  = "opensesame".toCharArray();
        char[]    password2  = "emasesnepo".toCharArray();
        String    original   = "The quick brown fox jumps over the lazy dog.";

        try
        {
            Encrypter enc1      = new AES256PasswordBasedEncrypter(password1);
            Encrypter enc2      = new AES256PasswordBasedEncrypter(password2);
            String    encrypted = enc1.encrypt(original);
            String    decrypted = enc2.decrypt(encrypted);
            
            assertEquals(original, decrypted);
            
            // If we got this far, something went very very wrong
            fail("Decryption apparently succeeded, even though the passwords were different.");
        }
        catch (final CannotDecryptException cde)
        {
            // Success!
        }
    }
    
    @Test
    public void testEmptyEncryptedText()
        throws Exception
    {
        char[]    password = "opensesame".toCharArray();
        Encrypter enc      = new AES256PasswordBasedEncrypter(password);
        
        try
        {
            String decrypted = enc.decrypt("");
            
            fail("Decryption should have failed.");
        }
        catch (final CannotDecryptException cde)
        {
            // Success!
        }
    }
    
    @Test
    public void testSyntacticallyIncorrectEncryptedTest()
        throws Exception
    {
        char[]    password = "opensesame".toCharArray();
        Encrypter enc      = new AES256PasswordBasedEncrypter(password);
        
        try
        {
            String decrypted = enc.decrypt("abcd1234");
            
            fail("Decryption should have failed.");
        }
        catch (final CannotDecryptException cde)
        {
            // Success!
        }
    }
    
    @Test
    public void testWronglySeparatedEncryptedText()
        throws Exception
    {
        char[]    password = "opensesame".toCharArray();
        Encrypter enc      = new AES256PasswordBasedEncrypter(password);
        
        try
        {
            String decrypted = enc.decrypt("y1hlDQgx2P75ksdxwcTaYQ==-jJqhtPCK7NhbpDehXPKqDVOQB3FLVYmLb2C5b8KJIzkuYIaXnroLXCq77I9W4Dd8");  // Note: wrong separator character (- instead of ,)
            
            fail("Decryption should have failed.");
        }
        catch (final CannotDecryptException cde)
        {
            // Success!
        }
    }
  
    
    /**
     * This method is not a unit test - rather it provides a convenient way to generate encrypted text for use elsewhere.
     * 
     * To enable it, uncomment the @Test annotation.
     */
//    @Test
    public void notATest_outputEncryptedText()
        throws Exception
    {
        char[]    password  = "CHANGEME!".toCharArray();
        String    original  = "admin";
        
        Encrypter enc       = new AES256PasswordBasedEncrypter(password);
        String    encrypted = enc.encrypt(original);
        
        // Remember: System.out is not natively Unicode capable - hence why we wrap it in a PrintWriter
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
        out.println(encrypted);
        out.flush();
        out.close();
    }

    
}
