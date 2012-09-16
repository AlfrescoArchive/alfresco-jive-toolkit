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

package org.alfresco.jive.community.ws.legacy;

import java.net.URI;
import java.util.Collections;

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

import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.community.impl.CmisDocument;
import org.alfresco.jive.community.impl.ConnectorConstants;
import org.alfresco.util.encryption.CannotDecryptException;
import org.alfresco.util.encryption.Encrypter;
import org.alfresco.util.encryption.impl.AES256PasswordBasedEncrypter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Function;
import com.jivesoftware.api.core.OpenClientErrorBuilder;
import com.jivesoftware.api.core.v1.converters.content.DocumentEntityConverter;
import com.jivesoftware.api.core.v1.entities.Entity;
import com.jivesoftware.api.core.v1.entities.EntityCollection;
import com.jivesoftware.api.core.v1.entities.containers.SpaceEntity;
import com.jivesoftware.api.core.v1.entities.content.DocumentEntity;
import com.jivesoftware.api.core.v1.providers.containers.SpaceProvider;
import com.jivesoftware.api.core.v1.services.BaseService;
import com.jivesoftware.api.core.v1.util.EntityHelper;
import com.jivesoftware.api.core.v1.util.EntityReference;
import com.jivesoftware.api.core.v1.util.ObjectEntityReference;
import com.jivesoftware.api.core.v1.util.Paginator;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.RejectedException;
import com.jivesoftware.community.aaa.JiveUserAuthentication;
import com.jivesoftware.community.proxy.DocumentProxy;

@Produces("application/json")
@Path("/alfresco")
public class AlfrescoService<C extends JiveContainer, A extends Entity> extends BaseService {

	private static final Logger log = Logger.getLogger(AlfrescoService.class.getName());

	public static final String PARAM_MIME_TYPE = "mime-type";
	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_CMIS_ID = "cmis:id";
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_USER = "X-AlfrescoJive-UserId";

	private AlfrescoNavigationManager alfrescoNavigationManager;
	private Function<Long, C> containerObjectConverter;
	private DocumentManager documentManager;
	private Encrypter encrypter;
	private Community rootSpace;
	private SpaceProvider spaceProvider;
	private UserManager userManager;
	private JiveContext jiveContext;
	private DocumentEntityConverter entityConverter;

	@Required
    public void setAlfrescoNavigationManager(
			AlfrescoNavigationManager alfrescoNavigationManager) {
		this.alfrescoNavigationManager = alfrescoNavigationManager;
	}

	@Required
    public final void setContainerObjectConverter(Function<Long, C> containerObjectConverter) {
        this.containerObjectConverter = containerObjectConverter;
    }

	@Required
	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	@Required
    public final void setRootSpace(Community rootSpace) {
        this.rootSpace = rootSpace;
    }

    @Required
    public final void setSpaceProvider(SpaceProvider spaceProvider) {
        this.spaceProvider = spaceProvider;
    }

	@Required
    public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@Required
	public void setJiveContext(JiveContext jiveContext) {
		this.jiveContext = jiveContext;
	}

	@Required
    public final void setEntityConverter(DocumentEntityConverter entityConverter) {
        this.entityConverter = entityConverter;
    }

	public AlfrescoService() {
		super();

		try {
			encrypter = new AES256PasswordBasedEncrypter(JiveGlobals.getJiveProperty(
					ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD,
					ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD_DEFAULT).toCharArray()
			);
		} catch (Exception e) {
			log.error("Error initializing encryption mechanism", e);
		}
	}

    @GET
    @Path("/spaces")
    public EntityCollection<SpaceEntity> getSpaces(@QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("25") int limit, @HeaderParam(PARAM_USER) String user)
    {
    	SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();

    	try {
			User jiveUser = userManager.getUser(encrypter.decrypt(user));

			sc.setAuthentication(new JiveUserAuthentication(jiveUser));

			Paginator paginator = paginationHelper.getPaginator(offset, limit);

	        return attachPagination(paginator, spaceProvider.getSubSpaces(rootSpace, offset, limit));

		} catch (CannotDecryptException e) {
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		}
    	catch (UserNotFoundException e) {
			throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} finally {
			sc.setAuthentication(auth);
		}

    }

    @GET
    @Path("/spaces/{id}/children")
    public EntityCollection<SpaceEntity> getSubSpaces(@PathParam("id") long id,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("25") int limit,
            @HeaderParam(PARAM_USER) String user)
    {
    	SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();

    	try {
			User jiveUser = userManager.getUser(encrypter.decrypt(user));

			sc = SecurityContextHolder.getContext();
			sc.setAuthentication(new JiveUserAuthentication(jiveUser));

			Paginator paginator = paginationHelper.getPaginator(offset, limit);

	        return attachPagination(paginator, spaceProvider.getSubSpaces(id, offset, limit));

		} catch (CannotDecryptException e) {
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		} catch (UserNotFoundException e) {
			throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} finally {
			sc.setAuthentication(auth);
		}
    }

