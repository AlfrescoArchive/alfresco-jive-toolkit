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
 * This exception is used to signal that decryption of some encrypted text failed.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: CannotDecryptException.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CannotDecryptException
    extends RuntimeException
{
    private static final long serialVersionUID = 5861663023997730443L;
    
    public CannotDecryptException(final String message)
    {
        super(message);
    }
    
    public CannotDecryptException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
    
}
