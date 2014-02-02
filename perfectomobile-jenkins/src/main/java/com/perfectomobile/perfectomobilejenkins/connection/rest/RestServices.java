package com.perfectomobile.perfectomobilejenkins.connection.rest;

import hudson.ProxyConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.auth.AuthScope;

import jenkins.model.Jenkins;

import com.perfectomobile.perfectomobilejenkins.Constants;
import com.perfectomobile.perfectomobilejenkins.connection.Proxy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class RestServices {

	private static RestServices instance = null;
	private static boolean isDebug = Boolean.valueOf(System.getProperty("pmDebug"));
	private static PrintStream logger = null;

	protected RestServices() {
		// Exists only to defeat instantiation.
	}

	public static RestServices getInstance() {

		if (instance == null) {
			instance = new RestServices();
		}
		return instance;
	}
	
	public void setLogger(PrintStream logger){
		this.logger = logger;
	}
	

	/**
	 * Setup REST Client
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 */
	private WebResource getService(final String url, final String user,
			final String password) {

		
		Client client = createClient();
		client.addFilter(new HTTPBasicAuthFilter(user, password));
		WebResource service = client.resource(url);
		return service;
	}

	/**
	 * Get List of available devices.
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getHandsets(final String url, final String accessId,
			final String secretKey) throws IOException, ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = service.path("services")
				.path("handsets").queryParam("operation", "list")
				.queryParam("availableTo", accessId)
				.queryParam("user", accessId).queryParam("password", secretKey)
				.queryParam("inUse", "false").get(ClientResponse.class);

		return perfectoResponse;
	}

	/**
	 * Get list of available scripts Example:
	 * https://www.perfectomobile.com/services/repositories/scripts?
	 * operation=list
	 * &user=jenkins@perfectomobile.com&password=Perfecto1&responseFormat=xml
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getRepoScripts(final String url,
			final String accessId, final String secretKey) throws IOException,
			ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = service.path("services")
				.path("repositories").path("scripts")
				.queryParam("operation", "list").queryParam("user", accessId)
				.queryParam("password", secretKey)
				.queryParam("responseFormat", "xml").get(ClientResponse.class);

		return perfectoResponse;
	}

	/**
	 * Get script variables Example:
	 * https://www.perfectomobile.com/services/repositories
	 * /scripts/PRIVATE:variables.xml?
	 * operation=download&user=jenkins@perfectomobile.com&password=Perfecto1
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKeyprintRequest
	 * @param script
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getRepoScriptsItems(final String url,
			final String accessId, final String secretKey, final String script)
			throws IOException, ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = service.path("services")
				.path("repositories").path("scripts").path(script)
				.queryParam("operation", "download")
				.queryParam("user", accessId).queryParam("password", secretKey)
				.get(ClientResponse.class);

		return perfectoResponse;
	}
	
	/**
	 * Uploads the item specified by repositoryItemKey to the repository area specified by repository. 
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://www.perfectomobile.com/services/repositories/<media or datatables>/<PRIVATE:\myapps\TestApp.apk>?
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
	 */
	public ClientResponse uploadFile(final String url,
			final String accessId, 
			final String secretKey, 
			final String repository,
			final @PathParam("repositoryItemKey") String repositoryItemKey,
			final File fileName) throws IOException,
			ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = null;
		
		
		service = service
				.path("services")
				.path("repositories")
				.path(repository)
				.path(repositoryItemKey)
				.queryParam("operation", "upload")
				.queryParam("user", accessId)
				.queryParam("password", secretKey)
				.queryParam("overwrite", "true");
			
		printRequest(service);
		System.out.println(service.toString());
		
		InputStream fileInStream = new FileInputStream(fileName);
		String contentDisposition = "attachment; filename=\"" + fileName.getName()+"\"";
		
		perfectoResponse = service.type(MediaType.MULTIPART_FORM_DATA)
                .header("Content-Disposition", contentDisposition)
                .post(ClientResponse.class, fileInStream); 
		
		perfectoResponse = service.get(ClientResponse.class);
		
		return perfectoResponse;
	}
	
	public ClientResponse uploadFile2(final String url,
			final String accessId, final String secretKey, final String repository,
			final String repositoryItemKey) throws IOException,
			ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = null;
		
		
		service = service
				.path("services")
				.path("repositories")
				.path(repository)
				.path(repositoryItemKey)
				.queryParam("operation", "upload")
				.queryParam("user", accessId)
				.queryParam("password", secretKey)
				.queryParam("overwrite", "true");
		
		File fileName = new File("/home/guy/Pictures/Screenshot from 2013-12-21 14:02:41.png");
			
		InputStream fileInStream = new FileInputStream(fileName);
		String sContentDisposition = "attachment; filename=\"" + fileName.getName()+"\"";
		
		System.out.println(service);
		perfectoResponse = service.type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", sContentDisposition)
                .post(ClientResponse.class, fileInStream); 
		System.out.println("after upload");
		return perfectoResponse;
	}

	/**
	 * Starts a new asynchronous execution of the specified script and returns
	 * immediately with the response data. 
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/executions?
	 * operation=execute&
	 * scriptKey=value&user=value&password=value[&optionalParameter=value]
	 * 
	 * -Response: {"executionId":
	 * "jenkins@perfectomobile.com_variables_13-12-23_12_59_54_8082",
	 * "reportKey":"PRIVATE:variables_13-12-23_12_59_54_8082.xml"}
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00209/0
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param script
	 * @param optinalParameters
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse executeScript(final String url,
			final String accessId, final String secretKey, final String script,
			final String optinalParameters) throws IOException,
			ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = null;
		
		
		service = service
				.path("services")
				.path("executions")
				.queryParam("operation", "execute")
				.queryParam("scriptKey", script)
				.queryParam("user", accessId)
				.queryParam("password", secretKey)
				.queryParams(
						getQueryParamForOptinalParameters(optinalParameters));
			
		printRequest(service);
	
		perfectoResponse = service.get(ClientResponse.class);
		
		return perfectoResponse;
	}

	/**
	 * Gets the status of the running or recently completed execution
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/executions/<executionId>?
	 * operation=status&user=value&password=value
	 * 
	 * -Response:
	 * {"failedValidations":"0","flowEndCode":"Failed","reason":"ParseError"
	 * ,"status":"Completed", "description":"Completed","failedActions":"0",
	 * "executionId"
	 * :"jenkins@perfectomobile.com_variables_13-12-23_12_59_54_8082",
	 * "reportKey":"PRIVATE:variables_13-12-23_12_59_54_8082.xml",
	 * "completionDescription":"variable/parameter DUT has no value",
	 * "progressPercentage"
	 * :"0.0","user":"jenkins@perfectomobile.com","completed":"true"}
	 * 
	 * 
	 * @see http 
	 *      ://help.perfectomobile.com/article/AA-00303/51/Guides-Documentation
	 *      /HTTP-API/Operations/Script-Operations/02.-Get-Execution-Status.html
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param executionId
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getExecutionStatus(final String url,
			final String accessId, final String secretKey,
			final String executionId) throws IOException, ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = service.path("services")
				.path("executions").path(executionId)
				.queryParam("operation", "status").queryParam("user", accessId)
				.queryParam("password", secretKey).get(ClientResponse.class);

		return perfectoResponse;
	}

	/**
	 * Gets the status of the running or recently completed execution
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/reports/<reportKey>?
	 * operation=download&user=value&password=value
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00308/0
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param reportKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse downloadExecutionReport(final String url,
			final String accessId, final String secretKey,
			final String reportKey) throws IOException, ServletException {

		// setup REST-Client
		WebResource service = getService(url, accessId, secretKey);
		ClientResponse perfectoResponse = service.path("services")
				.path("reports").path(reportKey)
				.queryParam("operation", "download")
				.queryParam("user", accessId).queryParam("password", secretKey)
				.queryParam("format", "html").get(ClientResponse.class);

		return perfectoResponse;
	}

	/**
	 * This method gets parameters as they appears in Jenkins textarea field and
	 * returns Map object that holds parameter name and parameter value. Each
	 * parameter has a prefix of "param."
	 * 
	 * @param optinalParameters
	 * @return Map object that holds parameter name and parameter value. For
	 *         example "param.timeout=10"
	 */
	public MultivaluedMap<String, String> getQueryParamForOptinalParameters(
			String optinalParameters) {

		MultivaluedMap<String, String> scriptParams = new MultivaluedMapImpl();

		String paramName;
		String paramValue;

		if (!optinalParameters.trim().isEmpty()) {
			// Split to lines
			StringTokenizer stLines = new StringTokenizer(optinalParameters, System.getProperty("line.separator"));
			System.out.println(stLines.toString());
			while (stLines.hasMoreTokens()) {
				String paramLine = stLines.nextToken();
				System.out.println("paramLine=" + paramLine);
				// Split a line. get the name and the value.
				StringTokenizer stOneLine = new StringTokenizer(paramLine, "=");
				while (stOneLine.hasMoreTokens()) {
					paramName = Constants.PM_EXEC_PARAMETER_PREFIX
							+ stOneLine.nextToken();
					paramValue = stOneLine.nextToken();
					scriptParams.add(paramName, paramValue);
				}
			}

		}

		return scriptParams;
	}

	/**
	 * Set proxy if available
	 */
	public void setProxy() {

		if (Jenkins.getInstance() != null) {

			ProxyConfiguration proxy = Jenkins.getInstance().proxy;

			if (proxy != null) {
				System.out.println("proxy details:");
				System.out.println(proxy.getUserName());
				System.out.println(proxy.getPassword());
				System.out.println(proxy.name);
				System.out.println(proxy.port);
			} else {
				System.out.println("No proxy available");
			}
			/*
			 * ClientConfig cc = new ClientConfig();
			 * cc.property(ApacheClientProperties.PROXY_URI, "http://proxy");
			 * cc.property(ApacheClientProperties.PROXY_USERNAME, "user");
			 * cc.property(ApacheClientProperties.PROXY_PASSWORD, "pass");
			 * Client client = ClientBuilder.newClient(cc.connector(new
			 * ApacheConnector(cc.getConfiguration())));
			 */
		}
	}

	private Client createClient() {

		Client client = null;

		Proxy pmProxy = Proxy.getInstance();

		//Create client with proxy settings
		if (pmProxy.hasProxy()) {

			final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();

			config.getProperties().put(
					DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI,
					"http://" + pmProxy.getProxyHost() + ":"
							+ pmProxy.getProxyPort());

			if (pmProxy.hasCredentials()) {
				config.getState().setProxyCredentials(AuthScope.ANY_REALM,
						pmProxy.getProxyHost(), pmProxy.getProxyPort(),
						pmProxy.getProxyUser(), pmProxy.getProxyPassword());
			}

			client = ApacheHttpClient.create(config);
		}else{
			ClientConfig config = new DefaultClientConfig();
			client = Client.create(config);
		}
		return client;
	}
	
	private void printRequest(WebResource service){
		if (isDebug){
			logger.println(service.toString());
		}
	}
}
