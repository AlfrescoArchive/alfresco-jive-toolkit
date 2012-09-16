Jive Toolkit
============

Description
-----------
The Jive Toolkit is divided up into a number of "components", organised into
folders as follows:

  common-components   - components that are "common" i.e. independent of both
                        Alfresco and Jive.  Some of these components end up
                        being installed into Alfresco, some into Jive, and
                        some into both systems.  All of them get built as
                        standard Java JAR files (libraries), and have no
                        dependencies on any Alfresco or Jive APIs.
                      
  alfresco-components - components that are dependent upon, and would need to
                        be deployed to, Alfresco.  These are packaged as AMP
                        files.  These have dependencies on Alfresco APIs.
  
  jive-components     - components that are dependent upon, and would need to
                        be deployed to, Jive.  This are packaged as standard
                        Jive plugins.  They have dependencies on Jive APIs.


Compiling the Toolkit
---------------------

Pre-requisites (one-time only steps):

1. Download and install a recent (2.1+ minimum) version of Apache Maven, from:
       http://maven.apache.org/
   Note: in the past there have been issues building Jive plugins using Maven
   3+ (see https://issues.alfresco.com/jira/browse/JIVE-41 for details), but
   in recent months they appear to have been resolved by Jive.  YMMV!

2. Follow the instructions at:
       https://svn.alfresco.com/repos/field/maven/README.txt
   Note: if you're unable to access this URL, it means you don't have access
   to the Alfresco "field SVN".  You may request access via Alfresco IT.
   
3. Merge the settings.xml.sample file into ~/.m2/settings.xml.  If you don't
   already have one you can simply copy it over verbatim (ensuring you drop
   the ".sample" suffix while doing so).
   
4. Open the ~/.m2/settings.xml and search for "####" (four hash characters).
   There should be 8 occurrences towards the end of the file - 2 login ids
   and 2 passwords.  Replace all of the text between and including the hash
   characters with your login and password details.
   
   For the Jive login and password, please contact Peter (pmonks@alfresco.com).
   
5. Check out the source code of the toolkit, from:
       https://svn.alfresco.com/repos/alfresco-enterprise/integrations/JiveToolkit/HEAD
   Note: if you're unable to access this URL, it means you don't have access
   to the Alfresco Enterprise SVN.  You may request access via Brian
   Remmington.


Building the AMPs and Jive plugin:

1. Run the provided build.sh script.
   Notes:
   * the generated AMP files and Jive plugin will be placed in the ./dist
     directory
   * there is currently no equivalent of the build.sh script for Windows, but
     the commands the build.sh script runs are relatively simple and should be
     trivial to manually execute (or better yet, script) on that platform

2. To clean all derived assets, run the provided clean.sh script.
   Notes:
   * this will also delete the Alfresco area in your local Maven repository
     (~/.m2/repository/org/alfresco), forcing it to be re-downloaded the next
     time a build is run.  Do not run this command if you don't have an
     internet connection - you will be unable to build the code.
   * there is currently no equivalent of the clean.sh script for Windows, but
     the commands the clean.sh script runs are relatively simple and should be
     trivial to manually execute (or better yet, script) on that platform

There is an outstanding enhancement request (JIVE-22) to create a master POM
that would replace both of these scripts with Maven equivalents.  See
https://issues.alfresco.com/jira/browse/JIVE-22 for more details.


Packaging the Toolkit (for distribution to QA etc.):

1. Run the provided package.sh script.
   Notes:
   * the generated zip filewill be placed in the ./dist directory
   * there is currently no equivalent of the package.sh script for Windows,
     but the commands the package.sh script runs are relatively simple and
     should be trivial to manually execute (or better yet, script) on that
     platform


Installing the Toolkit

See http://docs.alfresco.com/3.4/index.jsp?topic=%2Fcom.alfresco.Enterprise_3_4_0.doc%2Fconcepts%2Fjive-install-artifact.html
