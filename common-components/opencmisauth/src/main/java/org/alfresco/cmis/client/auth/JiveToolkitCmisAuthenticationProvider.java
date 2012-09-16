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


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.util.encryption.Encrypter;
import org.alfresco.util.encryption.impl.AES256PasswordBasedEncrypter;
import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;
import org.w3c.dom.Element;

/**
 * This class provides a custom OpenCMIS <code>AuthenticationProvider</code> that sends through
 * encrypted 
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveToolkitCmisAuthenticationProvider.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class JiveToolkitCmisAuthenticationProvider
    extends StandardAuthenticationProvider
{
    private final static long serialVersionUID = -4672820288661898523L;
    
    public  final static String SESSION_KEY_USER_ID         = "jivetoolkit.masqueraded.userid";
    public  final static String SESSION_KEY_SHARED_PASSWORD = "jivetoolkit.shared.password";
    
    private final static String HTTP_HEADER_NAME_USER_ID    = "X-AlfrescoJive-UserId";
    
    private transient Encrypter encrypter = null;
    
    
    /**
     * @see org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider#getHTTPHeaders(java.lang.String)
     */
    @Override
    public Map<String, List<String>> getHTTPHeaders(String url)
    {
        final Map<String, List<String>> result          = super.getHTTPHeaders(url);
        final String                    userId          = (String)getSession().get(SESSION_KEY_USER_ID);
        String                          encryptedUserId = null;
        
        if (userId == null || userId.trim().length() == 0)
        {
            throw new UnsupportedOperationException("OpenCMIS Session object is missing the mandatory '" + SESSION_KEY_USER_ID + "' key.");
        }

        lazyInitEncrypter();  // *sigh*
        
        encryptedUserId = encrypter.encrypt(userId);
        result.put(HTTP_HEADER_NAME_USER_ID, Collections.singletonList(encryptedUserId));
        
        return(result);
    }


    /**
     * @see org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider#getSOAPHeaders(java.lang.Object)
     */
    @Override
    public Element getSOAPHeaders(final Object portObject)
    {
        throw new UnsupportedOperationException("The SOAP binding is not supported by this authenticator.");
    }
    
    
    /**
     * This monstrosity is a truly awful hack/workaround for https://issues.apache.org/jira/browse/CMIS-379
     */
    private synchronized void lazyInitEncrypter()
    {
        if (encrypter == null)
        {
            String encryptionPassword = (String)getSession().get(SESSION_KEY_SHARED_PASSWORD);
            
            if (encryptionPassword == null || encryptionPassword.trim().length() == 0)
            {
                throw new UnsupportedOperationException("OpenCMIS Session object is missing the mandatory '" + SESSION_KEY_SHARED_PASSWORD + "' key.");
            }

            try
            {
                encrypter = new AES256PasswordBasedEncrypter(encryptionPassword.toCharArray());
            }
            catch (final RuntimeException re)
            {
                throw re;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    

}
