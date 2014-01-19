package com.perfectomobile.perfectomobilejenkins.service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.Secret;
import com.perfectomobile.perfectomobilejenkins.connection.rest.RestServices;
import com.perfectomobile.perfectomobilejenkins.parser.json.JsonParser;
import com.perfectomobile.perfectomobilejenkins.parser.xml.XmlParser;
import com.perfectomobile.perfectomobilejenkins.Constants;
import com.perfectomobile.perfectomobilejenkins.PerfectoMobileBuilder.DescriptorImpl;
import com.sun.jersey.api.client.ClientResponse;

/**
 * PM services during the execution of the script.
 * @author Guy Michaelis
 *
 */
public class PMExecutionServices {

	public static final int JOB_STATUS_RUNNING = 1;
	public static final int JOB_STATUS_FAILED = 2;
	public static final int JOB_STATUS_SUCCESS = 3;

	/**
	 * Get job status according to manipulation on PM get execution status response.
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
			// TODO Auto-generated catch block
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
	 * @param inputFile
	 * @return Map of parameter name and parameter type.
	 */
	public static Map <String, String> getScriptParameters(File inputFile) {

		Map<String, String> scriptParams = new LinkedHashMap<String, String>();

		String paramName;
		String paramType;

		NodeList nodeList = XmlParser.getInstance().getNodeList(inputFile,
				XmlParser.PARAMETER_ELEMENT_NAME);

		System.out.println("===============================================================");

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

			System.out.println("param name" + " : " + paramName + " / "
					+ "param type" + " : " + paramType);
			scriptParams.put(paramName, paramType);
		}

		return scriptParams;
	}

	/**
	 * Call PM to retrieve the execution report.
	 * @param descriptor
	 * @param build
	 * @param listener
	 * @param jsonExecutionResult PM response in json format
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
							Secret.toString(descriptor.getSecretKey()),
							reportKey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
			report = perfectoResponse.getEntity(File.class);
			
			EnvVars envVars = new EnvVars();
			try {
				envVars = build.getEnvironment(listener);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Put report under specific job
			String buildPath = System.getProperty("HUDSON_HOME") + 
					System.getProperty("file.separator") + 
					"jobs" +
					System.getProperty("file.separator") + 
					envVars.get("JOB_NAME") + 
					System.getProperty("file.separator") + 
					"builds" + 
					System.getProperty("file.separator") + 
					envVars.get("BUILD_NUMBER") + 
					System.getProperty("file.separator") + 
					"report.html";
			
			//System.out.println("buildPath=" + buildPath);
			System.out.println("WORKSPACE=" + envVars.get("WORKSPACE"));
			
			String reportName = envVars.get("WORKSPACE")+ System.getProperty("file.separator") + reportKey + ".html";
	        
			if(report.renameTo(new File(reportName))){
				listener.getLogger().println("move report into new location success");
			}else{
				listener.getLogger().println("move report fail");
			}
	 
		}

		return report;

	}
}
