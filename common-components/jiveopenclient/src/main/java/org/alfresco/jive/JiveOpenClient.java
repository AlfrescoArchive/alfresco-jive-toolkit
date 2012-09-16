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

import org.json.JSONObject;

/**
 * This interface declares some of the operations available via the Jive OpenClient APIs.
 * It it limited to those APIs that are used by the Jive Toolkit - many more APIs exist
 * in the actual Jive OpenClient.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: JiveOpenClient.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public interface JiveOpenClient
{

    /**
     * Retrieves all of the root-level spaces (Jive communities) visible to the provided user.
     * 
     * @param userId The id of the user on whose behalf to perform the operation <i>(must not be null, empty or blank)</i>.
     * @return The JSON for the root-level spaces <i>(will not be null, but may be empty or blank)</i>.
     * @throws AuthenticationException If basic authentication fails or if the given userId is invalid in Jive.
     * @throws CallFailedException     If an error occurred while attempting to call Jive.
     */
    public JSONObject getSpaces(final String userId)
        throws AuthenticationException,
               CallFailedException,
               ServiceUnavailableException;
    
    
    /**
     * Retrieves all of the sub-level spaces (Jive sub-communities) of the given parent space, visible to the provided user.
     * 
     * @param userId  The id of the user on whose behalf to perform the operation <i>(must not be null, empty or blank)</i>.
     * @param spaceId The id of the space whose sub-spaces are to be retrieved.
     * @return The JSON for the sub-spaces <i>(will not be null, but may be empty or blank)</i>.
     * @throws AuthenticationException If basic authentication fails or if the given userId is invalid in Jive.
     * @throws CallFailedException     If an error occurred while attempting to call Jive.
     * @throws SpaceNotFoundException  If there is no space in Jive with the given id. 
     */
    public JSONObject getSubSpaces(final String userId, final long spaceId)
        throws AuthenticationException,
               CallFailedException,
               SpaceNotFoundException,
               ServiceUnavailableException;
    
    
    /**
     * Create a document in Jive using the provided JSON, on behalf of the specified user.
     * 
     * @param userId   The id of the user on whose behalf to perform the operation <i>(must not be null, empty or blank)</i>.
     * @param spaceId  The id of the space in which to create this document.
     * @param cmisId   The cmis:id of the document being created <i>(must not be null, empty or blank)</i>.
     * @param fileName The fileName of the document <i>(must not be null, empty or blank)</i>.
     * @param fileSize The size of the file in bytes <i>(must be >= 0)</i>.
     * @param mimeType The mimeType of the file <i>(must not be null, empty or blank)</i>. 
     * @throws AuthenticationException If basic authentication fails or if the given userId is invalid in Jive.
     * @throws CallFailedException     If an error occurred while attempting to call Jive.
     * @throws SpaceNotFoundException  If there is no space in Jive with the given id. 
     */
    public void createDocument(final String userId,
                               final long   spaceId,
                               final String cmisId,
                               final String fileName,
                               final long   fileSize,
                               final String mimeType)
        throws AuthenticationException,
               CallFailedException,
               SpaceNotFoundException,
               ServiceUnavailableException,
               DocumentSizeException;
    
    
    /**
     * Update a document in Jive using the provided JSON, on behalf of the specified user.
     * 
     * @param userId The id of the user on whose behalf to perform the operation <i>(must not be null, empty or blank)</i>.
     * @param cmisId   The cmis:id of the document being created <i>(must not be null, empty or blank)</i>.
     * @param fileName The fileName of the document <i>(must not be null, empty or blank)</i>.
     * @param fileSize The size of the file in bytes <i>(must be >= 0)</i>.
     * @param mimeType The mimeType of the file <i>(must not be null, empty or blank)</i>. 
     * @throws AuthenticationException   If basic authentication fails or if the given userId is invalid in Jive.
     * @throws CallFailedException       If an error occurred while attempting to call Jive.
     * @throws DocumentNotFoundException If there is no document in Jive with the given cmis:id.
     */
    public void updateDocument(final String userId,
                               final String cmisId,
                               final String fileName,
                               final long   fileSize,
                               final String mimeType)
        throws AuthenticationException,
               CallFailedException,
               DocumentNotFoundException,
               ServiceUnavailableException,
               DocumentSizeException;

}
