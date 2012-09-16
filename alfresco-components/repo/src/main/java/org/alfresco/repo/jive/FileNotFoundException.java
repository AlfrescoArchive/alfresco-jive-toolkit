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

package org.alfresco.repo.jive;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This exception is thrown when a given NodeRef doesn't exist.  The only reason this class exists is because
 * <code>org.alfresco.service.cmr.model.FileNotFoundException</code> is a checked exception, which makes it
 * a <i>tad</i> difficult to use.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: FileNotFoundException.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class FileNotFoundException
    extends RuntimeException
{
    private static final long serialVersionUID = 6596710149740601804L;

    public FileNotFoundException(final NodeRef nodeRef)
    {
        super("NodeRef " + String.valueOf(nodeRef) + " does not exist.");
    }
    
}