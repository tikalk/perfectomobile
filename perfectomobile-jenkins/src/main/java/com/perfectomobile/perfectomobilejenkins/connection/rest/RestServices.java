package com.perfectomobile.perfectomobilejenkins.connection.rest;

import java.io.IOException;
import javax.servlet.ServletException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RestServices {
	
	
	private static WebResource getService(final String url, final String user,
            final String password) {
    // setup REST-Client
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    client.addFilter( new HTTPBasicAuthFilter(user, password )); 
    WebResource service = client.resource( url );
    return service;
    }
	
	/**
	 * Get List of available devices.
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getHandsets(final String url,
    		final String accessId,
            final String secretKey) throws IOException, ServletException {
        
    	// setup REST-Client
    	WebResource service = getService(url, accessId, secretKey );
        ClientResponse perfectoResponse = service.path("services").path("handsets").
    			queryParam("operation", "list").
    			queryParam("availableTo", accessId).
    			queryParam("user", accessId).
    			queryParam("password", secretKey).
    			queryParam("inUse", "false").
    			get(ClientResponse.class);
        
        return perfectoResponse;
    }
	
	/**
	 * Get list of available scripts
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public static ClientResponse getRepoScripts(final String url,
    		final String accessId,
            final String secretKey) throws IOException, ServletException {
        
    	// setup REST-Client
    	WebResource service = getService(url, accessId, secretKey );
        ClientResponse perfectoResponse = service.path("services").path("repositories").path("scripts").
    			queryParam("operation", "list").
    			queryParam("user", accessId).
    			queryParam("password", secretKey).
    			queryParam("responseFormat", "xml").
    			get(ClientResponse.class);
        
        return perfectoResponse;
    }
	
	/**
	 * Get script variables
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param script
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public static ClientResponse getRepoScriptsItems(final String url,
    		final String accessId,
            final String secretKey,
            final String script) throws IOException, ServletException {
        
    	// setup REST-Client
    	WebResource service = getService(url, accessId, secretKey );
        ClientResponse perfectoResponse = service.path("services").path("repositories").path("scripts").path(script).
    			queryParam("operation", "download").
    			queryParam("user", accessId).
    			queryParam("password", secretKey).
    			get(ClientResponse.class);
        
        return perfectoResponse;
    }

}
