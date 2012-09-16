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

(function()
{
   /**
    * Socialize document.
    *
    * @method onJiveActionSocialize
    * @param asset {object} Object literal representing the file or folder to be actioned
    */
   Alfresco.doclib.Actions.prototype.onJiveActionSocialize = function dlA_onJiveActionSocialize(asset)
   {
      // Save clicked document
      Jive.module.jiveCommunityPickerAsset = asset;
      
      // Listen for when a community has been selected
      if (!Jive.module.jiveCommunityPicker)
      {
         // Create the community picker
         Jive.module.jiveCommunityPicker = new Jive.module.CommunityPicker(this.id + "-jiveCommunityPicker");
         YAHOO.Bubbling.on("dataItemSelected", function (layer, args)
         {
            // Get selected community and prepare the documents nodeRefs to be socialized
            var community = args[1].selectedItem.item,
               asset = Jive.module.jiveCommunityPickerAsset,
               nodeRefs = [];
            if (YAHOO.lang.isArray(asset))
            {
               for (var i = 0, il = asset.length; i < il; i++)
               {
                   nodeRefs.push(asset[i].nodeRef);
               }
            }
            else
            {
               nodeRefs.push(asset.nodeRef);
            }

            // Call repository to add the documents to jive
            Alfresco.util.Ajax.jsonPut(
            {
               url: Alfresco.constants.PROXY_URI + 'jive/node/community/' + encodeURIComponent(community.id) + '/socialize',
               dataObj:
               {
                  nodeRefs: nodeRefs
               },
               successMessage: this.msg("jive.actions.document.socialize.success"),
               failureMessage: this.msg("jive.actions.document.socialize.failure")
            });
         }, this);
      }

      // Display the community picker
      Jive.module.jiveCommunityPicker.showDialog();
   };
   
   /**
    * Augment prototypes with Actions module (we cannot use Bubbling as the listeners have not yet been defined - see https://issues.alfresco.com/jira/browse/CLOUD-437)
    */
   if (Alfresco.DocumentList)
   {
      YAHOO.lang.augmentProto(Alfresco.DocumentList, Alfresco.doclib.Actions);
   }
   if (Alfresco.DocumentActions)
   {
      YAHOO.lang.augmentProto(Alfresco.DocumentActions, Alfresco.doclib.Actions);
   }
   if (Alfresco.DocListToolbar)
   {
      YAHOO.lang.augmentProto(Alfresco.DocListToolbar, Alfresco.doclib.Actions);
   }
})();