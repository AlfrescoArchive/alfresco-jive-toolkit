<#assign isUserContainer = action.isUserContainer()>

<html>
<head>
    <#if (edit)>
        <title><@s.text name="doc.edit_document.title"><@s.param>${document.subject?html}</@s.param></@s.text></title>
    <#else>
        <title><@s.text name="doc.managed.upload.title" /></title>
    </#if>

    <meta name="nosidebar" content="true" />

    <#-- used for simple editor -->
    <@resource.javascript>
        var _editor_lang = "${displayLanguage}";
        var _jive_spell_check_enabled = "false";
        var _jive_tables_enabled = "false";
        var _jive_images_enabled = "false";
    </@resource.javascript>

    <#-- Include JavaScript Library and RTE -->
    <@resource.dwr file="WikiTextConverter" />
    <@resource.dwr file="Draft" />


    <@resource.javascript>
        function showTitleField(theTitle) {
            $j('#jive-upload-doc-title').show();
            theName = theTitle
            fileName = theName.match(/[^\/\\]+$/);
            //alert(fileName);
            $j("#subject01").attr('value',fileName);
        }
    </@resource.javascript>


    <@resource.javascript>
        var __postSubmitted = false;
        var cancelPost = false;

        <#-- preferredMode is defined in posteditor.js -->
        preferredMode = "${preferredEditorMode}";


        function validateUploadPost(isSubmit) {
            if (cancelPost) {
                return true;
            }

            var hasError = false;

            // verify that a subject has been provided
            var sub = document.getElementById('subject01');
            if (typeof(sub) != "undefined" && (sub.value == null || trimString(sub.value) == '')) {
                // display alert
                var t = document.getElementById('post-error-table');
                if (t) {
                    t.style.display = "block";
	                    scroll(0,0);
                    t = document.getElementById('post-error-subject');
                    if (t) {
                        t.style.display = "block";
	                    scroll(0,0);
                    }
                }
                hasError = true;
            }
            else {
                var t = document.getElementById('post-error-subject');
                if (t) {
                    t.style.display = "none";
                }
            }

            if (hasError) {
                return false;
            }

            // hide alert
            var t = document.getElementById('post-error-table');
            if (t) {
                t.style.display = "none";
            }

            if (arguments.length > 0) {
                window.onbeforeunload = null;
            }


            if (!__postSubmitted && isSubmit) {
                __postSubmitted = true;
                <#if !rteDisabledBrowser>
                var body = window.editor.get('wysiwygtext').getHTML();
                $j('#wysiwygtext').val(body);
                // safari 1.x and 2.x bug: http://lists.apple.com/archives/Web-dev/2005/Feb/msg00106.html
                if(window.editor.get('wysiwygtext').isTextOnly()){
                    $j('#wysiwygtext').show().html(body).hide();
                }
                </#if>
                return true;
            }
            else if (!isSubmit) {
                return true;
            }
            return false;
        }

        function setSubmitText() {
            var approversExist = ${approvalRequired?string};
            var submitText = '${action.getText('doc.create.sbmtForApprvl.button')?js_string}';
            var publishText = '${action.getText('doc.create.publish.button')?js_string}';

            var approversExist = $j('input[name="documentApprovers"]').val() != '';
            if (approversExist) {
                $j('#postButton').val(submitText);
            }
            else {
                $j('#postButton').val(publishText);
            }
        }
    </@resource.javascript>

    <@resource.javascript>
        function officepluginDownloadPopover() {


            $j("#jive-office-plugin-download").popover({
                context: $j("#jive-plugin-popup"),
                destroyOnClose: false
            });
                       
        }


    </@resource.javascript>

    <#if allowedToModifyApprovers>
         <#include "/template/docs/include/doc-javascript-collab.ftl" />
    </#if><#-- allowedToModifyApprovers -->

    <#if displayOfficePluginDownload || displayShortOfficePluginDownload>
        <#include "/template/conversion/office-plugin-download.ftl" />
    </#if><#-- officePluginDownloadEnabled -->



    

	<link rel="stylesheet" href="<@resource.url value='/styles/jive-compose.css'/>" type="text/css" media="all" />
	<link rel="stylesheet" href="<@resource.url value='/styles/jive-content.css'/>" type="text/css" media="all" />

    <#if legacyBreadcrumb>
    <content tag="breadcrumb">
        <@s.action name="legacy-breadcrumb" executeResult="true" ignoreContextParams="true">
        <@s.param name="containerType" value="${container.objectType?c}" />
        <@s.param name="container" value="${container.ID?c}" />
        </@s.action>

        <#if (edit)>
        <a href="<@s.url value='/docs/${document.documentID}' />">
            ${document.subject?html}
        </a>
        </#if>
    </content>
    </#if>

