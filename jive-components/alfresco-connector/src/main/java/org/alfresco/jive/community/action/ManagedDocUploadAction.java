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
import org.alfresco.jive.community.impl.ConnectorConstants;
import org.apache.log4j.Logger;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.wiki.WikiContentHelper;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.DocumentVersion;
import com.jivesoftware.community.InvalidLanguageException;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.action.DocUploadAction;
import com.jivesoftware.community.impl.DbDocument;
import com.jivesoftware.community.renderer.impl.v2.JAXPUtils;
import com.jivesoftware.community.util.DocumentPermHelper;
import com.jivesoftware.util.StringUtils;


/**
 * This class TODO
 *
 * @author 
 * @version $Id: ManagedDocUploadAction.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class ManagedDocUploadAction
    extends DocUploadAction
{
    private static final long           serialVersionUID = 55038512562328555L;

    private static final Logger         log              = Logger.getLogger(ManagedDocUploadAction.class);

    // used internally to create a new document
    private DocumentType                docType;

    //private DocumentCollaborationHelper collabHelper;

    private AlfrescoNavigationManager   alfrescoNavigationManager;
    
    protected String                    remoteContainer;


    /*
     * private String description; private File uploadFile; private String
     * uploadFileContentType; private String uploadFileFileName;
     */
    protected void loadDocument(User user)
        throws Exception
    {

        String documentID = getDocumentID();
        if (isRestart())
        {
            getSession().remove(getDocumentSessionKey(documentID));
        }

        if (!StringUtils.isEmpty(getDocumentID()))
        {
            // try to load from session, otherwise, create a new temp doc
            if (getSession().containsKey(getDocumentSessionKey(documentID)))
            {
                setDocument((Document)getSession().get(getDocumentSessionKey(documentID)));
            }
            else if (!DbDocument.isTempDocID(documentID))
            {
                setDocument(documentManager.getDocument(documentID));
                DocumentVersion newestVersion = getDocument().getVersionManager().getNewestDocumentVersion();
                setDocument(newestVersion.getDocument());
                if (newestVersion.getAuthor() != null && user != null && newestVersion.getAuthor().getID() != user.getID()
                    && newestVersion.getDocumentState() != DocumentState.PUBLISHED)
                {

                    addActionMessage(getText("doc.create.edit_draft_warn.info", new String[] { getUsername(newestVersion.getAuthor()) }));
                }

                getSession().put(getDocumentSessionKey(documentID), getDocument());
            }
        }

        if (getDocument() == null)
        {
            // create a new temp doc
            cleanSession();
            Document newDocument = documentManager.createDocument(user, getDocumentType(), null, null, (String)null);
            // CS-16228 Default for new documents in user container.
            if (isUserContainer())
            {
                newDocument.setAuthorshipPolicy(Document.AUTHORSHIP_SINGLE);
            }

            if(getRemoteContainer() != null) {
            	newDocument.getProperties().put(ConnectorConstants.REMOTE_CONTAINER_PROPERTY, getRemoteContainer());
            }
            
            setDocument(newDocument);
        }
        else if (!DbDocument.isTempDocID(getDocument().getDocumentID()))
        {
            // set the community ID to be the community ID of the document
            this.setContainer(getDocument().getJiveContainer());
            DocumentVersion newestDocumentVersion = getDocument().getVersionManager().getNewestDocumentVersion();
            if (newestDocumentVersion.getDocument().getStatus() == JiveContentObject.Status.DRAFT)
            {
                if (DocumentPermHelper.isAllowedToEdit(newestDocumentVersion.getDocument(), user))
                {
                    setDocument(newestDocumentVersion.getDocument());
                }
            }
        }
                
        documentID = getDocument().getDocumentID();
        setDocumentID(documentID);
        getSession().put(getDocumentSessionKey(documentID), getDocument());

        //collabHelper = new DocumentCollaborationHelper(getDocument(), getContainer(), user, getJiveContext());
    }

    public void validate() {
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

            // perform field validation

            // validate uploaded document
            if (isJiveUploadSizeLimitExceeded()) {
                addFieldError("uploadFile", getText("doc.upload.err.doc_too_lrg.text"));
            }
            else if (getUploadFile() == null && !isEdit()) {
                addFieldError("uploadFile", getText("doc.upload.err.specify_doc.text"));
            }
            //if additional version, and docverse doc, make sure it's the same type of document as the previous version
            else if (getUploadFile() != null && getDocument().getID() > 0 && getDocument().getVersionManager().getDocumentVersionCount() > 0)
            {
                //String[] nameAndContentType = getUnzippedNameAndContentType(getDocument());
                //if it's a DocVerse document..
                //String name = nameAndContentType[0];
                //String contentType = nameAndContentType[1];

                if (getConversionManager().isConvertable(getDocument())) {
                    //make sure file types are the same
                  
                    if (!getConversionManager().isSameDocumentType(getUploadFileFileName(), getDocument().getBinaryBody().getName())) {
                        addActionError(getText("doc.upload.err.diffconttype.text"));
                        //if no file extension on uploaded file, then make sure they're the same content type
                    }
                    //else if (!uploadFileFileName.contains(".") && !documentConversionActionContext
                    //        .isSameContentType(uploadFileContentType, contentType))
                   // {
                   //     addActionError(getText("doc.upload.err.diffconttype.text"));
                   // }
                }
            }

            if ((StringUtils.trimToEmpty(getSubject())).length() == 0) {
                addFieldError("subject", getText("doc.create.error_title.text"));
            }
            else if(!getSubject().equals(getDocument().getSubject())) {
                try {
                    getDocument().setSubject(getSubject());
                }
                catch (DocumentAlreadyExistsException e) {
                    addFieldError("subject", getText("doc.create.error_title_unique"));
                }
                
                // if binary content is unchanged, need to update remote document title
                /* JIVE-64 Disabling this
                if (getUploadFile() == null) {
                	
                	String objectId = getDocument().getProperties().get(ConnectorConstants.REMOTE_OBJECT_PROPERTY);
                	RemoteDocument remoteDoc = new RemoteDocument(objectId, null);
                	getAlfrescoNavigationManager().updateDocument(remoteDoc, getSubject());
                }
                */
            }
            
            if (getUploadFile() != null && getActionErrors().isEmpty()) {                                                            
                saveBinaryBody();
            }

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

    public boolean isApprovalRequired()
    {
        return false;
    }


    private String getUsername(User user)
    {
        if (user != null)
        {
            return user.getUsername();
        }
        else
        {
            return getText("global.guest");
        }
    }


    /**
     * Returns the default document type
     * 
     * @return the default document type
     */
    private DocumentType getDocumentType()
    {
        if (docType == null)
        {
            try
            {
                docType = getJiveContext().getDocumentTypeManager().getDocumentType(ConnectorConstants.MANAGED_TYPE);
            }
            catch (DocumentObjectNotFoundException e)
            {
                log.error("Unable to access default document type", e);
                return null;
            }
        }

        return docType;
    }


    public void setRemoteContainer(String remoteContainer)
    {
        this.remoteContainer = remoteContainer;
    }


    public String getRemoteContainer()
    {
        return remoteContainer;
    }
    
    /**
     * Returns true if the page user can add or remove document approvers
     *
     * @return true if the page user can add or remove document approvers
     */
    public boolean isAllowedToModifyApprovers() {
        return false;
    }
    
    /**
     * Returns the default document type
     * 
     * @return the default document type
     */
    public AlfrescoNavigationManager getAlfrescoNavigationManager()
    {
        if (alfrescoNavigationManager == null) {            
            alfrescoNavigationManager = getJiveContext().getSpringBean("alfrescoNavigationManager");            
        }
        return alfrescoNavigationManager;
    }
}
