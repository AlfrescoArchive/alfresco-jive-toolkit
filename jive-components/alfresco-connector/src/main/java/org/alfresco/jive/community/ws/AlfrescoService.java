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

package org.alfresco.jive.community.ws;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.jivesoftware.api.core.v1.entities.EntityCollection;
import com.jivesoftware.api.core.v1.entities.containers.SpaceEntity;


@Produces("application/json")
@Path("/alfresco")
public interface AlfrescoService {

	public static final String PARAM_MIME_TYPE = "mime-type";
	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_USER = "X-AlfrescoJive-UserId";
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_CMIS_ID = "cmis:id";
	
    @GET
    @Path("/spaces")
    public EntityCollection<SpaceEntity> getSpaces(
    		@QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("25") int limit, 
            @HeaderParam(PARAM_USER) String user);
    
    @GET
    @Path("/spaces/{id}/children")
    public EntityCollection<SpaceEntity> getSubSpaces(
    		@PathParam("id") long id,
            @QueryParam("offset") @DefaultValue("0") int offset, 
            @QueryParam("limit") @DefaultValue("25") int limit,
            @HeaderParam(PARAM_USER) String user);
    
    @POST
    @Path("/spaces/{id}/documents")
    public Response createDocument(
    		@PathParam("id") long id, 
    		@FormParam(PARAM_MIME_TYPE) String contentType,
            @FormParam(PARAM_SIZE) int size, 
            @FormParam(PARAM_FILENAME) String fileName,
            @FormParam(PARAM_CMIS_ID) String cmisId, 
            @HeaderParam(PARAM_USER) String user);
    
    @PUT
    @Path("/documents")
    public Response updateDocument(
    		@FormParam(PARAM_MIME_TYPE) String contentType,
            @FormParam(PARAM_SIZE) int size, 
            @FormParam(PARAM_FILENAME) String fileName,
            @FormParam(PARAM_CMIS_ID) String cmisId,
            @HeaderParam(PARAM_USER) String user);
}
