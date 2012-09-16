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

package org.alfresco.repo.jive.impl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.jive.AbstractJiveService;
import org.alfresco.repo.jive.InvalidCommunityException;
import org.alfresco.repo.jive.JiveCommunity;
import org.alfresco.repo.jive.NotAFileException;
import org.alfresco.repo.jive.FileNotFoundException;
import org.alfresco.repo.jive.NotSocializedException;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * This class is a mocked out version of the <code>JiveService</code>.  It emulates calls
 * to Jive in a test-appropriate fashion.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id: MockJiveService.java 41626 2012-09-14 23:59:00Z wabson $
 * @see org.alfresco.repo.jive.JiveService
 *
 */
public class MockJiveService
    extends AbstractJiveService
{
    private final static Log log = LogFactory.getLog(MockJiveService.class);
    
    private final TreeModel mockCommunities;   // Oh how I wish that java.util had a sane, generics-based Tree data structure...
    
    
    public MockJiveService()
    {
        this.mockCommunities = buildMockData1();
//        this.mockCommunities = buildMockData2();
    }
    
    
    /**
     * @see org.alfresco.repo.jive.JiveService#socializeDocument(org.alfresco.service.cmr.repository.NodeRef, long)
     */
    @Override
    public void socializeDocuments(final List<NodeRef> nodeRefs, final long communityId)
        throws FileNotFoundException,
               NotAFileException,
               InvalidCommunityException
    {
        validateNodeRefsAreSocializable(nodeRefs);
        getSubCommunities(communityId);   // Lazy way to validate the communityId - fine for this mockup code, but not cool for "real" code
        markNodeRefsSocialized(nodeRefs, communityId);
    }

    
    /**
     * @see org.alfresco.repo.jive.JiveService#updateDocument(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void updateDocument(final NodeRef nodeRef, final boolean contentUpdated)
        throws FileNotFoundException,
               NotSocializedException
    {
        validateNodeRefIsSocialized(nodeRef);

        // Nothing "real" to do in this mockup implementation
        log.info((contentUpdated ? "Content of " : "Properties of ") + "nodeRef " + String.valueOf(nodeRef) + " updated in Jive.");
    }


    /**
     * @see org.alfresco.repo.jive.JiveService#getCommunities()
     */
    @Override
    public List<JiveCommunity> getCommunities()
    {
        List<JiveCommunity>          result = null;
        final DefaultMutableTreeNode root   = (DefaultMutableTreeNode)mockCommunities.getRoot();
        
        if (root == null)
        {
            result = new ArrayList<JiveCommunity>();
        }
        else
        {
            result = getSubCommunities(root);
        }
        
        return(result);
    }
        
        
    /**
     * @see org.alfresco.repo.jive.JiveService#getCommunities(long)
     */
    @Override
    public List<JiveCommunity> getSubCommunities(final long communityId)
        throws InvalidCommunityException
    {
        List<JiveCommunity>          result = null;
        final DefaultMutableTreeNode root   = (DefaultMutableTreeNode)mockCommunities.getRoot();
        
        if (root != null)
        {
            final TreeNode parent = findNodeById(mockCommunities, root, communityId);
            
            if (parent != null)
            {
                result = getSubCommunities(parent);
            }
            else
            {
                throw new InvalidCommunityException(communityId);
            }
        }
        
        return(result);
    }
    
    
    private final List<JiveCommunity> getSubCommunities(final TreeNode parent)
    {
        List<JiveCommunity> result = new ArrayList<JiveCommunity>();
        
        if (parent != null)
        {
            int childCount = mockCommunities.getChildCount(parent);
            
            if (childCount > 0)
            {
                for (int i = 0; i < childCount; i++)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)mockCommunities.getChild(parent, i);
                    
                    if (node != null)
                    {
                        JiveCommunity subCommunity = (JiveCommunity)node.getUserObject();
                        
                        if (subCommunity != null)
                        {
                            result.add(subCommunity);
                        }
                    }
                }
            }
        }
        
        return(result);
    }
    

    private final static TreeNode findNodeById(final TreeModel tree, final DefaultMutableTreeNode node, final long communityId)
    {
        TreeNode result = null;
        
        if (node != null)
        {
            JiveCommunity community = (JiveCommunity)node.getUserObject();
            
            if (community != null && community.getId() == communityId)
            {
                result = node;
            }
            else
            {
                int childCount = tree.getChildCount(node);
                
                if (childCount > 0)
                {
                    for (int i = 0; i < childCount; i++)
                    {
                        // Recursion
                        result = findNodeById(tree, (DefaultMutableTreeNode)tree.getChild(node, i), communityId);
                        
                        // Termination condition
                        if (result != null)
                        {
                            break;
                        }
                    }
                }
            }
        }
        
        return(result);
    }
    


    private final TreeModel buildMockData1()
    {
        TreeModel result = null;

        /*
         * Build up a tree that looks like this:
         * 
         * root
         *   Child 1
         *     Child 1.1
         *       Child 1.1.1
         *       Child 1.1.2
         *       Child 1.1.3
         *         Child 1.1.3.1
         *           Child 1.1.3.1.1
         *             Child 1.1.3.1.1.1
         *               Child 1.1.3.1.1.1.1
         *                 Child 1.1.3.1.1.1.1.1
         *     Child 1.2
         *       Child 1.2.1
         *       Child 1.2.2
         *     Child 1.3
         *   Child 2
         *     Child 2.1
         *     Child 2.2
         *     Child 2.3
         *     Child 2.4
         *     ... 45 other children omitted for brevity ...
         *     Child 2.50
         *   Child 3
         *   Child 4 which has some punctuation characters: '";/?\[]{}
         *   Child 5 which has an extremely long name that is intended to test out how it'll appear in the Share UI
         *   Child 6 which has some Unicode characters: ¢€£¥©®™§¶†‡❦ठःअठी३
         */
        
        // First up we create all the nodes
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JiveCommunity(0, "root"));
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(new JiveCommunity(1, "Child 1"));
        DefaultMutableTreeNode child11 = new DefaultMutableTreeNode(new JiveCommunity(11, "Child 1.1"));
        DefaultMutableTreeNode child111 = new DefaultMutableTreeNode(new JiveCommunity(111, "Child 1.1.1"));
        DefaultMutableTreeNode child112 = new DefaultMutableTreeNode(new JiveCommunity(112, "Child 1.1.2"));
        DefaultMutableTreeNode child113 = new DefaultMutableTreeNode(new JiveCommunity(113, "Child 1.1.3"));
        DefaultMutableTreeNode child1131 = new DefaultMutableTreeNode(new JiveCommunity(1131, "Child 1.1.3.1"));
        DefaultMutableTreeNode child11311 = new DefaultMutableTreeNode(new JiveCommunity(11311, "Child 1.1.3.1.1"));
        DefaultMutableTreeNode child113111 = new DefaultMutableTreeNode(new JiveCommunity(113111, "Child 1.1.3.1.1.1"));
        DefaultMutableTreeNode child1131111 = new DefaultMutableTreeNode(new JiveCommunity(1131111, "Child 1.1.3.1.1.1.1"));
        DefaultMutableTreeNode child11311111 = new DefaultMutableTreeNode(new JiveCommunity(11311111, "Child 1.1.3.1.1.1.1.1"));
        DefaultMutableTreeNode child12 = new DefaultMutableTreeNode(new JiveCommunity(12, "Child 1.2"));
        DefaultMutableTreeNode child121 = new DefaultMutableTreeNode(new JiveCommunity(121, "Child 1.2.1"));
        DefaultMutableTreeNode child122 = new DefaultMutableTreeNode(new JiveCommunity(122, "Child 1.2.2"));
        DefaultMutableTreeNode child13 = new DefaultMutableTreeNode(new JiveCommunity(13, "Child 1.3"));
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(new JiveCommunity(2, "Child 2"));
        DefaultMutableTreeNode child21 = new DefaultMutableTreeNode(new JiveCommunity(21, "Child 2.1"));
        DefaultMutableTreeNode child22 = new DefaultMutableTreeNode(new JiveCommunity(22, "Child 2.2"));
        DefaultMutableTreeNode child23 = new DefaultMutableTreeNode(new JiveCommunity(23, "Child 2.3"));
        DefaultMutableTreeNode child24 = new DefaultMutableTreeNode(new JiveCommunity(24, "Child 2.4"));
        DefaultMutableTreeNode child25 = new DefaultMutableTreeNode(new JiveCommunity(25, "Child 2.5"));
        DefaultMutableTreeNode child26 = new DefaultMutableTreeNode(new JiveCommunity(26, "Child 2.6"));
        DefaultMutableTreeNode child27 = new DefaultMutableTreeNode(new JiveCommunity(27, "Child 2.7"));
        DefaultMutableTreeNode child28 = new DefaultMutableTreeNode(new JiveCommunity(28, "Child 2.8"));
        DefaultMutableTreeNode child29 = new DefaultMutableTreeNode(new JiveCommunity(29, "Child 2.9"));
        DefaultMutableTreeNode child210 = new DefaultMutableTreeNode(new JiveCommunity(210, "Child 2.10"));
        DefaultMutableTreeNode child211 = new DefaultMutableTreeNode(new JiveCommunity(211, "Child 2.11"));
        DefaultMutableTreeNode child212 = new DefaultMutableTreeNode(new JiveCommunity(212, "Child 2.12"));
        DefaultMutableTreeNode child213 = new DefaultMutableTreeNode(new JiveCommunity(213, "Child 2.13"));
        DefaultMutableTreeNode child214 = new DefaultMutableTreeNode(new JiveCommunity(214, "Child 2.14"));
        DefaultMutableTreeNode child215 = new DefaultMutableTreeNode(new JiveCommunity(215, "Child 2.15"));
        DefaultMutableTreeNode child216 = new DefaultMutableTreeNode(new JiveCommunity(216, "Child 2.16"));
        DefaultMutableTreeNode child217 = new DefaultMutableTreeNode(new JiveCommunity(217, "Child 2.17"));
        DefaultMutableTreeNode child218 = new DefaultMutableTreeNode(new JiveCommunity(218, "Child 2.18"));
        DefaultMutableTreeNode child219 = new DefaultMutableTreeNode(new JiveCommunity(219, "Child 2.19"));
        DefaultMutableTreeNode child220 = new DefaultMutableTreeNode(new JiveCommunity(220, "Child 2.20"));
        DefaultMutableTreeNode child221 = new DefaultMutableTreeNode(new JiveCommunity(221, "Child 2.21"));
        DefaultMutableTreeNode child222 = new DefaultMutableTreeNode(new JiveCommunity(222, "Child 2.22"));
        DefaultMutableTreeNode child223 = new DefaultMutableTreeNode(new JiveCommunity(223, "Child 2.23"));
        DefaultMutableTreeNode child224 = new DefaultMutableTreeNode(new JiveCommunity(224, "Child 2.24"));
        DefaultMutableTreeNode child225 = new DefaultMutableTreeNode(new JiveCommunity(225, "Child 2.25"));
        DefaultMutableTreeNode child226 = new DefaultMutableTreeNode(new JiveCommunity(226, "Child 2.26"));
        DefaultMutableTreeNode child227 = new DefaultMutableTreeNode(new JiveCommunity(227, "Child 2.27"));
        DefaultMutableTreeNode child228 = new DefaultMutableTreeNode(new JiveCommunity(228, "Child 2.28"));
        DefaultMutableTreeNode child229 = new DefaultMutableTreeNode(new JiveCommunity(229, "Child 2.29"));
        DefaultMutableTreeNode child230 = new DefaultMutableTreeNode(new JiveCommunity(230, "Child 2.30"));
        DefaultMutableTreeNode child231 = new DefaultMutableTreeNode(new JiveCommunity(231, "Child 2.31"));
        DefaultMutableTreeNode child232 = new DefaultMutableTreeNode(new JiveCommunity(232, "Child 2.32"));
        DefaultMutableTreeNode child233 = new DefaultMutableTreeNode(new JiveCommunity(233, "Child 2.33"));
        DefaultMutableTreeNode child234 = new DefaultMutableTreeNode(new JiveCommunity(234, "Child 2.34"));
        DefaultMutableTreeNode child235 = new DefaultMutableTreeNode(new JiveCommunity(235, "Child 2.35"));
        DefaultMutableTreeNode child236 = new DefaultMutableTreeNode(new JiveCommunity(236, "Child 2.36"));
        DefaultMutableTreeNode child237 = new DefaultMutableTreeNode(new JiveCommunity(237, "Child 2.37"));
        DefaultMutableTreeNode child238 = new DefaultMutableTreeNode(new JiveCommunity(238, "Child 2.38"));
        DefaultMutableTreeNode child239 = new DefaultMutableTreeNode(new JiveCommunity(239, "Child 2.39"));
        DefaultMutableTreeNode child240 = new DefaultMutableTreeNode(new JiveCommunity(240, "Child 2.40"));
        DefaultMutableTreeNode child241 = new DefaultMutableTreeNode(new JiveCommunity(241, "Child 2.41"));
        DefaultMutableTreeNode child242 = new DefaultMutableTreeNode(new JiveCommunity(242, "Child 2.42"));
        DefaultMutableTreeNode child243 = new DefaultMutableTreeNode(new JiveCommunity(243, "Child 2.43"));
        DefaultMutableTreeNode child244 = new DefaultMutableTreeNode(new JiveCommunity(244, "Child 2.44"));
        DefaultMutableTreeNode child245 = new DefaultMutableTreeNode(new JiveCommunity(245, "Child 2.45"));
        DefaultMutableTreeNode child246 = new DefaultMutableTreeNode(new JiveCommunity(246, "Child 2.46"));
        DefaultMutableTreeNode child247 = new DefaultMutableTreeNode(new JiveCommunity(247, "Child 2.47"));
        DefaultMutableTreeNode child248 = new DefaultMutableTreeNode(new JiveCommunity(248, "Child 2.48"));
        DefaultMutableTreeNode child249 = new DefaultMutableTreeNode(new JiveCommunity(249, "Child 2.49"));
        DefaultMutableTreeNode child250 = new DefaultMutableTreeNode(new JiveCommunity(250, "Child 2.50"));
        DefaultMutableTreeNode child3 = new DefaultMutableTreeNode(new JiveCommunity(3, "Child 3"));
        DefaultMutableTreeNode child4 = new DefaultMutableTreeNode(new JiveCommunity(4, "Child 4 which has some punctuation characters: '\";/?\\[]{}"));
        DefaultMutableTreeNode child5 = new DefaultMutableTreeNode(new JiveCommunity(5, "Child 5 which has an extremely long name that is intended to test out how it'll appear in the Share UI"));
        DefaultMutableTreeNode child6 = new DefaultMutableTreeNode(new JiveCommunity(6, "Child 6 which has some Unicode characters: ¢€£¥©®™§¶†‡❦ठःअठी३"));

        // Now link all of the nodes together to create the tree structure
        root.add(child1);
        root.add(child2);
        root.add(child3);
        root.add(child4);
        root.add(child5);
        root.add(child6);
        
        child1.add(child11);
        child1.add(child12);
        child1.add(child13);
        
        child11.add(child111);
        child11.add(child112);
        child11.add(child113);
        
        child113.add(child1131);
        child1131.add(child11311);
        child11311.add(child113111);
        child113111.add(child1131111);
        child1131111.add(child11311111);
        
        child12.add(child121);
        child12.add(child122);
        
        child2.add(child21);
        child2.add(child22);
        child2.add(child23);
        child2.add(child24);
        child2.add(child25);
        child2.add(child26);
        child2.add(child27);
        child2.add(child28);
        child2.add(child29);
        child2.add(child210);
        child2.add(child211);
        child2.add(child212);
        child2.add(child213);
        child2.add(child214);
        child2.add(child215);
        child2.add(child216);
        child2.add(child217);
        child2.add(child218);
        child2.add(child219);
        child2.add(child220);
        child2.add(child221);
        child2.add(child222);
        child2.add(child223);
        child2.add(child224);
        child2.add(child225);
        child2.add(child226);
        child2.add(child227);
        child2.add(child228);
        child2.add(child229);
        child2.add(child230);
        child2.add(child231);
        child2.add(child232);
        child2.add(child233);
        child2.add(child234);
        child2.add(child235);
        child2.add(child236);
        child2.add(child237);
        child2.add(child238);
        child2.add(child239);
        child2.add(child240);
        child2.add(child241);
        child2.add(child242);
        child2.add(child243);
        child2.add(child244);
        child2.add(child245);
        child2.add(child246);
        child2.add(child247);
        child2.add(child248);
        child2.add(child249);
        child2.add(child250);
        
        result = new DefaultTreeModel(root);
        return(result);
    }
    
    
    /**
     * Alternative mock data that returns a tree of communities matched to the mocked up screen shots.
     * @return
     */
    private final TreeModel buildMockData2()
    {
        TreeModel result = null;
        
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JiveCommunity(0, "Alfresco Green Energy"));
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(new JiveCommunity(1, "Engineering"));
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(new JiveCommunity(2, "Finance"));
        DefaultMutableTreeNode child3 = new DefaultMutableTreeNode(new JiveCommunity(3, "Human Resources"));
        DefaultMutableTreeNode child31 = new DefaultMutableTreeNode(new JiveCommunity(31, "Archived Policies"));
        DefaultMutableTreeNode child32 = new DefaultMutableTreeNode(new JiveCommunity(32, "Company Policies"));
        DefaultMutableTreeNode child33 = new DefaultMutableTreeNode(new JiveCommunity(33, "Performance Appraisals"));
        DefaultMutableTreeNode child4 = new DefaultMutableTreeNode(new JiveCommunity(4, "Marketing"));
        
        // Now link all of the nodes together to create the tree structure
        root.add(child1);
        root.add(child2);
        root.add(child3);
        root.add(child4);
        
        child3.add(child31);
        child3.add(child32);
        child3.add(child33);
        
        result = new DefaultTreeModel(root);
        return(result);
    }
    

}
