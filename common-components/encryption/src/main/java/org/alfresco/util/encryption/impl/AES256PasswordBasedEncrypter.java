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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.util.encryption.CannotDecryptException;
import org.alfresco.util.encryption.Encrypter;



/**
 * This class provides an <code>Encrypter</code> implementation that uses AES 256-bit encryption in combination with a password
 * to encrypt / decrypt source text. 
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: AES256PasswordBasedEncrypter.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class AES256PasswordBasedEncrypter
    implements Encrypter
{
    private final static Log log = LogFactory.getLog(AES256PasswordBasedEncrypter.class);
    
    private final static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private final static String PASSWORD_ALGORITHM    = "PBKDF2WithHmacSHA1";
    private final static String SECRET_KEY_ALGORITHM  = "AES";
    private final static String CHARACTER_ENCODING    = "UTF-8";
    
    private final static int    NUM_ITERATIONS = 1024;
    private final static int    KEY_LENGTH     = 256;
    private final static String SEPARATOR      = ",";
    
    private final SecretKey secretKey;
    
    
    /**
     * Constructor for the class.
     * 
     * @param password The password to use when encrypting data <i>(must not be null, empty or blank)</i>.
     */
    public AES256PasswordBasedEncrypter(final char[] password)
        throws NoSuchAlgorithmException,
               InvalidKeySpecException,
               NoSuchPaddingException
    {
        // PRECONDITIONS
        assert password != null && password.length > 0 : "password must not be null or empty";
        
        // Body
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
        KeySpec          spec    = new PBEKeySpec(password, SALT, NUM_ITERATIONS, KEY_LENGTH);
        
        secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SECRET_KEY_ALGORITHM);
    }
    

    /**
     * @see org.alfresco.util.encryption.Encrypter#encrypt(byte[])
     */
    @Override
    public String encrypt(final String clearText)
    {
        String result = null;
        long   start  = -1;
        long   end    = -1;
        
        if (log.isDebugEnabled())
            start = System.nanoTime();
        
        if (clearText != null)
        {
            try
            {
                Cipher cipher     = Cipher.getInstance(CIPHER_TRANSFORMATION);
                byte[] iv         = null;
                byte[] cipherText = null;
                
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                iv         = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
                cipherText = cipher.doFinal(clearText.getBytes(CHARACTER_ENCODING));
                
                byte[] ivBase64         = Base64.encodeBase64(iv);
                byte[] cipherTextBase64 = Base64.encodeBase64(cipherText);
                
                String intermediate = (new String(ivBase64)) + SEPARATOR + (new String(cipherTextBase64));
                byte[] encrypted    = Base64.encodeBase64(intermediate.getBytes(CHARACTER_ENCODING));
                
                result = new String(encrypted, CHARACTER_ENCODING);
            }
            catch (final RuntimeException re)
            {
                throw re;
            }
            catch (final Exception e)
            {
                // Convert checked exceptions to unchecked, as they're Pure Evil™
                throw new RuntimeException(e);
            }
        }
        
        if (log.isDebugEnabled())
        {
            end = System.nanoTime();
            long diff = end - start;
            double diffInMs = (double)diff / 1000000;
            log.debug("Encryption took " + String.valueOf(diffInMs) + "ms");
        }
        
        return(result);
    }


    /**
     * @see org.alfresco.util.encryption.Encrypter#decrypt(byte[])
     */
    @Override
    public String decrypt(final String encryptedText)
        throws CannotDecryptException
    {
        String result = null;
        long   start  = -1;
        long   end    = -1;
        
        if (log.isDebugEnabled())
            start = System.nanoTime();
        
        if (encryptedText != null)
        {
            try
            {
                byte[] encrypted    = Base64.decodeBase64(encryptedText.getBytes(CHARACTER_ENCODING));
                String intermediate = new String(encrypted, CHARACTER_ENCODING);
                     
                int separatorIndex = intermediate.indexOf(SEPARATOR);
                
                if (separatorIndex == -1)
                {
                    throw new CannotDecryptException("Encrypted text " + intermediate + " is malformed.");
                }
                
                byte[] ivBase64         = intermediate.substring(0, separatorIndex).getBytes();
                byte[] iv               = Base64.decodeBase64(ivBase64);
                byte[] cipherTextBase64 = intermediate.substring(separatorIndex + SEPARATOR.length()).getBytes();
                byte[] cipherText       = Base64.decodeBase64(cipherTextBase64);
            
                Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                result = new String(cipher.doFinal(cipherText), CHARACTER_ENCODING);
            }
            catch (final RuntimeException re)
            {
                throw re;
            }
            catch (final Exception e)
            {
                throw new CannotDecryptException("Unable to decrypt text: " + encryptedText, e);
            }
        }
        
        if (log.isDebugEnabled())
        {
            end = System.nanoTime();
            long diff = end - start;
            double diffInMs = (double)diff / 1000000;
            log.debug("Decryption took " + String.valueOf(diffInMs) + "ms");
        }
        
        return(result);
    }
    

    private static byte[] SALT = { (byte)0x8d, (byte)0x28, (byte)0xfa, (byte)0x55, (byte)0xe4, (byte)0x90, (byte)0x4b, (byte)0x62 };    
}
