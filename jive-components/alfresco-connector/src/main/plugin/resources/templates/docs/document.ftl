<#--
  - $Revision: 28372 $
  - $Date: 2006-03-09 14:51:53 -0800 (Thu, 09 Mar 2006) $
  -
  - Copyright (C) 1999-2008 Jive Software. All rights reserved.
  - This software is the proprietary information of Jive Software.  Use is subject to license terms.
-->

<#include "/template/docs/include/doc-macros.ftl" />
<#assign fromPrintPreview = (decorator?? && decorator == 'print')/>

<html>
<head> 	
    <title>${action.renderSubjectToText(document)}</title>

	<link rel="stylesheet" href="<@resource.url value='/styles/jive-content.css'/>" type="text/css" media="all" />
	<link rel="stylesheet" href="<@resource.url value='/styles/tiny_mce3/themes/advanced/skins/default/content.css'/>" type="text/css" media="all" />

    <#if (FeedUtils.isFeedsEnabled())>
        <link rel="alternate" type="${FeedUtils.getFeedType()}"
            title="${document.subject?html} <@s.text name='doc.main.vrsnHistFeed.tooltip' />"
            href="<@s.url value="/community/feeds/document-history/${document.documentID}"/>" />

        <link rel="alternate" type="${FeedUtils.getFeedType()}"
            title="${document.subject?html} <@s.text name='doc.main.commentsFeed.tooltip' />"
            href="<@s.url value="/community/feeds/document-comments/${document.documentID}"/>" />
    </#if>

    <#if legacyBreadcrumb>
    <content tag="breadcrumb">
        <@s.action name="legacy-breadcrumb" executeResult="true" ignoreContextParams="true">
            <@s.param name="container" value="${container.ID?c}" />
            <@s.param name="containerType" value="${container.objectType?c}" />
        </@s.action>
        <a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}'/>/content?filterID=content~objecttype~objecttype[document]"><@s.text name="doc.main.brdcrmb.documents.link" /></a>
    </content>
    </#if>

    <#if fromPrintPreview>

        <#include "/template/decorator/default/header-clickjacking-prevent.ftl" />

        <#include "/template/decorator/default/header-javascript.ftl" />

        <@resource.javascript id="core" output="true" />
        <#list jiveContext.getSpringBean("javascriptURLConfigurator").pluginJavascriptSrcURLs as src >
            <script type="text/javascript" src="<@s.url value='${src}' />" ></script>
        </#list>

        <@resource.javascript id="rte" output="true" />
        <@resource.javascript output="true" />
    </#if>

	<@resource.template file="/soy/share/share.soy" />
   	<@resource.javascript file="/resources/scripts/apps/share/views/share_view.js" />
    <@resource.javascript file="/resources/scripts/apps/share/models/share_source.js" />
    <@resource.javascript file="/resources/scripts/apps/share/main.js" />   

    <@resource.template file="/soy/groups/membership.soy" />
    <@resource.javascript file="/resources/scripts/apps/socialgroup/membership/views/membership_view.js" />
    <@resource.javascript file="/resources/scripts/apps/socialgroup/membership/models/membership_source.js" />
    <@resource.javascript file="/resources/scripts/apps/socialgroup/membership/main.js" />

    <@resource.template file="/resources/scripts/apps/email_notification/main.js" />

    <@resource.template file="/soy/nav/movecontent.soy" />
    <@resource.template file="/soy/nav/modalizer.soy" />

    <@resource.javascript>

        $j(function() {
			$j('.jive-shared-list-toggle').click(function() {
				$j('.jive-shared-list-short').toggle();
				$j('.jive-shared-list-all').toggle();
                                return false;
			});
        });

        <#if followable>
            <#assign startFollowText><@s.text name="doc.startFollow.desc" /></#assign>
            <#assign stopFollowText><@s.text name="doc.stopFollow.desc" /></#assign>
            <#assign followError><@s.text name='global.follow.error'/></#assign>
            var i18n = {startFollowing : '${startFollowText?js_string}',
                        stopFollowing  : '${stopFollowText?js_string}',
                        followError    : '${followError?js_string}'}

            var jiveFollow = new jive.FollowApp.Main({objectType: 102, objectID:${document.ID?c}, featureName:'document', i18n:i18n});
        </#if>

        <#if trackable>
            var jiveTracking = new jive.TrackingApp.Main({objectType: 102, objectID:${document.ID?c}, featureName:'document', i18n:null});
        </#if>
        
        var jiveShare = new jive.ShareApp.Main({objectType: 102, objectID:${document.ID?c}, linkId:'jive-link-document-sendnotify'});
        var jiveMembership = new jive.MembershipApp.Main({objectID:${container.ID?c}});
        var jiveMoveContent = new jive.Move.Content.Main({
            objectType: ${document.objectType?c},
            objectID: ${document.ID?c},
            personalContainerTitleKey:'nav.bar.create.personal_container.title.document',
            personalContainerCaptionKey:'nav.bar.create.personal_container.caption.document',
            searchPlaceholderKey:'place.picker.move.search.document',
            containerID: ${container.ID?c},
            containerType: ${container.objectType?c}});
        var jiveReportAbuse = new jive.Modalizer.Main({triggers:['#jive-link-abuse a'],liveTriggers:['.js-link-abuse'], width: 'medium'});
        $j(function() { new jive.EmailNotification.Main(${document.ID?c}, ${document.objectType?c}); });
    </@resource.javascript>


    <#assign docRollbackConfirmText>
        <@s.text name="doc.rollback.confirm.text" />
    </#assign>
    <#assign docDeleteConfirmDelDocText>
        <@s.text name="doc.delete.confirm_del_doc.text" />
    </#assign>
    <script type="text/javascript">
        var documentID = ${document.ID?c};

        function publishDraft() {
            var form = $j('#documentPublishForm');
            jive.util.securedForm(form).always(function() {
                form.submit();
            });
        }

        function restoreVersion(version) {
           if (confirm('${docRollbackConfirmText?js_string}')){
                var form = $j('#documentRestoreForm');
                jive.util.securedForm(form).always(function() {
                    form.submit();
                });
           }
        }

    </script>

    <!-- BEGIN action bar, only for non-binary documents -->
    <#if !document.binaryBody??>
        <#include "/template/global/include/action-bar-macros.ftl" />
        <@renderActionBar document />
    </#if>
    <!-- END action bar -->

