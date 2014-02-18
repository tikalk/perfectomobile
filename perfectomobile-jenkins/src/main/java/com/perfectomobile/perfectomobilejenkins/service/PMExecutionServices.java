package com.perfectomobile.perfectomobilejenkins.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hudson.EnvVars;
import hudson.console.HyperlinkNote;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.Secret;

import com.perfectomobile.perfectomobilejenkins.connection.http.HttpServices;
import com.perfectomobile.perfectomobilejenkins.connection.rest.RestServices;
import com.perfectomobile.perfectomobilejenkins.entities.UploadFile;
import com.perfectomobile.perfectomobilejenkins.parser.json.JsonParser;
import com.perfectomobile.perfectomobilejenkins.parser.xml.XmlParser;
import com.perfectomobile.perfectomobilejenkins.Constants;
import com.perfectomobile.perfectomobilejenkins.PerfectoMobileBuilder.DescriptorImpl;
import com.sun.jersey.api.client.ClientResponse;

/**
 * PM services during the execution of the script.
 * 
 * @author Guy Michaelis
 * 
 */
public class PMExecutionServices {

	public static final int JOB_STATUS_RUNNING = 1;
	public static final int JOB_STATUS_FAILED = 2;
	public static final int JOB_STATUS_SUCCESS = 3;

	/**
	 * Get job status according to manipulation on PM get execution status
	 * response.
	 * 
	 * @param perfectoResponse
	 * @param listener
	 * @return
	 */
	public static int getJobStatus(String perfectoResponse,
			BuildListener listener) {

		String executionStatus = null;
		String flowEndCode = null;
		int returnStatus = JOB_STATUS_RUNNING;

		listener.getLogger().println(perfectoResponse);

		try {
			executionStatus = JsonParser.getInstance().getElement(
					perfectoResponse, Constants.PM_RESPONSE_NODE_STATUS);
			flowEndCode = JsonParser.getInstance().getElement(perfectoResponse,
					Constants.PM_RESPONSE_NODE_FLOW_END_CODE);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		listener.getLogger().println(
				Constants.PM_RESPONSE_NODE_STATUS + "=" + executionStatus);
		listener.getLogger().println(
				Constants.PM_RESPONSE_NODE_FLOW_END_CODE + "=" + flowEndCode);

		if (executionStatus.equals(Constants.PM_EXEC_STATUS_COMPLETED)) {
			if (flowEndCode.equals(Constants.PM_EXEC_FLOW_END_CODE_SUCCESS)) {
				returnStatus = JOB_STATUS_SUCCESS;
			} else {
				returnStatus = JOB_STATUS_FAILED;
			}
		}

		return returnStatus;

	}

	/**
	 * Get script parameters from the file returned by PM cloud
	 * 
	 * @param inputFile
	 * @return Map of parameter name and parameter type.
	 */
	public static Map<String, String> getScriptParameters(File inputFile) {

		Map<String, String> scriptParams = new LinkedHashMap<String, String>();

		String paramName;
		String paramType;

		NodeList nodeList = XmlParser.getInstance().getNodeList(inputFile,
				XmlParser.PARAMETER_ELEMENT_NAME);


		// do this the old way, because nodeList is not iterable
		for (int itr = 0; itr < nodeList.getLength(); itr++) {

			// Get parameter element
			Node parameterNode = nodeList.item(itr);
			Element parameterElement = (Element) parameterNode;

			// Get data element which holds the parameter type
			NodeList dataList = parameterElement
					.getElementsByTagName(XmlParser.DATA_ELEMENT_NAME);
			Node dataNode = dataList.item(0);
			Element dataElement = (Element) dataNode;
			paramType = dataElement.getAttribute(XmlParser.CLASS_ATT_NAME);

			// Get name element which holds the parameter name
			NodeList nameList = dataElement
					.getElementsByTagName(XmlParser.NAME_ELEMENT_NAME);
			Node nameNode = nameList.item(0);
			Element nameElement = (Element) nameNode;
			paramName = nameElement.getTextContent();

			scriptParams.put(paramName, paramType);
		}

		return scriptParams;
	}

	/**
	 * Call PM to retrieve the execution report.
	 * 
	 * @param descriptor
	 * @param build
	 * @param listener
	 * @param jsonExecutionResult
	 *            PM response in json format
	 * @return the execution report
	 */
	public static File getExecutionReport(DescriptorImpl descriptor,
			AbstractBuild build, BuildListener listener,
			String jsonExecutionResult) {

		String reportKey = null;
		File report = null;
		ClientResponse perfectoResponse = null;

		try {
			reportKey = JsonParser.getInstance().getElement(
					jsonExecutionResult, Constants.PM_RESPONSE_NODE_REPORT_KEY);

			listener.getLogger().println(
					Constants.PM_RESPONSE_NODE_REPORT_KEY + "=" + reportKey);

			listener.getLogger().println(
					"Calling PM cloud to get execution report ...");

			perfectoResponse = RestServices.getInstance()
					.downloadExecutionReport(descriptor.getUrl(),
							descriptor.getAccessId(),
							descriptor.getSecretKey(),
							reportKey);
		} catch (IOException e) {
			listener.getLogger().println(e.toString());
			return null;
		} catch (ServletException e) {
			listener.getLogger().println(e.toString());
			return null;
		} catch (ParseException e) {
			listener.getLogger().println(e.toString());
			return null;
		}

		//
		if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
			report = perfectoResponse.getEntity(File.class);
			
			EnvVars envVars = new EnvVars();
			try {
				envVars = build.getEnvironment(listener);
			} catch (IOException e) {
				listener.getLogger().println(e.toString());
				return null;
			} catch (InterruptedException e) {
				listener.getLogger().println(e.toString());
				return null;
			}
			
			
			//replace colon with underscore because it is forbiden in windows
			reportKey = reportKey.replace(":", "_");
			
			
			String reportName = envVars.get("WORKSPACE")
					+ System.getProperty("file.separator") + reportKey
					+ ".html";
			
			listener.getLogger().println(
					HyperlinkNote.encodeTo(envVars.get("JOB_URL") + "/ws/" + 
							reportKey + ".html", "Show report"));
			
			try {
				FileUtils.moveFile(report, new File(reportName));
			} catch (IOException e) {
				listener.getLogger().println(e.toString());
				return null;
			}


		}else{
			listener.getLogger().println("WARNING: Can't show report. PM returned status " + perfectoResponse.getStatus());
		}

		return report;

	}