</head>
<body class="jive-body-formpage jive-body-formpage-document j-doc jive-body-formpage-doc-upload">

<!-- BEGIN main body -->
<div id="jive-body-main">

    <div class="jive-create-doc jive-create-large jive-content doc-page clearfix">

            <header>
              <h2>
                  <span class="jive-icon-big jive-icon-document"></span>
                  <#if edit>
                  <@s.text name="doc.edit_document.title"><@s.param>${document.subject?html}</@s.param></@s.text>
                  <#else>
                  <@s.text name="doc.upload.title" />
                  </#if>
                      <span class="details"><@s.text name="doc.create.in.label" />
                          <#if action.isUserContainer(container)>
                            <@s.text name="ctr.choose.myctr.document.header"/>
                          <#else>
                            <a href="<@s.url value='${JiveResourceResolver.getJiveObjectURL(container)}' includeParams='none'/>">${container.name?html}</a>
                          </#if>
                      </span>
            </h2>
              <#if referencedContentObject?exists>
              <@s.text name="doc.upload.thisDocWillRef.text" />
              <#if (referencedContentObject.getObjectType() == JiveConstants.DOCUMENT)>
              <@s.text name="global.document" />
              <#elseif (referencedContentObject.getObjectType() == JiveConstants.BLOGPOST)>
              <@s.text name="doc.upload.blog_post.text" />
              <#elseif (referencedContentObject.getObjectType() == JiveConstants.THREAD)>
              <@s.text name="doc.upload.thread.text" />
              </#if>
              '${referencedContentObject.subject?html}'
              </#if>
            <span class="j-page-crease"></span>
            </header>

            <#if (action.hasActionMessages())>
                <table class="jive-info-message" cellpadding="10" cellspacing="3" border="0" id="jive-info-message">
                <tr valign="top">
                    <td width="1%"><img src="<@s.url value='/images/warn-16x16.gif' />" width="16" height="16" border="0" alt="<@s.text name='global.info' />"></td>
                    <td width="99%">
                        <span class="jive-info-text">
                            <@s.iterator value="actionMessages">
                                <@s.property escape="false" /> <br />
                            </@s.iterator>
                        </span>
                    </td>
                </tr>
                </table><br />

            </#if>

        <div class="jive-error-message jive-error-box" id="post-error-table" style="display:none;">
            <div id="post-error-subject" style="display:none;">
                <span class="jive-icon-med jive-icon-redalert"></span>
                <@s.text name="post.err.pls_enter_uploadeddoc.text" />
            </div>
        </div>

        <script type="text/javascript">
            // dwr error handling i18n
            var errorHandlerMessage = "<@s.text name='post.dwr_error_message.text' />";

            function editorErrorHandler(message, exception) {
                $j('#dwr-error-text').html(errorHandlerMessage + " [" + message + "]");

                // Fade in and then fade out after 10 seconds.
                $j('#dwr-error-text').fadeIn(function() {
                    window.setTimeout(function() {
                        $j('#dwr-error-text').fadeOut();
                    }, 10000);
                });
            };

            DWREngine.setErrorHandler(editorErrorHandler);
        </script>

        <#include "/template/global/include/form-message.ftl"/>

        <#assign formAction><#if edit><@s.url action='doc-managed-upload-edit' /><#else><@s.url action='doc-managed-upload' /></#if></#assign>
        <#assign uploadInProgressLabel><@s.text name="doc.upload.in_progress.label"/></#assign>
        <#assign validateFunction>validateUploadPost</#assign>
        <@resource.javascript>var multi_selector = null;</@resource.javascript>
        <#include "/template/global/include/upload-progress.ftl"/>
        <form class="j-form" action="${formAction}" method="post" enctype="multipart/form-data" name="postform" id="postform" onsubmit="return validateUploadPost(true);">
        <input type="hidden" name="containerType" value="${container.objectType?c}" />
        <input type="hidden" name="container" value="${container.ID?c}" />
        <@jive.token name="document.upload.${document.documentID}" />
        <#if (document?exists)>
        <input type="hidden" name="edittedVersionID" value="${edittedVersion?c}" />
        <input type="hidden" name="startEditTime" value="${startEditTime?c}" />
        <input type="hidden" name="documentID" value="${document.documentID}" />
        </#if>


        <#if (authentication.anonymous)>
        <div id="jive-compose-anonymous" class="clearfix">
            <div class="clearfix">
                    <h4><label for="name01"><@s.text name="global.name" /><@s.text name="global.colon" /></label></h4>
                    <input type="text" name="name" size="30" maxlength="75"  id="name01" value="${name!?html}">

                    <h4><label for="email01"><@s.text name="global.email" /><@s.text name="global.colon" /></label></h4>
                    <input type="text" name="email" size="30" maxlength="75"  id="email01" value="${email!?html}">
            </div>
        </div>
        </#if>


        <div id="jive-post-bodybox">


            <#if (edit)>
            <div id="jive-post-title">
                <div class="jive-form-label">
                    <label for="subject01">
                        <@s.text name="global.title" />
                        <span class="jive-compose-directions font-color-meta-light"><@s.text name="doc.upload.title.info" /></span>
                    </label>
                </div>
                <input type="text" name="subject" size="75" maxlength="255" id="subject01"
                value="${subject?default('')?html}">
                <@macroFieldErrors name="subject" />
            </div>
            </#if>

            <div class="jive-upload-doc">
                <div class="jive-form-label">
                    <label for="attachFile_1">
                        <#if (edit)>
                        <@s.text name="doc.upload.upldNewVersion.label" />
                        <#else>
                        <@s.text name="doc.upload.upldNew.label" />
                        </#if>
                        <span id="jive-attach-maxsize" class="jive-compose-directions font-color-meta-light">
                        <@s.text name="attach.maxSize.text" /><@s.text name="global.colon" /> ${statics['com.jivesoftware.util.ByteFormat'].getInstance().formatKB(jiveContext.binaryBodyManager.maxBodySize)}
                        , <#if (jiveContext.binaryBodyManager.allowAllByDefault)><#if (!jiveContext.binaryBodyManager.disallowedTypes.empty)><@s.text name="doc.create.flTypesNotAllwd.text" /><@s.text name="global.colon" />&nbsp;<#list jiveContext.binaryBodyManager.disallowedTypesAsFileExtensions as extension>${extension?html}<#if extension_has_next>, </#if></#list><#else><@s.text name="attach.allTypesAllowed.text" /></#if><#else><@s.text name="doc.create.flTypesAllowed.text" /><@s.text name="global.colon" />&nbsp;<#if (!jiveContext.binaryBodyManager.allowedTypes.empty)><#list jiveContext.binaryBodyManager.allowedTypesAsFileExtensions as extension>${extension?html}<#if extension_has_next><@s.text name="global.comma" />&nbsp;</#if></#list><#else><@s.text name="doc.create.noFlTypesAllwd.text" /></#if></#if>
                        </span>
                    </label>
                </div>
                <div class="jive-upload-doc-select">
                    <input type="file" id="attachFile_1" <#if (!edit)>onChange="showTitleField(this.value);"</#if> name="uploadFile" />

                    <#if (!edit)>
                    <div id="jive-upload-doc-title" style="display: none">
                        <div class="jive-form-label">
                            <label for="subject01">
                                <@s.text name="global.title" />
                                <span class="jive-form-label-desc"><@s.text name="doc.upload.title.info" /></span>
                            </label>
                        </div>
                        <input type="text" name="subject" size="75" maxlength="255" id="subject01"
                        value="${subject?default('')?html}">
                    </div>
                    </#if>
                    <#if (edit)>
                    <div id="jive-compose-current-details">
                        <h6><@s.text name="doc.upload.curDocDetails.gtitle" /></h6>
                        <ul>
                            <li><strong><@s.text name="doc.upload.filename.radio" /></strong> <span class="${SkinUtils.getJiveObjectCss(document, 1)}"></span>${document.binaryBody.name}</li>
                            <li><strong><@s.text name="doc.upload.size.radio" /></strong> ${statics['com.jivesoftware.util.ByteFormat'].getInstance().format(document.binaryBody.size)}</li>
                            <li><strong><@s.text name="doc.upload.creation_date.radio" /></strong> ${document.documentVersion.creationDate?datetime?string.medium_short}</li>
                        </ul>
                    </div>
                    </#if>
                    <#if (displayOfficePluginDownload || displayShortOfficePluginDownload)>
                         <span id="jive-plugin-popup">
                            <a href="#" class="font-color-normal" onClick="officepluginDownloadPopover(); return false;"><@s.text name="officeintegration.plugin.sidebartitle" /></a>
                         </span>
                    </#if>
                </div>
            </div>


            <@macroFieldErrors name="uploadFile" />
            <@macroFieldErrors name="subject" />


            <#-- Div containing the body textarea and controls -->
            <div class="jive-editor-panel">
                <div class="jive-form-label">
                    <label for="description01">
                        <@s.text name="global.description" />
                        <span class="jive-compose-directions font-color-meta-light"><@s.text name="doc.upload.description.info" /></span>
                    </label>
                </div>
                <textarea id='wysiwygtext' name="description" rows="10" cols="30">${description!?html}</textarea>
                <@macroFieldErrors name="description" />
            </div>


            <br />



            <!-- BEGIN compose section -->
        <div class="jive-compose-section jive-compose-section-cats-tags clearfix">

                <#if !action.isUserContainer(container)>
                    <#assign objectTagSetIDs = action.getObjectTagSetIDs(document)>
                    <#include "/template/global/include/category.ftl" />
                </#if>

			    <@macroFieldErrors name="tags"/>

                <div id="jive-compose-tags">
                    <span id="jive-compose-tags-container">

                        <h4><label for="jive-tags"><span class="jive-icon-med jive-icon-tag"></span>
                            <@s.text name="doc.create.tags.gtitle" /></label>
                            <span id="tag_directions" class="jive-compose-directions font-color-meta-light"><@s.text name="doc.create.spaceSeprtsTags.text" /></span>
                        </h4>

                        <div id="jive-compose-tags-form">

                            <input type="text" name="tags" size="65" id="jive-tags"
                                <#if (draftEnabled)>onchange="autoSave.messageChangeHandler()"</#if>
                                value="${tags!?html}" />

                            <ul class="autocomplete" id="jive-tag-choices"></ul>

                            <#if (popularTags?size > 0)>
                                <div id="jive-populartags-container">
                                    <span>
                                        <strong><@s.text name="doc.create.popular_tags.gtitle" /><@s.text name="global.colon" /></strong>
                                        <#if container.objectType == JiveConstants.SOCIAL_GROUP>
                                            <@s.text name="document.editor.tags.group.popular.instructions" />
                                        <#elseif container.objectType == JiveConstants.PROJECT>
                                            <@s.text name="document.editor.tags.project.popular.instructions" />
                                        <#else>
                                            <@s.text name="document.editor.tags.popular.instructions" />
                                        </#if>
                                    </span>
                                    <div>
                                        <#list popularTags as tag>
                                            <a name="populartag" rel="nofollow" href="#" onclick="swapTag(this); <#if !action.isUserContainer(container)>TagSet.highlightCategory('${tag?js_string}');</#if> return false;"
                                            <#if (tags?exists && ((tags.indexOf(' ' + tag + ' ') > -1) || (tags.startsWith(tag + ' ')) || (tags.endsWith(' ' + tag))))>
                                                class="jive-tagname-${tag?html} jive-tag-selected"
                                            <#else>
                                                class="jive-tagname-${tag?html} jive-tag-unselected"
                                            </#if>
                                            >${action.renderTagToHtml(tag)}</a>&nbsp;
                                        </#list>
                                    </div>
                                </div>
                            </#if>
                        </div>

                        <!-- NOTE: this include MUST come after the 'tags' input element -->
                        <@resource.javascript file="/resources/scripts/tag-selector.js"/>
                    </span>
                </div>

            </div>
            <!-- END compose section -->


            <#include "/template/docs/include/doc-collab.ftl"/>


        </div>
        <!-- #jive-post-bodybox -->



       <!-- upload progress meter for attachments -->
        <div id="progressBar" style="display: none;">
            <div id="theMeter">
                <div id="progressBarText"></div>
                <div id="progressBarBox"><div id="progressBarBoxContent"></div></div>
            </div>
        </div>


        <div id="jive-compose-buttons">
            <div>
                <#if approvalRequired>
                <div id="approvers-text">
                <@s.text name='doc.create.approval.text' />
                <#list communityApprovers as communityApprover>
                <@jive.userDisplayNameLink user=communityApprover/>
                </#list>.
                </div>
                    <#assign publishValue = action.getText('doc.upload.sbmtForApprvl.button')/>
                <#else>
                    <#assign publishValue = action.getText('doc.upload.publish.button')/>
                </#if>
                <input type="submit"
                    name="doPost"
                    id="postButton"
                    value="${publishValue}"
                    style="font-weight: bold;"

                />
                <input type="submit" id="draftButton"
                    name="method:saveDraft"
                    <#--<#if (authorshipPolicy == statics['com.jivesoftware.community.Document'].AUTHORSHIP_OPEN)>disabled=true</#if>-->
                    value="<@s.text name='doc.upload.save_draft.button' />"

                />

            <#if (draftEnabled)>
                <input type="hidden" name="draftid" id="draftid" value="0" />
                <input type="submit"
                    name="method:cancel"
                    value="<@s.text name='global.cancel' />"
                    onclick="cancelPost = true; autoSave.doDiscard();"
                    >
            <#else>
                <input type="submit"
                    name="method:cancel"
                    value="<@s.text name='global.cancel' />"
                    onclick="cancelPost = true;"
                    >
            </#if>
                <div id="autosave" class="jive-description"></div>
            </div>

        </div>
            </form>



            <span class="j-doc-shadow j-left-doc-shadow"></span>
            <span class="j-doc-shadow j-right-doc-shadow"></span>
    </div>
    <#if ((document.ID > 0) && ((document.authorCommentDelegator.commentCount > 0) || (document.commentDelegator.commentCount > 0)))>
        <#include "/template/global/include/comment-macros.ftl" />
        <@comments contentObject=document isPrintPreview=fromPrintPreview/>
    </#if>

