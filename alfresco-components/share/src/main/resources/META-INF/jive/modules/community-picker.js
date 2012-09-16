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

if (typeof Jive == "undefined" || !Jive)
{
   var Jive = {};
}

/**
 * Jive top-level module namespace.
 *
 * @namespace Jive
 * @class Jive.module
 */
Jive.module = Jive.module || {};


/**
 * Property Picker.
 *
 * @namespace Jive.module
 * @class Jive.module.CommunityPicker
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /** Patch needed for Alfresco */
   var original_loadTreeNodes = Alfresco.module.DataPicker.prototype._loadTreeNodes;
		
   Jive.module.CommunityPicker = function(htmlId)
   {
      // Call super class constructor
      Jive.module.CommunityPicker.superclass.constructor.call(this, htmlId);

      // Merge options
      this.options = YAHOO.lang.merge(Alfresco.util.deepCopy(Jive.module.CommunityPicker.superclass.options,
      {
         copyFunctions: true
      }), this.options);

      // Re-register with our own name
      this.name = "Jive.module.CommunityPicker";
      Alfresco.util.ComponentManager.reregister(this);
      return this;
   };

   YAHOO.extend(Jive.module.CommunityPicker, Alfresco.module.DataPicker,
   {
		
      /**
       * Object container for initialization options
       */
      options:
      {
			/**
	       * The extra template to get transient properties and i18n messages
	       *
	       * @property propertyPickerTemplateUrl
	       * @type string
	       * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/property-picker"
	       */
	      communityPickerTemplateUrl: Alfresco.constants.URL_SERVICECONTEXT + "jive/modules/community-picker",

         /**
          * Default property tab configuration.
          * Other tabs may be added by overriding setOptions() and adding a new tab like "properties"
          * to the tabs option.
          *
          * @property tabs
          * @type object
          * @default A single tab with communities
          */
         tabs:
			[
				{
			   	id: "communities",
			    	treeNodes: 
			    	[
			      	{
			        		id: "jive",
			        		treeNodes:
			        		{
			          		url: "{url.proxy}jive/communities",
			          		id: "{node.id}",
								type: "community",
								label: "{node.name}",
								title: "{node.name}",
								path: "data",
								listItems:
								{
									url: "{url.proxy}jive/communities/{node.id}",
									id: "{item.id}",
									type: "community",
									label: "{item.name}",
									title: "{item.name}",
									path: "data"
								},
								treeNodes: 
								{
									url: "{url.proxy}jive/communities/{node.id}",
									id: "{node.id}",
									type: "community",
									label: "{node.name}",
									title: "{node.name}",
									path: "data"
								}								
							},
							listItems:
							{
								url: "{url.proxy}jive/communities",
								id: "{item.id}",
								type: "community",
								label: "{item.name}",
								title: "{item.name}",
								path: "data"
							}							                  
						}
					],
					listItems: []
				}
			]
      },


      /**
       * Event callback when superclass' dialog template has been loaded.
       *
       * @method onTemplateLoaded
       * @override
       * @param response {object} Server response from load template XHR request
       */
      onTemplateLoaded: function RPP_onTemplateLoaded(response)
      {
         // Load the UI template, which will bring in additional i18n-messages from the server
         Alfresco.util.Ajax.request(
         {
            url: this.options.communityPickerTemplateUrl,
            dataObj:
            {
               htmlid: this.id
            },
            successCallback:
            {
               fn: this.onCommunityPickerTemplateLoaded,
               obj: response,
               scope: this
            },
            failureMessage: this.msg("message.load.template.error", this.options.communityPickerTemplateUrl),
            execScripts: true
         });
      },

      /**
       * Event callback when this class' template has been loaded
       *
       * @method onCommunityPickerTemplateLoaded
       * @override
       * @param response {object} Server response from load template XHR request
       */
      onCommunityPickerTemplateLoaded: function PP_onCommunityPickerTemplateLoaded(response, superClassResponse)
      {
         // Inject the template from the XHR request into a new DIV element and insert it when showDialog() is called
         var tmpEl = document.createElement("div");
         tmpEl.setAttribute("style", "display:none");
         tmpEl.innerHTML = response.serverResponse.responseText;         

         // Let the original template get rendered.
         Jive.module.CommunityPicker.superclass.onTemplateLoaded.call(this, superClassResponse);

      },
		
		/**
		 * Override method so we can provide never ending hierarchiel child community navagation.
		 */
		_loadTreeNodes: function PP__loadTreeNodes(parent, parentNodeObj, treeNodeObjs, msgPath, yuiTreeCallback)
		{
			if (!treeNodeObjs)
			{
				// When data is about to be loaded: Pass in a url to get the child communities from
				treeNodeObjs = this.options.tabs[0].treeNodes;
			}
			else if (parentNodeObj != null && YAHOO.lang.isArray(treeNodeObjs))
			{
				// When data has been received: Pass in instructions how to parse data
				parentNodeObj = this.options.tabs[0].treeNodes[0];
				for (var i = 0, il = treeNodeObjs.length; i < il; i++)
				{
					treeNodeObjs[i].listItems = parentNodeObj.treeNodes.listItems;
					treeNodeObjs[i].treeNodes = parentNodeObj.treeNodes.treeNodes;
				}
			}			
			// Invoke the original load tree nodes method
			original_loadTreeNodes.call(this, parent, parentNodeObj, treeNodeObjs , msgPath, yuiTreeCallback);       
		},

      /**
       * Internal show dialog function
       * @method _showDialog
       * @protected
       */
      _showDialog: function PP__showDialog()
      {
         // Add class so we can override styles in css
         YAHOO.util.Dom.addClass(this.widgets.dialog.body.parentNode, "jive-community-picker");

         // Show dialog as usual
         Jive.module.CommunityPicker.superclass._showDialog.call(this);
      }
	});

   /* Dummy instance to load optional YUI components early */
   var dummyInstance = new Jive.module.CommunityPicker("null");
})();

