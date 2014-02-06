package com.perfectomobile.perfectomobilejenkins.connection.http;

import hudson.ProxyConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.ws.rs.PathParam;

import jenkins.model.Jenkins;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpServices {
	
	private static HttpServices instance = null;
	private static boolean isDebug = Boolean.valueOf(System.getProperty("pmDebug"));
	private static PrintStream logger = null;

	protected HttpServices() {
	}

	public static HttpServices getInstance() {

		if (instance == null) {
			instance = new HttpServices();
		}
		return instance;
	}
	
	public void setLogger(PrintStream logger){
		this.logger = logger;
	}
	
	private void printRequest(URI uri){
		if (isDebug){
			logger.println(uri);
		}
	}
	
	
	/**
	 * Set proxy on the client if available
	 */
	private void setProxy(HttpPost httpPost) {

		ProxyConfiguration proxy = Jenkins.getInstance().proxy;
		
		if (proxy != null) {
			HttpHost httpHost = new HttpHost(proxy.name, proxy.port);
			RequestConfig config = RequestConfig.custom()
		                .setProxy(httpHost)
		                .build();
				
			httpPost.setConfig(config);
		}
	}
	
	/**
	 * Uploads the item specified by repositoryItemKey to the repository area specified by repository. 
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://www.perfectomobile.com/services/repositories/<media or datatables>/<PRIVATE:/myapps/TestApp.apk>?
	 * operation=upload&user=value&password=value&overwrite=true
	 * 
	 * -Response: {"executionId":
	 * {"status":"success"}
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00311/53/Guides-Documentation/HTTP-API/Operations/Repository-Operations/02.-Upload-Item-to-Repository.html
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param repository
	 * @param repositoryItemKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 * @throws URISyntaxException 
	 */
	public HttpResponse uploadFile(final String url,
			final String accessId, 
			final String secretKey, 
			final String repository,
			final @PathParam("repositoryItemKey") String repositoryItemKey,
			final File fileName) throws URISyntaxException, IOException {
		
		
		String path = "/services/repositories/" + repository + "/" + repositoryItemKey; 
		String queryString = "?" + "operation=upload" + "&user=" + accessId + "&password=" + secretKey + "&overwrite=true";
		URI uri = new URI(url + path + queryString);
			
		printRequest(uri);
		
		HttpResponse response = sendRequest(uri, fileName);
        
        System.out.println(response.getStatusLine().getStatusCode());
        
        return response;
	}
	
	/**
	 * Call http client to upload a file
	 * @param uri
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	private HttpResponse sendRequest(URI uri, File file) throws IOException{
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("localhost", 8080),
                new UsernamePasswordCredentials("username", "password"));
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();

	    HttpPost httpPost = new HttpPost(uri);
	    
	    setProxy(httpPost);

	    InputStreamEntity reqEntity = null;
	    
		try {
			reqEntity = new InputStreamEntity(
			        new FileInputStream(file), -1);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    reqEntity.setContentType("binary/octet-stream");
	    reqEntity.setChunked(true); // Send in multiple parts if needed
	    httpPost.setEntity(reqEntity);
	    HttpResponse response = null;
	    
	    try {
			response = httpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            httpClient.close();
        }
	    
	    return response;
	}

}
