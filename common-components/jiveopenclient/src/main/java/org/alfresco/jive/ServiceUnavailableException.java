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
 * This exception is used to signal that Jive was unreachable.
 *
 * @author Jared Ottley (jottley@alfresco.com)
 * @version $Id: ServiceUnavailableException.java 28085 2011-08-08 12:31:06Z jottley $
 *
 */
public class ServiceUnavailableException
    extends JiveOpenClientException
{
    private static final long serialVersionUID = 8929666569503635168L;

    public ServiceUnavailableException()
    {
        super("Failed to connect to Jive.  Please try again Shortly.  If the error persists please contact your System Administrator.");
    }

    public ServiceUnavailableException(final Throwable cause)
    {
        super("Failed to connect to Jive.   Please try again Shortly.  If the error persists please contact your System Administrator.", cause);
    }

}
