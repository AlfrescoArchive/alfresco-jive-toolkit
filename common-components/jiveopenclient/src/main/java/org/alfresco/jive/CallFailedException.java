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

/**
 * This exception is used to signal that an authentication error occurred when calling Jive.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: CallFailedException.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class CallFailedException
    extends JiveOpenClientException
{
    private static final long serialVersionUID = 193495805867377749L;

    public CallFailedException(final String message)
    {
        super(message);
    }
    
    public CallFailedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
    
    public CallFailedException(final int statusCode)
    {
        this("Unexpected status code returned from Jive: " + statusCode);
    }
    
    public CallFailedException(final Throwable cause)
    {
        this("Unexpected error while calling Jive: " + cause.getMessage(), cause);
    }

}
