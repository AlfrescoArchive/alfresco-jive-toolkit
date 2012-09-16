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

package org.alfresco.util.encryption;

/**
 * This interface defines a generic encrypt / decrypt process.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: Encrypter.java 41626 2012-09-14 23:59:00Z wabson $
 */
public interface Encrypter
{
    /**
     * Encrypt the given clear text.
     * 
     * @param clearText The clear text to encrypt <i>(may be null)</i>.
     * @return The encrypted text, in base64 format <i>(null when input is null)</i>.
     */
    public String encrypt(final String clearText);

    
    /**
     * Decrypt the given encrypted text to clear text.
     * 
     * @param encryptedText The base64 data to decrypt <i>(may be null)</i>.
     * @return The clear text <i>(null when input is null)</i>.
     * @throws CannotDecryptException If decryption fails.  This is usually due to an invalid passwords.
     */
    public String decrypt(final String encryptedText)
        throws CannotDecryptException;
}
