package com.perfectomobile.perfectomobilejenkins.connection;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;

/**
 * 
 * @author Guy
 *
 */
public class Proxy {

	private  String proxyHost;
	private  int proxyPort;
	private  String proxyUser;
	private  String proxyPassword;
	private boolean isProxy;
	
	private static Proxy instance = null;

	public static Proxy getInstance() {

		//if (instance == null) {
			instance = new Proxy();
		//}
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

	public void print() {

		if (Jenkins.getInstance() != null) {
			ProxyConfiguration proxy = Jenkins.getInstance().proxy;

			if (proxy != null) {

				System.out.println("proxy details:");
				System.out.println("proxyHost=" + proxyHost);
				System.out.println("proxyPort=" + proxyPort);
				System.out.println("proxyUser=" + proxyUser);
				System.out.println("proxyPassword=" + proxyPassword);
			} else {
				System.out.println("No proxy defined");
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
		
		System.out.println("hasCredentials=" + hasCredentials);
		return hasCredentials;
	}
	
	public boolean hasProxy(){
		return isProxy;
	}

}
