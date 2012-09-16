

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
	<title>Test File Browser Widget</title>
	
	<!-- CSS -->
	<link rel="stylesheet" type="text/css" 
		href="<@s.url value='${themePath}/styles/fileChooser.css' />" />
	
	<!-- JavaScript -->
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.5.2.min.js"></script>
	<script type="text/javascript" 
		src="<@s.url value='/plugins/alfresco-connector/resources/script/fileChooser.js' />"></script>
	
	<script type="text/javascript">
		$(document).ready(function() {
			var url = "<@s.url action='getRemoteContainer' />";
			
			var fileChooser = $("#fileChooser").fileChooser(url)
				.setCallBackFunction(function(key) {
					alert("Key selected: " + key);
				})
				.construct();
		});
	</script>
</head>

<body>
	<!-- To be filled by JavaScript -->
	<div id="fileChooser">
	</div>
</body>

</html>