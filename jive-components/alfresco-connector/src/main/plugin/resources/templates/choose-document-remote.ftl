<#--
  - $Revision: 32834 $
  - $Date: 2006-08-02 15:56:32 -0700 (Wed, 02 Aug 2006) $
  -
  - Copyright (C) 1999-2008 Jive Software. All rights reserved.
  - This software is the proprietary information of Jive Software. Use is subject to license terms.
-->

<html>
<head>    
    <title>${title?html}</title>

    <meta name="nosidebar" content="true" />

	<!-- CSS -->
	<link rel="stylesheet" type="text/css" 
		href="<@s.url value='/plugins/alfresco-connector/resources/styles/fileChooser.css' />" />
    <link rel="stylesheet" type="text/css" media="all"
    	href="<@resource.url value='/styles/jive-content.css'/>" />
	
	<!-- JavaScript -->
	<script type="text/javascript" 
		src="<@s.url value='/plugins/alfresco-connector/resources/script/fileChooser.js' />"></script>
	
	<script type="text/javascript">
		$j(document).ready(function() {
			var url = "<@s.url action='getRemoteContainer' />";
			
			var fileChooser = $j("#jive-choose-container").fileChooser(url)
				.setFileSelectable(${(!upload)?string})
				.setFetchFiles(${(!upload)?string})
				.setDirectorySelectable(${upload?string})
				.setFetchDirectories(${upload?string})
				.setCallBackFunction(function(key) {
					$j("#jive-remote-container-id").val(key);
					$j("#choosejivecontainerform").submit();
				})
				.construct();
		});
	</script>
</head>
<body class="jive-body-formpage jive-body-choose-container">

<!-- BEGIN header & intro  -->
<header class="j-page-header">        
         <h2>${title?html}<span><@s.text name="global.colon"/> <@s.text name="document.choose.location.remote"/></span></h2>        
</header>
<!-- END header & intro -->



<!-- BEGIN main body -->
<div id="jive-body-main">


    <!-- BEGIN main body column -->
    <div id="jive-body-maincol-container">
        <div id="jive-body-maincol">

            <!-- BEGIN formblock -->
            <div class="j-box j-enhanced jive-box-form jive-standard-formblock-container">

                    <form action="<@s.url action='choose-document-remote'/>" id="choosejivecontainerform" name="jivechoosecontainerform" class="j-form">                        
                        
                        <input type="hidden" name="contentType" id="jive-content-type" value="${contentType?c}"/>
                        <input type="hidden" name="containerType" id="jive-container-type" value="${containerType?c}"/>
                        <input type="hidden" name="container" id="jive-container-id" value="${container.ID?c}"/>
                        
                        <input type="hidden" name="remoteDocument" id="jive-remote-container-id"/>
                        
                        <input type="hidden" name="upload" value="${upload?string}" />
                        <input type="hidden" name="managed" value="true"/>

                    </form>

                    <#--BEGIN doc create continue -->
                    <div id="jive-doc-create-continue" class="clearfix">
                        
                            <#if action.isBinaryBodyUploadCapable() && binaryEnabled && !tempObjectID?exists>
                                <hr noshade size="1"/>
                            </#if>

							<!-- Choose container -->
                            <div id="jive-choose-container" class="clearfix">
                            	
                            </div> <!-- #jive-choose-container -->           
                                  
                    </div> <!-- #jive-doc-create-continue -->


            </div>
            <!-- END formblock -->

                    
                    
            </div>
        </div>
        <!-- END main body column -->


    </div>
    <!-- END main body -->
</body>
</html>
