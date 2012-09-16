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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.jivesoftware.api.core.OpenClientErrorBuilder;
import com.jivesoftware.api.core.v1.entities.EntityCollection;
import com.jivesoftware.api.core.v1.entities.containers.SpaceEntity;
import com.jivesoftware.api.core.v1.entities.content.DocumentEntity;
import com.jivesoftware.api.core.v1.entities.users.UserEntity;
import com.jivesoftware.api.core.v1.util.EntityReference;
import com.jivesoftware.api.core.v1.util.ObjectEntityReference;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.CommunityNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.RejectedException;
import com.jivesoftware.community.aaa.JiveUserAuthentication;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.proxy.DocumentProxy;
import com.jivesoftware.community.web.JiveResourceResolver;

public class AlfrescoServiceImpl extends RemoteSupport implements AlfrescoService {

	private static final Logger log = Logger.getLogger(AlfrescoServiceImpl.class);

	private Encrypter encrypter;
	
	///////////////////////
	// AUTOWIRED MEMBERS //
	///////////////////////
		
	private Community rootSpace;
	private DocumentManager documentManager;
	private CommunityManager communityManager;
	private AlfrescoNavigationManager alfrescoNavigationManager;
	
	/**
	 * Initiation of AlfrescoServiceImpl: Create encrypter
	 */
	public void init() {
		try {
			encrypter = new AES256PasswordBasedEncrypter(JiveGlobals.getJiveProperty(
					ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD, 
					ConnectorConstants.USERNAME_ENCRYPTION_PASSWORD_DEFAULT).toCharArray()
			);
		} catch (Exception e) {
			log.error("Error initializing encryption mechanism", e);
		}
	}
	
	//////////////////////
	// OVERRIDE METHODS //
	//////////////////////
	
	@Override
	public EntityCollection<SpaceEntity> getSpaces(int offset, int limit, String user) {
    	SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();
    	
    	try {
    		log.info("User " + user);
			User jiveUser = userManager.getUser(encrypter.decrypt(user));
    		
    		log.info("User Jive" + jiveUser);
			sc.setAuthentication(new JiveUserAuthentication(jiveUser));
						
			return EntityCollection.create(getSubSpaces(rootSpace, offset, limit));

		} catch (CannotDecryptException e) {
			e.printStackTrace();
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		} 
    	catch (UserNotFoundException e) {
    		e.printStackTrace();
			throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} finally {
			sc.setAuthentication(auth);
		}
	}

	protected Collection<SpaceEntity> getSubSpaces(Community parentSpace, int offset, int limit) {
        //  Iterator<Community> subspaces = communityManager.getCommunities(parentSpace, offset, limit); does not work with hidden spaces
        Iterator<Community> subspaces = communityManager.getCommunities(parentSpace);
        Iterators.skip(subspaces, offset);
        subspaces = Iterators.limit(subspaces, limit);

        List<SpaceEntity> entityList= new ArrayList<SpaceEntity>();
        while( subspaces.hasNext()) {
        	Community community = subspaces.next();
        	SpaceEntity entity = createEntity(community);
        	entityList.add(entity);
        }
        
        return ImmutableList
                .copyOf(entityList);
    }

	@Override
	public EntityCollection<SpaceEntity> getSubSpaces(long id, int offset, int limit, String user) {
		SecurityContext sc = SecurityContextHolder.getContext();
    	Authentication auth = sc.getAuthentication();
    	
    	try {
			User jiveUser = userManager.getUser(encrypter.decrypt(user));			
			sc.setAuthentication(new JiveUserAuthentication(jiveUser));
			
			Community parentSpace = communityManager.getCommunity(id);
						
			return EntityCollection.create(getSubSpaces(parentSpace, offset, limit));

		} catch (CannotDecryptException e) {
			throw OpenClientErrorBuilder.forbidden("Cannot decrypt user value");
		} 
    	catch (UserNotFoundException e) {
			throw OpenClientErrorBuilder.forbidden("No user specified or specified user does not exist");
		} catch (CommunityNotFoundException e) {
			throw OpenClientErrorBuilder.notFound(-1, "Community with id " + id + " not found");
		} catch (UnauthorizedException e) {
			OpenClientErrorBuilder.unauthorized();			
		} finally {
			sc.setAuthentication(auth);
		}
		return EntityCollection.create(Collections.EMPTY_LIST);		
	}

