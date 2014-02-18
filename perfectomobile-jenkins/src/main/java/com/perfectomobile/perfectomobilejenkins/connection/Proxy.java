package com.perfectomobile.perfectomobilejenkins.connection;

import java.io.PrintStream;

import com.perfectomobile.perfectomobilejenkins.Constants;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;

/**
 * Get Jenkins proxy details
 * @author Guy
 *
 */
public class Proxy {

	private  String proxyHost;
	private  int proxyPort;
	private  String proxyUser;
	private  String proxyPassword;
	private boolean isProxy;
	private static final boolean isDebug = Boolean.valueOf(System.getProperty(Constants.PM_DEBUG_MODE));
	
	private static Proxy instance = null;

	public static Proxy getInstance() {

		instance = new Proxy();

		return instance;
	}

	protected Proxy() {
		if (Jenkins.getInstance() != null) {

			if (Jenkins.getInstance() != null) {
				ProxyConfiguration proxy = Jenkins.getInstance().proxy;

				if (proxy != null) {
					proxyUser = proxy.getUserName();
					proxyPassword = proxy.getPassword();
					proxyHost = proxy.name;
					proxyPort = proxy.port;
					isProxy=true;
				}else{
					isProxy=false;
				}
			}
		}
	}

	public void print(PrintStream logger) {

		if (Jenkins.getInstance() != null && isDebug) {
			ProxyConfiguration proxy = Jenkins.getInstance().proxy;

			if (proxy != null) {

				logger.println("proxy details:");
				logger.println("proxyHost=" + proxyHost);
				logger.println("proxyPort=" + proxyPort);
				logger.println("proxyUser=" + proxyUser);
				logger.println("proxyPassword=" + proxyPassword);
			} else {
				logger.println("No proxy defined");
			}
		}
	}
	
	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}
	
	public boolean hasCredentials(){
		boolean hasCredentials = true;
		
		if (proxyUser == null || proxyPassword == null || proxyUser.isEmpty() && proxyPassword.isEmpty()) {
			hasCredentials = false;
		}
		
		return hasCredentials;
	}
	
	public boolean hasProxy(){
		return isProxy;
	}

}
