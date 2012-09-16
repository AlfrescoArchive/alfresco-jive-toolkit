[#ftl]
<html>
<head>
  <title>Jive Communities</title>
  <link rel="stylesheet" href="/alfresco/css/main.css" type="text/css"/>
</head>
<body>
[#if subCommunities??]
  <p>
    <h1>Jive Sub-Communities:</h1>
    <ul>
  [#list subCommunities as subCommunity]
      <li>${subCommunity.name}</li>
  [/#list]
    </ul>
  </p>
[#else]
  <p>The current user does not have access to any Jive Sub-Communities.</p>
[/#if]
</body>
</html>