	/**
	 * Call PM to upload files to the repository.
	 * 
	 * @param descriptor
	 * @param build
	 * @param listener
	 * @param uploadFiles
	 *            files to upload
	 * @return the execution report
	 */
	public static void uploadFiles(DescriptorImpl descriptor,
			AbstractBuild build, BuildListener listener,
			String textAreaParameters) {

		HttpResponse perfectoResponse = null;
		List<UploadFile> uploadFiles = new ArrayList<UploadFile>();

		
		String paramName;
		String paramValue;
		String parameTypeWithEndTag;
		String parameType;
		String repositoryKey;
		String filePath;

		if (!textAreaParameters.trim().isEmpty()) {
			// Split to lines
			StringTokenizer stParameters = new StringTokenizer(textAreaParameters, System.getProperty("line.separator"));
			while (stParameters.hasMoreTokens()) {
				String parameter = stParameters.nextToken();
				// Split a line. get the name and the value.
				StringTokenizer stParamToken = new StringTokenizer(parameter, Constants.PARAM_NAME_VALUE_SEPARATOR);
				while (stParamToken.hasMoreTokens()) {
					String paramWithType = stParamToken.nextToken();
					StringTokenizer stParamWithType = new StringTokenizer(paramWithType, Constants.PARAM_TYPE_START_TAG);
					paramName = stParamWithType.nextToken();
					parameTypeWithEndTag = stParamWithType.nextToken();
					parameType = parameTypeWithEndTag.substring(0, parameTypeWithEndTag.length()- Constants.PARAM_TYPE_END_TAG.length());
					paramValue = stParamToken.nextToken();
					if (parameType.equals(Constants.PARAM_TYPE_MEDIA) || parameType.equals(Constants.PARAM_TYPE_DATATABLES)){
						StringTokenizer stParamValue = new StringTokenizer(paramValue, Constants.PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR);
						repositoryKey = stParamValue.nextToken();
						filePath = stParamValue.nextToken();
						UploadFile uploadFile = new UploadFile(parameType, filePath, repositoryKey);
						uploadFiles.add(uploadFile);
					}
				}
			}

		}

		if (uploadFiles.size() > 0) {

			for (UploadFile uploadFile : uploadFiles) {

				try {
					// Print upload details
					listener.getLogger()
							.println(
									"Calling PM cloud to upload files into repository:");
					listener.getLogger().println(
							"Repository = " + uploadFile.getRepository());
					listener.getLogger().println(
							"Repository Item Key = "
									+ uploadFile.getRepositoryItemKey());
					listener.getLogger().println(
							"File path = " + uploadFile.getFilePath());

					// Call PM to upload the files
					HttpServices.getInstance().setLogger(listener.getLogger());
					perfectoResponse = HttpServices.getInstance().uploadFile(
							descriptor.getUrl(), descriptor.getAccessId(),
							descriptor.getSecretKey(),
							uploadFile.getRepository(),
							uploadFile.getRepositoryItemKey(),
							new File(uploadFile.getFilePath()));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
