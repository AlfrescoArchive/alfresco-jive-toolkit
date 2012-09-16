<#--
  ~ $Revision: 138063 $
  ~ $Date: 2011-02-10 16:00:01 -0800 (Thu, 10 Feb 2011) $
  ~
  ~ Copyright (C) 1999-2008 Jive Software. All rights reserved.
  ~
  ~ This software is the proprietary information of Jive Software. Use is subject to license terms.
  -->

<!-- BEGIN document -->

<div class="jive-content clearfix doc-page">
    <header>
        <h2><span class="jive-icon-big jive-icon-document"></span>${action.renderSubjectToText(document)}</h2>
        <div class="j-byline font-color-meta">
            <#assign docVersion = document.versionManager.getDocumentVersion(version)/>
            <@s.text name="doc.created_by.modified_by">
                <@s.param><#if !document.user.anonymous><@jive.userDisplayNameLink user=document.user/><#else><@s.text name="prsntr.created.guest.text"/></#if></@s.param>
                <@s.param>${document.creationDate?datetime?string.medium_short}</@s.param>
                <@s.param><#if docVersion.author?exists><@jive.userDisplayNameLink user=docVersion.author/><#else><@s.text name="prsntr.modified.guest.text" /></#if></@s.param>
                <@s.param>${docVersion.modificationDate?datetime?string.medium_short}</@s.param>
            </@s.text>

<#if (document.documentType.ID == 1)>
            <div class="j-version j-rc4">
                <#if (ActionUtils.previousVersionExists(document) && document.textBody)>
                <a href="<@s.url value='/docs/${document.documentID}/diff?secondVersionNumber=${document.documentVersion.versionNumber?c}'/>">
                    <span class="jive-icon-sml jive-icon-versions"></span><@s.text name="prsntr.default.version.label" /> ${version}
                </a>
                <#else>
                  
                <span class="jive-icon-sml jive-icon-versions"></span><@s.text name="prsntr.default.version.label" /> ${version}

                </#if>
            </div>
                </#if>
        </div>

        <#if action.isUserContainer() >
        <div class="jive-content-personal font-color-meta">
            <strong class="font-color-meta"><@s.text name="global.visibility"/><@s.text name="global.colon"/></strong>
            <#if action.getVisibilityPolicy(action.user, document) == enums['com.jivesoftware.visibility.VisibilityPolicy'].owner>
            <!-- visible only to you, the author -->
            <span>
                <img src="<@s.url value='/images/transparent.png' />" title="" class="jive-icon-sml jive-icon-bookmark-private"/>
                <@s.text name="doc.visibility.owner.radio"/>
            </span>
            </#if>
            <#if action.getVisibilityPolicy(action.user, document) == enums['com.jivesoftware.visibility.VisibilityPolicy'].open>
            <!-- visible to anyone -->
            <span>
                <@s.text name="doc.visibility.open.radio"/>
            </span>
            </#if>
            <#if action.getVisibilityPolicy(action.user, document) == enums['com.jivesoftware.visibility.VisibilityPolicy'].restricted>
            <#assign viewers = action.removeOwner(action.getViewers(document), document)>
                <span class="jive-shared-list jive-shared-list-short">
                <!-- visible to specific people -->
                    <img src="<@s.url value='/images/transparent.png' />" title="" class="jive-icon-sml jive-icon-content-private-shared"/>
                <#list viewers as user>
                <#if user != document.user>
                     <@jive.userDisplayNameLink user=user/><#if user_has_next>,</#if>
                <#if user_index == 4><#break></#if>
                </#if>
                </#list>
                    <#if (viewers.size() > 4)>
                        ... <!-- show max of 5, then do show/hide of rest -->
                    (<a href="#" class="jive-shared-list-toggle jiveSharedShowAll">show all</a>)
                    </#if>
                </span>
                <#if (viewers.size() > 4)>
                <span class="jive-shared-list jive-shared-list-all" style="display: none;">
                    <img src="<@s.url value='/images/transparent.png' />" title="" class="jive-icon-sml jive-icon-content-private-shared"/>
                    <#list viewers as user>
                    <#if user != document.user>
                    <@jive.userDisplayNameLink user=user/><#if user_has_next>,</#if>
                    </#if>
                    </#list>
                    &nbsp;(<a href="#" class="jive-shared-list-toggle jiveSharedShowLess">show less</a>)
                </span>
                </#if>
            </#if>
        </div>
        </#if>
        <span class="j-page-crease"></span>
    </header>


    <section class="jive-content-body">
        <#include "/template/docs/presenter/presenter-body.ftl" />
        <#include "/template/docs/presenter/presenter-attachments.ftl" />
    </section>

    <footer class="jive-content-footer clearfix font-color-meta">
        <@jive.displayViewCount viewCount=document.viewCount containerClass='jive-content-footer-item'/>

        <#include "/template/docs/presenter/presenter-tags.ftl" />

    </footer>
    <span class="j-doc-shadow j-left-doc-shadow"></span>
        <span class="j-doc-shadow j-right-doc-shadow"></span>
</div>


<!-- END document -->
