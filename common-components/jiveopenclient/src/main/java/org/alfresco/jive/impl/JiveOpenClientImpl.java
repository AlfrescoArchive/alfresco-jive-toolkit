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

package org.alfresco.jive.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.alfresco.util.encryption.Encrypter;
import org.alfresco.jive.AuthenticationException;
import org.alfresco.jive.CallFailedException;
import org.alfresco.jive.DocumentNotFoundException;
import org.alfresco.jive.DocumentSizeException;
import org.alfresco.jive.JiveOpenClient;
import org.alfresco.jive.ServiceUnavailableException;
import org.alfresco.jive.SpaceNotFoundException;


/**
 * This class is a generic client to the Jive OpenClient REST APIs.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @author Jared Ottley (jottley@alfresco.com)
 * @version $Id: JiveOpenClientImpl.java 41626 2012-09-14 23:59:00Z wabson $
 * @see org.alfresco.jive.JiveOpenClient
 *
 */
public class JiveOpenClientImpl
    implements JiveOpenClient
{
    private final static Log log = LogFactory.getLog(JiveOpenClientImpl.class);
    
    private final static String HEADER_NAME_USER_ID = "X-AlfrescoJive-UserId";
    
    private final static String OPENCLIENT_API_GET_SPACES      = "/rpc/rest/alfrescoService/alfresco/spaces";
    private final static String OPENCLIENT_API_GET_SUB_SPACES  = "/rpc/rest/alfrescoService/alfresco/spaces/{0}/children";     // {0} = space in which to enumerate child spaces
    private final static String OPENCLIENT_API_CREATE_DOCUMENT = "/rpc/rest/alfrescoService/alfresco/spaces/{0}/documents";  // {0} = parent space for the new document 
    private final static String OPENCLIENT_API_UPDATE_DOCUMENT = "/rpc/rest/alfrescoService/alfresco/documents";
    
    private final static String MIME_TYPE_JSON            = "application/json";
    private final static String MIME_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private final static String CHARSET_UTF8              = "UTF-8";
    private final static String USER_AGENT                = "Jive Open Client Java API v1.0";
    private final static int    DEFAULT_TIMEOUT_IN_MS     = 10000;  // 10 seconds
    
    private final static String JIVE_JSON_PREFIX = "throw 'allowIllegalResourceCall is false.';";
    
    private final static String JVM_PROPERTY_PROXY_HOST = "http.proxyHost";
    private final static String JVM_PROPERTY_PROXY_PORT = "http.proxyPort";
    
    private HttpClient httpClient  = null;
    private Encrypter  encrypter   = null;
    private URL        jiveUrl     = null;
    private String     userName    = null;
    private char[]     password    = null;
    private int        timeoutInMs = DEFAULT_TIMEOUT_IN_MS;
    
    
    public void setEncrypter(final Encrypter encrypter)
    {
        // PRECONDITIONS
        assert encrypter != null : "encrypter must not be null";
        
        // Body
        this.encrypter = encrypter;
    }
    
    
    public void setJiveUrl(final String jiveUrl)
        throws MalformedURLException
    {
        // PRECONDITIONS
        assert jiveUrl != null && jiveUrl.trim().length() > 0 : "jiveUrl cannot be null, empty or blank";
        
        // Body
        this.jiveUrl = new URL(jiveUrl);
    }
    
    
    public void setUserName(final String userName)
    {
        // PRECONDITIONS
        assert userName != null && userName.trim().length() > 0 : "userName cannot be null, empty or blank";
        
        // Body
        this.userName = userName;
    }
    
    
    public void setPassword(final char[] password)
    {
        this.password = password;
    }
    
    
    public void setTimeoutInMs(final int timeoutInMs)
    {
        // PRECONDITIONS
        assert timeoutInMs > 0 : "timeoutInMs must be > 0";
        
        // Body
        this.timeoutInMs = timeoutInMs;
    }

    
    public void init()
    {
        if (jiveUrl == null)
        {
            throw new IllegalStateException("jiveUrl has not been set.");
        }
        
        int port = jiveUrl.getPort() == -1 ? jiveUrl.getDefaultPort() : jiveUrl.getPort();
        
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getParams().setAuthenticationPreemptive(true);
        httpClient.getParams().setSoTimeout(timeoutInMs);
        httpClient.getParams().setConnectionManagerTimeout(timeoutInMs);
        httpClient.getState().setCredentials(
                                             new AuthScope(jiveUrl.getHost(), port),
                                             new UsernamePasswordCredentials(userName, String.valueOf(password))  // Security risk due to String interning...
                                            );
        
        // Set the proxy host and port, if specified.
        // Note: unclear why HTTPClient doesn't do this automatically...
        String proxyHost = System.getProperty(JVM_PROPERTY_PROXY_HOST);
        
        if (proxyHost != null && proxyHost.trim().length() > 0)
        {
            String proxyPortStr = System.getProperty(JVM_PROPERTY_PROXY_PORT);
            
            if (proxyPortStr != null && proxyPortStr.trim().length() > 0)
            {
                int proxyPort = -1;
                
                try
                {
                    proxyPort = Integer.valueOf(proxyPortStr.trim());
                    httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
                }
                catch (final NumberFormatException nfe)
                {
                    log.warn("Proxy configuration (" + proxyHost + ":" + proxyPortStr + ") is invalid. Skipping configuration of proxy server.");
                }
            }
            else
            {
                log.warn("Proxy configuration is missing the port number. Skipping configuration of proxy server.");
            }
        }
    }
    
    
    /**
     * @see org.alfresco.jive.JiveOpenClient#getSpaces(java.lang.String)
     */
    @Override
    public JSONObject getSpaces(final String userId)
        throws AuthenticationException,
        	   ServiceUnavailableException,
               CallFailedException
    {
        JSONObject result  = null;
        int        status  = -1;
        String     jsonStr = null;
        
        final GetMethod get = new GetMethod(buildUrl(OPENCLIENT_API_GET_SPACES));

        setCommonHeaders(userId, get);
        get.setRequestHeader("Content-Type", MIME_TYPE_JSON);
        
        try
        {
            status = callJive(get);

            if (status < 300)
            {
                jsonStr = get.getResponseBodyAsString();
                result  = parseJson(jsonStr);
            }
            else if (status == 401 || status == 403)
            {
                throw new AuthenticationException();
            }
            else if (status == 503)
            {
            	throw new ServiceUnavailableException();
            }
            else
            {
                throw new CallFailedException(status);
            }
        }
        catch (final JSONException je)
        {
            throw new CallFailedException("Unable to parse JSON:\n" + jsonStr, je);
        }
        catch (final HttpException he)
        {
            throw new CallFailedException(he);
        }
        catch (final IOException ioe)
        {
            throw new CallFailedException(ioe);
        }
        finally
        {
            get.releaseConnection();
        }
        
        return(result);
    }
    
    
    /**
     * @see org.alfresco.jive.JiveOpenClient#getSubSpaces(java.lang.String, long)
     */
    @Override
    public JSONObject getSubSpaces(final String userId, final long spaceId)
        throws AuthenticationException,
               CallFailedException,
               SpaceNotFoundException,
               ServiceUnavailableException
    {
        JSONObject      result      = null;
        int             status      = -1;
        String          jsonStr     = null;

        final String    resolvedUrl = OPENCLIENT_API_GET_SUB_SPACES.replace("{0}", String.valueOf(spaceId));
        final GetMethod get         = new GetMethod(buildUrl(resolvedUrl));
        
        setCommonHeaders(userId, get);
        get.setRequestHeader("Content-Type", MIME_TYPE_JSON);

        try
        {
            status = callJive(get);

            if (status < 300)
            {
                jsonStr = get.getResponseBodyAsString();
                result  = parseJson(jsonStr);
            }
            else if (status == 401 || status == 403)
            {
                throw new AuthenticationException();
            }
            else if (status == 404)
            {
                throw new SpaceNotFoundException(spaceId);
            }
            else if (status == 503)
            {
            	throw new ServiceUnavailableException();
            }
            else
            {
                throw new CallFailedException(status);
            }
        }
        catch (final JSONException je)
        {
            throw new CallFailedException("Unable to parse JSON:\n" + jsonStr, je);
        }
        catch (final HttpException he)
        {
            throw new CallFailedException(he);
        }
        catch (final IOException ioe)
        {
            throw new CallFailedException(ioe);
        }
        finally
        {
            get.releaseConnection();
        }
        
        return(result);
    }
    
    
    /**
     * @see org.alfresco.jive.JiveOpenClient#createDocument(java.lang.String, long, java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public void createDocument(final String userId,
                               final long   spaceId,
                               final String cmisId,
                               final String fileName,
                               final long   fileSize,
                               final String mimeType)
        throws AuthenticationException,
               CallFailedException,
               SpaceNotFoundException,
               ServiceUnavailableException,
               DocumentSizeException
    {
        final String          resolvedUrl = OPENCLIENT_API_CREATE_DOCUMENT.replace("{0}", String.valueOf(spaceId));
        final PostMethod      post        = new PostMethod(buildUrl(resolvedUrl));
        final NameValuePair[] body        = constructNameValuePairs(cmisId, fileName, fileSize, mimeType);
        int                   status      = -1;
        
        setCommonHeaders(userId, post);
        post.setRequestHeader("Content-Type", MIME_TYPE_FORM_URLENCODED);
        post.setRequestBody(body);
        
        try
        {
            status = callJive(post);

            if (status >= 400)
            {
                if (status == 401 || status == 403)
                {
                    throw new AuthenticationException();
                }
                else if (status == 404)
                {
                    throw new SpaceNotFoundException(spaceId);
                }
                else if (status == 409)
                {
                    throw new DocumentSizeException(fileName, fileSize);
                }
                else if (status == 503)
                {
                	throw new ServiceUnavailableException();
                }
                else
                {
                    throw new CallFailedException(status);
                }
            } 
            else if (status >= 300)
            {
            	log.warn("Status code: " + status + ". cmisObjectID: "+cmisId);
            }  
        }
        catch (final HttpException he)
        {
            throw new CallFailedException(he);
        }
        catch (final IOException ioe)
        {
            throw new CallFailedException(ioe);
        }
        finally
        {
            post.releaseConnection();
        }
    }


    /**
     * @see org.alfresco.jive.JiveOpenClient#updateDocument(java.lang.String, java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public void updateDocument(final String userId,
                               final String cmisId,
                               final String fileName,
                               final long   fileSize,
                               final String mimeType)
        throws AuthenticationException,
               CallFailedException,
               DocumentNotFoundException,
               ServiceUnavailableException,
               DocumentSizeException
    {
        final PutMethod     put    = new PutMethod(buildUrl(OPENCLIENT_API_UPDATE_DOCUMENT));
        final PostMethod    temp   = new PostMethod();
        int                 status = -1;

        setCommonHeaders(userId, put);
        put.setRequestHeader("Content-Type", MIME_TYPE_FORM_URLENCODED);

        // These shenanigans are required because PutMethod doesn't directly support content as NameValuePairs.
        temp.setRequestBody(constructNameValuePairs(cmisId, fileName, fileSize, mimeType));
        put.setRequestEntity(temp.getRequestEntity());
        
        try
        {
            status = callJive(put);

            if (status >= 400)
            {
                if (status == 401 || status == 403)
                {
                    throw new AuthenticationException();
                }
                else if (status == 404)
                {
                    throw new DocumentNotFoundException(cmisId);
                }
                else if (status == 409)
                {
                	throw new DocumentSizeException(fileName, fileSize);
                }
                else if (status == 503)
                {
                	throw new ServiceUnavailableException();
                }
                else
                {
                    throw new CallFailedException(status);
                }
            }
            else if (status >= 300)
            {
            	log.warn("Status code: " + status + ". cmisObjectID: "+cmisId);
            }
        }
        catch (final HttpException he)
        {
            throw new CallFailedException(he);
        }
        catch (final IOException ioe)
        {
            throw new CallFailedException(ioe);
        }
        finally
        {
            put.releaseConnection();
        }
    }
    
    
    
    /**
     * Sets default values for a bunch of standard request headers in the given <code>HttpMethod</code>.
     * Also sets the custom X-AlfrescoJive-UserId header to the encrypted user id.
     * 
     * @param userId The userId to encrypt and add to the header <i>(must not be null, empty or blank)</i>.
     * @param method The method to add the headers to <i>(may be null, in which case this method won't do anything)</i>.
     */
    private void setCommonHeaders(final String userId, final HttpMethod method)
    {
        if (userId == null || userId.trim().length() == 0)
        {
            throw new AuthenticationException();
        }
        
        if (method != null)
        {
            final String encryptedUserId = encrypter.encrypt(userId);
            
            method.setDoAuthentication(true);
            method.setFollowRedirects(false);
            method.setRequestHeader("Accept",            MIME_TYPE_JSON);
            method.setRequestHeader("Accept-Charset",    CHARSET_UTF8);
//            method.setRequestHeader("Accept-Encoding",   "gzip");   // Would dearly love to support compressed responses, but difficult to support in HttpClient 3.1 (used in Alfresco 3.4.x)
            method.setRequestHeader("Connection",        "Keep-Alive");
            method.setRequestHeader("Keep-Alive",        "300");
            method.setRequestHeader("User-Agent",        USER_AGENT);
            method.setRequestHeader(HEADER_NAME_USER_ID, encryptedUserId);
        }
    }

    
    /**
     * Jive returns JSON with a weird (and invalid, according to RFC-4627) prefix.
     * This method strips it off (if it exists) and parses the JSON.  
     * 
     * @param responseBody The JSON string to process <i>(may be null, empty or blank)</i>.
     * @return The "cleansed" response body <i>(will be null if the input was null)</i>.
     */
    private JSONObject parseJson(String jsonStr)
        throws JSONException
    {
        JSONObject result = null;
        
        if (jsonStr != null)
        {
            if (jsonStr != null && jsonStr.startsWith(JIVE_JSON_PREFIX))
            {
                jsonStr = jsonStr.substring(JIVE_JSON_PREFIX.length());
            }
            
            result = new JSONObject(new JSONTokener(jsonStr));
        }
        
        return(result);
    }

    
    /**
     * Builds a fully qualified Jive URL, using the specified path.
     *  
     * @param path The path to embed within the URL <i>(must not be null, empty or blank)</i>.
     * @return The fully qualified Jive URL <i>(will not be null)</i>.
     */
    private String buildUrl(final String path)
    {
        StringBuffer result     = new StringBuffer(128);
        String       jiveUrlStr = jiveUrl.toString();
        
        if (path == null || path.trim().length() == 0)
        {
            throw new IllegalArgumentException("path must not be null, empty or blank.");
        }
        
        boolean jiveUrlEndsWithSlash = jiveUrlStr.endsWith("/");
        boolean pathStartsWithSlash  = path.startsWith("/");
        
        if (jiveUrlEndsWithSlash && pathStartsWithSlash)
        {
            jiveUrlStr = jiveUrlStr.substring(0, jiveUrlStr.length() - 1);
        }
        else if (!jiveUrlEndsWithSlash && !pathStartsWithSlash)
        {
            jiveUrlStr = jiveUrlStr + "/";
        }
        
        result.append(jiveUrlStr);
        result.append(path);
        
        return(result.toString());
    }
    
    
    /**
     * Constructs an array of <code>NameValuePair</code>s from the given Jive parameters.
     * 
     * @param cmisId   The cmis:id to send to Jive <i>(must not be null, empty or blank)</i>.
     * @param fileName The fileName to send to Jive <i>(must not be null, empty or blank)</i>.
     * @param fileSize The fileSize to send to Jive <i>(must be >= 0)</i>.
     * @param mimeType The mimeType to send to Jive <i>(must not be null, empty or blank)</i>.
     * @return A populated array of <code>NameValuePair</code>s.
     */
    private NameValuePair[] constructNameValuePairs(final String cmisId,
                                                    final String fileName,
                                                    final long   fileSize,
                                                    final String mimeType)
    {
        // PRECONDITIONS
        assert cmisId   != null && cmisId.trim().length()   > 0 : "cmisId must not be null, empty or blank.";
        assert fileName != null && fileName.trim().length() > 0 : " fileName must not be null, empty or blank.";
        assert fileSize >= 0                                    : "fileSize must be greater than or equal to 0.";
        assert mimeType != null && mimeType.trim().length() > 0 : "mimeType must not be null, empty or blank.";
        
        // Body
        final NameValuePair[] result = { new NameValuePair("cmis:id",   cmisId),
                                         new NameValuePair("filename",  fileName),
                                         new NameValuePair("size",      String.valueOf(fileSize)),
                                         new NameValuePair("mime-type", mimeType) };
        return(result);
    }
    
    
    /**
     * Thin wrapper around httpClient.executeMethod that gives us a single place to log all requests, if need be.
     * 
     * @param method The method to execute <i>(must not be null)</i>.
     * @return The HTTP status code returned by the method.
     */
    private int callJive(final HttpMethod method)
        throws HttpException,
               IOException
    {
        return(callJive(method, null));
    }

    
    /**
     * Thin wrapper around httpClient.executeMethod that gives us a single place to log all requests, if need be.
     * 
     * @param method The method to execute <i>(must not be null)</i>.
     * @param body   The body being sent in the request of the method <i>(may be null)</i>. This is used for debugging purposes only.
     * @return The HTTP status code returned by the method.
     */
    private int callJive(final HttpMethod method, final String body)
        throws HttpException,
               IOException
    {
        int result = -1;
        
        if (log.isDebugEnabled())
            log.debug("About to call Jive: " + requestToString(method, body));
            
        result = httpClient.executeMethod(method);
        
        if (log.isDebugEnabled())
            log.debug("Response from Jive: " + responseToString(method));
        
        return(result);
    }

    
    /**
     * Debugging method for obtaining the state of a request as a String.
     * 
     * @param method The method to retrieve the request state from <i>(may be null)</i>.
     * @return The request state as a human-readable string value <i>(will not be null)</i>.
     */
    private String requestToString(final HttpMethod method, final String body)
    {
        StringBuffer result = new StringBuffer(128);
        
        if (method != null)
        {
            result.append("\n\tMethod: ");
            result.append(method.getName());
            result.append("\n\tURL: ");
            
            try
            {
                result.append(String.valueOf(method.getURI()));
            }
            catch (final URIException ue)
            {
                result.append("unknown, due to: " + ue.getMessage());
            }
            
            result.append("\n\tHeaders: ");
            
            for (final Header header : method.getRequestHeaders())
            {
                result.append("\n\t\t");
                result.append(header.getName());
                result.append(" : ");
                result.append(header.getValue());
            }
            
            result.append("\n\tAuthenticating? " + method.getDoAuthentication());
            
            if (body != null)
            {
                result.append("\n\tBody: ");
                result.append(body);
            }
        }
        else
        {
            result.append("(null)");
        }
        
        return(result.toString());
    }
    
    
    /**
     * Debugging method for obtaining the state of a response as a String.
     * 
     * @param method The method to retrieve the response state from <i>(may be null)</i>.
     * @return The response state as a human-readable string value <i>(will not be null)</i>.
     */
    private String responseToString(final HttpMethod method)
    {
        StringBuffer result = new StringBuffer(128);
        
        if (method != null)
        {
            result.append("\n\tStatus: ");
            result.append(method.getStatusCode());
            result.append(" ");
            result.append(method.getStatusText());
            
            result.append("\n\tHeaders: ");
            
            for (final Header header : method.getResponseHeaders())
            {
                result.append("\n\t\t");
                result.append(header.getName());
                result.append(" : ");
                result.append(header.getValue());
            }
            
            result.append("\n\tBody:");
            result.append("\n");
            
            try
            {
                result.append(method.getResponseBodyAsString());
            }
            catch (final IOException ioe)
            {
                result.append("unknown, due to: " + ioe.getMessage());
            }
        }
        else
        {
            result.append("(null)");
        }
        
        return(result.toString());
    }
    
}
