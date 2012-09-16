[#ftl]
{
  "data" :
  [
[#if subCommunities??]
  [#list subCommunities as subCommunity]
    {
      "id"   : ${subCommunity.id?c},
      "name" : "${jsonUtils.encodeJSONString(subCommunity.name)}"
    }[#if subCommunity != subCommunities?last],[/#if]
  [/#list]
  ]
[/#if]
}