</div>
<!-- END main body -->


<content tag="jiveTooltip">
<div id="jiveTT-note-tags" class="jive-tooltip-help notedefault snp-mouseoffset" >
    <@s.text name="doc.upload.tag_explained.info"/>
</div>
</content>



<script type="text/javascript">

        function buildRTE(){
			<#assign toggleDisplay><@s.text name="rte.toggle_display" /></#assign>
			<#assign editDisabled><@s.text name="rte.edit.disabled" /></#assign>
            <#assign editDisabledSummary><@s.text name="rte.edit.disabled.desc" /></#assign>
			<#assign alwaysUse><@s.text name="post.alwaysUseThisEditor.tab" /></#assign>
            var rte = new jive.rte.RTEWrap({
                $element      : $j("#wysiwygtext"),
                controller    : jiveControl,
                preset        : "mini",
                autoSave      : window.autoSave,
                preferredMode : "${preferredEditorMode}",
                startMode     : "${preferredEditorMode}",
                mobileUI      : <#if rteDisabledBrowser>true<#else>false</#if>,
                toggleText    : '${toggleDisplay?js_string}',
                alwaysUseTabText  : '${alwaysUse?js_string}',
                editDisabledText : '${editDisabled?js_string}',
                editDisabledSummary : '${editDisabledSummary?js_string}',
                communityName : '${SkinUtils.getCommunityName()?js_string}',
                isEditing     : <#if (edit)>true<#else>false</#if>
            });
            $j('#documentApprovers').change(setSubmitText).keyup(setSubmitText).blur(setSubmitText).focus(setSubmitText);
        }
        $j(buildRTE);
    window.DWRTimeout = 20000;

</script>



</body>
</html>
