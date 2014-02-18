package com.perfectomobile.perfectomobilejenkins.connection.rest;

import java.io.File;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.Test;

public class RestServicesTest {
	
	@Test
	public void testGetExecutionParametersWithoutParameters(){
		
		String params = "";
		
		MultivaluedMap <String, String> paramMap = RestServices.getInstance().getQueryParamForOptinalParameters(params);
		
	}
	
	@Test
	public void testGetExecutionParametersOneParameter(){
		
		String params = "parameter1(string)=dolphine";
		
		MultivaluedMap <String, String> paramMap = RestServices.getInstance().getQueryParamForOptinalParameters(params);
		
		System.out.println(paramMap.toString());
	}
	
	@Test
	public void testGetExecutionParametersMultiParameter(){
		
		String params = "parameter1(string)=Dolphine" + System.getProperty("line.separator") +
				"parameter2(string)=Rona" + System.getProperty("line.separator") +
				"parameter3(string)=Jonathan" + System.getProperty("line.separator") +
				"parameter4(string)=Empire" + System.getProperty("line.separator");
		
		MultivaluedMap <String, String> paramMap = RestServices.getInstance().getQueryParamForOptinalParameters(params);
		
		System.out.println(paramMap.toString());
	}

}