	@Override
	public Response createDocument(long id, String contentType, int size,
			String fileName, String cmisId, String user) {
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
	        		JiveApplication.getEffectiveContext().getDocumentTypeManager().getDocumentType(ConnectorConstants.MANAGED_TYPE), 
	        		null, fileName, new String());
	        
	        
	        Community community = null;
			try {
				community = communityManager.getCommunity(id);
			} catch (CommunityNotFoundException e1) {
				throw OpenClientErrorBuilder.internalServerError("Community not found");
			}
	        
	        
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
			
			documentManager.addDocument(community, cmisDocument, Collections.emptyMap());
			
			cmisDocument.setDocumentState(DocumentState.PUBLISHED);
			cmisDocument.save(false, true);
	        
			// Create response
			EntityReference<DocumentEntity> documentEntityReference = ObjectEntityReference
					.create(cmisDocument, createEntity(cmisDocument));
	        
			//URI documentUri = EntityHelper.getSelfLink(documentEntityReference.getEntity());
			
			String documentUrl = JiveResourceResolver.getJiveObjectURL(cmisDocument, true);
			URI documentUri = new URI(documentUrl);
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
		} catch (URISyntaxException e) {
			throw OpenClientErrorBuilder.internalServerError(OpenClientErrorBuilder.ERROR_CODE_UNKOWN,
			"URISyntaxException was thrown");
		} finally {
			sc.setAuthentication(auth);
		}
	}

	@Override
	public Response updateDocument(String contentType, int size,
			String fileName, String cmisId, String user) {
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
			EntityReference<DocumentEntity> documentEntityReference = ObjectEntityReference.create(cmisDocument,
					createEntity(cmisDocument));
			String documentUrl = JiveResourceResolver.getJiveObjectURL(cmisDocument, true);
			URI documentUri;
			try {
				documentUri = new URI(documentUrl);
			} catch (URISyntaxException e) {
				throw OpenClientErrorBuilder.internalServerError(OpenClientErrorBuilder.ERROR_CODE_UNKOWN,
				"URISyntaxException was thrown");
			}
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
	
	
    protected SpaceEntity createEntity(Community community) {
        SpaceEntity entity = new SpaceEntity();
        
        entity.setName(community.getName());
        entity.setDescription(community.getDescription());
        entity.setId(community.getID());

        entity.setCreationDate(community.getCreationDate());
        entity.setModificationDate(community.getModificationDate());
        entity.setDisplayName(community.getDisplayName());
        
        return entity;
    }
    
    
    protected DocumentEntity createEntity(Document document) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setVersion(document.getVersionID());
        documentEntity.setReplyCount(document.getCommentDelegator().getCommentCount());
        documentEntity.setId(document.getID());  
       	documentEntity.setAuthor(createEntity(document.getUser()));     

       	documentEntity.setSubject(document.getSubject());
                        
        documentEntity.setStatus(getStatus(document.getStatus()));
        documentEntity.setCreationDate(document.getCreationDate());
        documentEntity.setModificationDate(document.getModificationDate());
        
        return documentEntity;
    }
    
    private String getStatus(JiveContentObject.Status status) {
        return status.name().toLowerCase();
    }
    
    private UserEntity createEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getID());

        entity.setName(user.getName());
        entity.setUsername(user.getUsername());
        
        if (!user.isEnabled()) {
            entity.setEnabled(user.isEnabled());
        }
        entity.setCreationDate(user.getCreationDate());
        entity.setEmail(user.getEmail());
        entity.setModificationDate(user.getModificationDate());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEnabled(user.isEnabled());
        
        return entity;
    }
    
	///////////////////////
	// AUTOWIRED SETTERS //
	///////////////////////

    @Required
    public void setAlfrescoNavigationManager(
			AlfrescoNavigationManager alfrescoNavigationManager) {
		this.alfrescoNavigationManager = alfrescoNavigationManager;
	}

	public void setRootSpace(Community rootSpace) {
		this.rootSpace = rootSpace;
	}

	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	public void setCommunityManager(CommunityManager communityManager) {
		this.communityManager = communityManager;
	}

}
