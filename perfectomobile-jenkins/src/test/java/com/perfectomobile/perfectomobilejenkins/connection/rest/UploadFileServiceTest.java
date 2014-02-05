package com.perfectomobile.perfectomobilejenkins.connection.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.http.HttpResponse;
import org.junit.Test;

import com.perfectomobile.perfectomobilejenkins.connection.http.HttpServices;

public class UploadFileServiceTest {

	@Test
	public void testuploadFile() throws FileNotFoundException, ParseException{
		
		HttpResponse perfectoResponse = null;
		File fileName = new File("/home/guy/Pictures/Screenshot from 2013-12-15 15:06:45.png");
		
		try {
			perfectoResponse = HttpServices.getInstance().uploadFile(
					"https://www.perfectomobile.com", 
					"jenkins@perfectomobile.com",
					"Perfecto1",
					"media", 
					"PRIVATE:/pictures/pic12.png",
					fileName);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
