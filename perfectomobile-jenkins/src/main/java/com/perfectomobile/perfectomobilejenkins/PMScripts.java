package com.perfectomobile.perfectomobilejenkins;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import com.perfectomobile.perfectomobilejenkins.connection.rest.RestServices;
import com.perfectomobile.perfectomobilejenkins.parser.xml.XmlParser;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Singelton which holds PM scripts
 * @author guy
 *
 */
public class PMScripts {

	private static PMScripts instance = null;
	private String[] scripts = null;

	protected PMScripts() {
		// Exists only to defeat instantiation.
	}

	public static PMScripts getInstance() {

		if (instance == null) {
			instance = new PMScripts();
		}
		return instance;
	}

	public String[] getAllScripts(String url, String accessId, String secretKey) {

		if (scripts == null) {

			ClientResponse perfectoResponse = null;
			try {
				perfectoResponse = RestServices.getInstance().getRepoScripts(
						url, accessId, secretKey);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			File resultFile = perfectoResponse.getEntity(File.class);

			scripts = XmlParser.getInstance()
					.getXmlElements(resultFile, XmlParser.ITEM_ELEMENT_NAME)
					.toArray(new String[0]);
		}

		return scripts;
	}
	
	

}
