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

package org.alfresco.model.jive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy;
import org.alfresco.repo.jive.CannotDeleteSocializedDocumentException;
import org.alfresco.repo.jive.JiveRequest;
import org.alfresco.repo.jive.JiveService;


/**
 * This class implements the behaviours of the jive:socialized aspect.  Specifically, deletes are vetoed (blocked) and updates
 * of any kind (content or properties) are sent to Jive.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @author Jared Ottley (jottley@alfresco.com)
 * @version $Id: JiveSocializedAspect.java 41626 2012-09-14 23:59:00Z wabson $
 *
 */
public class JiveSocializedAspect
    implements OnUpdateNodePolicy,
               BeforeDeleteNodePolicy,
               OnCopyNodePolicy
{
    private final static Log log = LogFactory.getLog(JiveSocializedAspect.class);
    
    private PolicyComponent policyComponent = null;
    private NodeService     nodeService     = null;
    private JiveService     jiveService     = null;
    
    
    public void setPolicyComponent(final PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    
    public void setNodeService(final NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    
    public void setJiveService(final JiveService jiveService)
    {
        this.jiveService = jiveService;
    }
    
    
    public void init()
    {
    	policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME,     JiveModel.ASPECT_JIVE_SOCIALIZED, new JavaBehaviour(this, "onUpdateNode",     NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, JiveModel.ASPECT_JIVE_SOCIALIZED, new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(OnCopyNodePolicy.QNAME,       JiveModel.ASPECT_JIVE_SOCIALIZED, new JavaBehaviour(this, "getCopyCallback",  NotificationFrequency.EVERY_EVENT));
    }
    

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy#onUpdateNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void onUpdateNode(final NodeRef nodeRef)
    {
        if (!JiveRequest.isJiveRequest())
        {
            if (log.isDebugEnabled()) log.debug("The content of socialized node " + String.valueOf(nodeRef) + " was updated - sending content to Jive.");
            jiveService.updateDocument(nodeRef, true);
        }
        else
        {
            if (log.isDebugEnabled()) log.debug("The content of socialized node " + String.valueOf(nodeRef) + " was updated by Jive - not sending content to Jive.");
        }
    }


    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        // Note: we don't veto deletion of working copies, since they are deleted during the checkin process and we need to allow that
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            if (log.isDebugEnabled()) log.debug("Vetoing deletion of socialized node " + String.valueOf(nodeRef));
            throw new CannotDeleteSocializedDocumentException(nodeRef);
        }
        else
        {
            if (log.isDebugEnabled()) log.debug("Allowing deletion of node " + String.valueOf(nodeRef) + " as it is a working copy.");
        }
    }

    
    /**
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
     */
	@Override
	public CopyBehaviourCallback getCopyCallback(final QName classRef, final CopyDetails copyDetails) {
		
		// A copy can't be initiated from the Jive side, but we want to honor the flag
		if (!JiveRequest.isJiveRequest())
		{
			if (log.isDebugEnabled()) log.debug("Socialized node " + String.valueOf(copyDetails.getSourceNodeRef()) + " copied to " + String.valueOf(copyDetails.getTargetNodeRef()) + " - Removing Socialized aspect and not sending properties to Jive.");
		}
		
		return new DoNothingCopyBehaviourCallback();
	}
}
