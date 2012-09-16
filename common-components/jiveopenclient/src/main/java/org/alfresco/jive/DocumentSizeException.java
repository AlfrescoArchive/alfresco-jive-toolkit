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
 * This exception is used to signal that a Document is larger (by name or size than what Jive allows.
 * Name length <= 256 Characters
 * Document Size <= 100Mb
 *
 * @author Jared Ottley (jottley@alfresco.com)
 * @version $Id: DocumentNotFoundException.java 28173 2011-06-02 23:22:22Z jottley $
 *
 */
public class DocumentSizeException
    extends JiveOpenClientException
{
    private static final long serialVersionUID = -1163536246410161116L;

	public DocumentSizeException(final String fileName, final long fileSize)
    {
    	super((new Object() {
    		@Override
    		public String toString() {
    			
    			String message = "The file exceeds the allowed file size (100MB) or name length (256 Characters).";
    			
    			if (fileName.length() > 256)
    	        {
    	        	message = "The file name exceeds the allowed length in Jive (256 Characters).";	
    	        } 
    			else if (fileSize > 104857600L) // == 100MB in bytes
    	        {
    	        	message = "The file exceeds the allowed size in Jive (100MB).";	
    	        }
    			
    			return message;
    		}
    	}).toString());
    	
        
    }

}
