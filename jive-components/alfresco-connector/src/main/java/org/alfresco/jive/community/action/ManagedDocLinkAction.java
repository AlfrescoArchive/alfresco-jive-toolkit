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

package org.alfresco.jive.community.action;

import org.alfresco.jive.cmis.manager.AlfrescoNavigationManager;
import org.alfresco.jive.cmis.manager.RemoteDocument;
import org.alfresco.jive.community.impl.CmisDocument;
import org.alfresco.jive.community.impl.ConnectorConstants;
import org.apache.log4j.Logger;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.wiki.WikiContentHelper;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.InvalidLanguageException;
import com.jivesoftware.community.proxy.DocumentProxy;
import com.jivesoftware.community.renderer.impl.v2.JAXPUtils;
import com.jivesoftware.util.StringUtils;

public class ManagedDocLinkAction extends ManagedDocUploadAction {
	private static final long serialVersionUID = 5322965369654141927L;
    private static final Logger log = Logger.getLogger(ManagedDocLinkAction.class);
	    
	private String remoteDocument;

	private int uploadFileSize;
	
    public void validate() {
    	if (!(getDocument() instanceof CmisDocument)) {
    		// TODO: Display correct error on view
            log.debug("Document not correct type");
    	}
    	
        // create a temporary document, if needed
        if (!isEdit() && getDocument() == null) {
            try {
                loadDocument();
            }
            catch (Exception e) {
                log.debug("Failed to create temporary document.", e);
                addActionError(getText("doc.upload.cldNotCreateDoc.text"));
            }
        }

        try {
            if (getDocument() == null) {
                return;
            }

            
            try {
				loadDocument();
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				log.error("Error loading document", ex);
			}
			
			if ((StringUtils.trimToEmpty(getSubject())).length() == 0) {
                addFieldError("subject", getText("doc.create.error_title.text"));
            }
            else {
                try {
                    getDocument().setSubject(getSubject());
                }
                catch (DocumentAlreadyExistsException e) {
                    addFieldError("subject", getText("doc.create.error_title_unique"));
                }
            }
			
			
			
            saveBinaryBody();

            // convert the description to internal Jive HTML.
            String desc = getDescription();

            org.w3c.dom.Document doc;
            if (isMobileEditor()){
                doc = WikiContentHelper.mobileContentToJiveDocument(desc);
            }else{
                doc = WikiContentHelper.editorHtmlToJiveDocument(desc, this.getDocument(), getImageUrlBuilder(), getJiveLinkBuilder());
            }
            filterDocument(doc);
            desc = JAXPUtils.toXmlString(doc);
            getDocument().setSummary(desc);

            if (getLanguage() != null) {
                try {
                    getDocument().setLanguage(getLanguage());
                }
                catch (InvalidLanguageException e) {
                    log.debug("Could not save document, invalid language.", e);
                    addFieldError("language", getText("doc.upload.invalidLangErr.text"));
                }
            }

            // community must be specified
            if (getContainer() == null) {
                addActionError(getText("doc.create.error_community"));
            }
            else {
                if (!hasPostPermission()) {
                    addActionError(getText("doc.upload.ntAuthToAddToCm.text", new String[]{getContainer().getName()}));
                }
            }

            if (hasErrors()) {
                // an error has occurred. backup the temp doc in the session
                getSession().put(getDocumentSessionKey(getDocument().getDocumentID()), getDocument());
            }
        }
        catch (UnauthorizedException e) {
            addActionError(getText("doc.upload.err.not_auth.text"));
        }

        validatedTags = tagActionUtil.getValidTags(tags, this);
    }

    protected void saveBinaryBody() {
    	CmisDocument cmisDocument = (CmisDocument) ((DocumentProxy) getDocument()).getUnproxiedObject();
    	    	
    	try {
			cmisDocument.setLinkedBinaryBody(remoteDocument, getUploadFileFileName(), getUploadFileContentType(), uploadFileSize);
		} catch (BinaryBodyException e) {
            handleUploadError(e);
        } catch (DocumentAlreadyExistsException e) {
        	log.debug("Failed to set linked binary body.", e);
            addActionError(getText("doc.managed.cmis_not_unique.text"));
            
		}

		AlfrescoNavigationManager manager = (AlfrescoNavigationManager)getJiveContext().getSpringBean("alfrescoNavigationManager");
		// Update document in Alfresco to set the "socialized" aspect
		String objectId = getDocument().getProperties().get(ConnectorConstants.REMOTE_OBJECT_PROPERTY);
    	RemoteDocument remoteDoc = new RemoteDocument(objectId, null);
    	manager.updateDocument(remoteDoc, getSubject());
    	
    }
	
    
    public String input() {
    	String code = super.input();
    	
    	AlfrescoNavigationManager manager = (AlfrescoNavigationManager)getJiveContext().getSpringBean("alfrescoNavigationManager");
		RemoteDocument remoteDoc = manager.getDocumentMetadata(remoteDocument); 
		setUploadFileFileName(remoteDoc.getFileName());
		setUploadFileContentType(remoteDoc.getMimeType());
		setSubject(remoteDoc.getFileName());	
		setUploadFileSize(remoteDoc.getSize());
    	return code;
    }
	///////////////////////
	// GETTERS / SETTERS //
	///////////////////////

    
    
	public String getRemoteDocument() {
		return remoteDocument;
	}

	public void setRemoteDocument(String remoteDocument) {
		this.remoteDocument = remoteDocument;		
		
	}

	public void setUploadFileSize(int uploadFileSize) {
		this.uploadFileSize = uploadFileSize;
	}

	public int getUploadFileSize() {
		return uploadFileSize;
	}
}