    @POST
    @Path("/spaces/{id}/documents")
    public Response createDocument(@PathParam("id") long id, @FormParam(PARAM_MIME_TYPE) String contentType,
            @FormParam(PARAM_SIZE) int size, @FormParam(PARAM_FILENAME) String fileName,
            @FormParam(PARAM_CMIS_ID) String cmisId, @HeaderParam(PARAM_USER) String user)
    {
    	SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();

    	try {
			User jiveUser = userManager.getUser(encrypter.decrypt(user));
			if (jiveUser == null || jiveUser.isAnonymous()) {
				throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
			}

			sc = SecurityContextHolder.getContext();
			sc.setAuthentication(new JiveUserAuthentication(jiveUser));

	        // Create a managed document
	        Document document = documentManager.createDocument(jiveUser,
	        		jiveContext.getDocumentTypeManager().getDocumentType(ConnectorConstants.MANAGED_TYPE),
	        		null, fileName, new String());
	        document.setDocumentState(DocumentState.PUBLISHED);
	        documentManager.addDocument(getContainer(id), document, Collections.emptyMap());

	        // Associate managed document with external content
	        CmisDocument cmisDocument = (CmisDocument) ((DocumentProxy) document).getUnproxiedObject();

	        try {
				cmisDocument.setLinkedBinaryBody(cmisId, fileName, contentType, size);
			} catch (BinaryBodyException e) {
				switch (e.getErrorType()) {
		            case BinaryBodyException.TOO_LARGE: {
		            	throw OpenClientErrorBuilder.conflict(BinaryBodyException.TOO_LARGE, "The document is too large or has a file name longer than 256 characters");
		            }
		            case BinaryBodyException.BAD_CONTENT_TYPE: {
		            	throw OpenClientErrorBuilder.internalServerError("The content type of this document is not allowed");
		            }
		            default: {
		            	throw OpenClientErrorBuilder.internalServerError("There was an error creating your document");
		            }
				}
	        }

			cmisDocument.save(false, true);

			// Create response
			EntityReference<DocumentEntity> documentEntityReference = ObjectEntityReference.create(cmisDocument, entityConverter.apply(cmisDocument));
	        URI documentUri = EntityHelper.getSelfLink(documentEntityReference.getEntity());
	        return Response.created(documentUri).entity(documentEntityReference.getEntity()).build();

    	} catch (CannotDecryptException e) {
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		} catch (UserNotFoundException e) {
    		throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} catch (UnauthorizedException e) {
			throw OpenClientErrorBuilder.unauthorized();
		} catch (DuplicateIDException e) {
			throw OpenClientErrorBuilder.duplicateDocumentId();
		} catch (DocumentObjectNotFoundException e) {
			throw OpenClientErrorBuilder.internalServerError(OpenClientErrorBuilder.ERROR_CODE_OBJECT_TYPE_NOT_FOUND,
					"Document type for managed documents not found");
		} catch (RejectedException e) {
			throw OpenClientErrorBuilder.internalServerError(OpenClientErrorBuilder.ERROR_CODE_UNKOWN,
					"RejectedException was thrown");
		} catch (DocumentAlreadyExistsException e) {
			throw OpenClientErrorBuilder.duplicateDocumentId();
		} finally {
			sc.setAuthentication(auth);
		}
    }

    @PUT
    @Path("/documents")
    public Response updateDocument(@FormParam(PARAM_MIME_TYPE) String contentType,
            @FormParam(PARAM_SIZE) int size, @FormParam(PARAM_FILENAME) String fileName,
            @FormParam(PARAM_CMIS_ID) String cmisId,@HeaderParam(PARAM_USER) String user) {
    	SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();

    	try {
			User jiveUser = userManager.getUser(encrypter.decrypt(user));

			sc = SecurityContextHolder.getContext();
			sc.setAuthentication(new JiveUserAuthentication(jiveUser));

			// Retrieve existing managed document
			long documentId = alfrescoNavigationManager.getJiveId(cmisId);
			Document document = documentManager.getDocument(documentId);
			document.setSubject(fileName);

			// Associate managed document with external content
	        CmisDocument cmisDocument = (CmisDocument) ((DocumentProxy) document).getUnproxiedObject();

	        try {
				cmisDocument.setLinkedBinaryBody(cmisId, fileName, contentType, size);
			} catch (BinaryBodyException e) {
				switch (e.getErrorType()) {
		            case BinaryBodyException.TOO_LARGE: {
		            	throw OpenClientErrorBuilder.conflict(BinaryBodyException.TOO_LARGE, "The document is too large or has a file name longer than 256 characters");
		            }
		            case BinaryBodyException.BAD_CONTENT_TYPE: {
		            	throw OpenClientErrorBuilder.internalServerError("The content type of this document is not allowed");
		            }
		            default: {
		            	throw OpenClientErrorBuilder.internalServerError("There was an error creating your document");
		            }
				}
	        }

			cmisDocument.save(false, true);

			// Create response
			EntityReference<DocumentEntity> documentEntityReference = ObjectEntityReference.create(cmisDocument, entityConverter.apply(cmisDocument));
	        URI documentUri = EntityHelper.getSelfLink(documentEntityReference.getEntity());
	        return Response.created(documentUri).entity(documentEntityReference.getEntity()).build();

		} catch (CannotDecryptException e) {
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		} catch (UserNotFoundException e) {
			throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} catch (DocumentObjectNotFoundException e) {
			throw OpenClientErrorBuilder.internalServerError(OpenClientErrorBuilder.ERROR_CODE_OBJECT_TYPE_NOT_FOUND,
					"Document type for managed documents not found");
		} catch (UnauthorizedException e) {
			throw OpenClientErrorBuilder.unauthorized();
		} catch (DocumentAlreadyExistsException e) {
			throw OpenClientErrorBuilder.duplicateDocumentId();
		} finally {
			sc.setAuthentication(auth);
		}
    }

    private C getContainer(long id) {
        return getContainerObjectConverter().apply(id);
    }

    public Function<Long, C> getContainerObjectConverter() {
        return containerObjectConverter;
    }

}