</head>

<!--featured content macro-->
<@jive.featureContentObject objectType=document.objectType?c objectID=document.ID?c containerType=container.objectType?c containerID=container.ID?c/>

<body class="jive-body-content j-doc">

    <form id="documentRestoreForm" method="post" action='<@s.url value="/docs/${document.documentID}/restore" />'>
        <input type="hidden" name="version" value="${version}" />
        <@jive.token name="document.restore.${document.documentID}" lazy=true />
    </form>

    <form id="documentPublishForm" method="post" action='<@s.url action="doc-publish" />'>
        <@jive.token name="document.publish.${document.documentID}" lazy=true />
        <input type="hidden" name="document" value="${document.documentID}" />
    </form>

    <#if !legacyBreadcrumb>
    <!-- BEGIN header & intro  -->
    <header id="jive-body-intro">
        <div class="j-context">
        <#if action.isUserContainer() >
            <@s.text name='doc.main.up_to_user_docs_in.link'>
                <@s.param><a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}'/>/content?filterID=content~objecttype~objecttype[document]"></@s.param>
                <@s.param></a></@s.param>
                <@s.param><a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}'/>"><@jive.displayUserDisplayName user=document.user/></a></@s.param>
            </@s.text>
        <#else>
            <@s.text name='doc.main.up_to_docs_in.link'>
                <@s.param><a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}'/>/content?filterID=content~objecttype~objecttype[document]"></@s.param>
                <@s.param></a></@s.param>
                <@s.param><a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}'/>"><span class="${SkinUtils.getJiveObjectCss(container, 1)}"></span>${container.name?html}</a></@s.param>
            </@s.text>
            <@s.action name="context-menu" executeResult="true" ignoreContextParams="true">
                <@s.param name="container" value="${container.ID?c}" />
                <@s.param name="containerType" value="${container.objectType?c}" />
            </@s.action>
        </#if>
        </div>
    </header>
    <!-- END header & intro -->
    </#if>

    <!-- BEGIN main body -->
    <div class="jive-wiki-post-moderating jive-content-header-moderating">
        <span class="jive-icon-med jive-icon-moderation"></span><@s.text name="mod.post_in_moderation.text" />
    </div>
    <div id="jive-body-main" class="j-layout j-layout-ls clearfix">
        <div class="j-column-wrap-l">
            <div class="j-column j-column-l lg-margin">
                    <#if (action.isUserContainer()) &&  (!SkinUtils.isPersonalContentEnabled(document.objectType))>
                    <div class="jive-warn-box">
                        <div>
                            <span class="jive-icon-med jive-icon-warn"></span>
                            <@s.text name="doc.main.personalDisabled.text" />
                        </div>
                    </div>
                    </#if>

                    <div id="object-follow-notify" class="jive-info-box" style="display:none"></div>

                    <div id="content-featured-notify" class="jive-info-box" style="display:none"></div>

                    <@jive.showMovedMesage content=document container=container/>

                    <#include "/template/global/include/form-message.ftl"/>

                    <#include "/template/docs/include/doc-needs-approval.ftl" />
    
                    <#include "/template/docs/doc-state.ftl" />

                    <#include "/template/docs/doc-upload-message.ftl" />

                    <#include "/plugins/alfresco-connector/resources/templates/docs/presenter/presenter-default.ftl" />

                        <@jive.ratings container document "doc"/>

                    <#include "/template/global/include/comment-macros.ftl" />
                    <@comments contentObject=document isPrintPreview=fromPrintPreview/>

                    <@docDeletePrompt doc=document/>
            </div>
        </div>



        <!-- BEGIN sidebar column -->
        <div id="jive-body-sidebarcol-container" class="j-column j-column-s j-content-extras">

            <#include "/template/docs/doc-sidebar.ftl" />

        </div>
        <!-- END sidebar column -->


    </div>

</body>
</html>
