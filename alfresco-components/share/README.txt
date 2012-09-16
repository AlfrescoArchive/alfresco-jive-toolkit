Jive Socialise for Alfresco Share
======================================

Author: Erik Winlšf, Will Abson

This extension extends the Alfresco Share application to support Socializing 
selected content items from a Document Library using Jive.

The add-on must be installed with the repository components, which are provided
in a separate WAR file.

Installation
------------

The extensions have been developed to install on top of an existing Alfresco
4.1 installation, via an AMP file.

After installing the AMP file, start up Alfresco and navigate to the Module
Management console at http://hostname/share/page/modules/deploy. Check that
the module 'Jive Socialize Action' is listed in the Deployed Modules section
(if not then you should enable it using the console).

Using the module
-----------------

Locate the document (or documents) that you wish to send to Jive within the
Share Document Library

  * For single documents, click the Socialize action for that item in the
    Document List view or the Document Details page
  
  * For multiple documents, select the items you wish to send to Jive using
    the checkboxes next to each item, then from the toolbar drop-down select
    Socialize.