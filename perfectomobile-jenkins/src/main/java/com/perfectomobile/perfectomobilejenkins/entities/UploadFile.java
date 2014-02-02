package com.perfectomobile.perfectomobilejenkins.entities;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;

public class UploadFile implements Describable<UploadFile> {
	private String repository;
	private String filePath;
	private String repositoryItemKey;
	
	@DataBoundConstructor
	public UploadFile (String repository, String filePath, String repositoryItemKey){
		this.setRepository(repository);
		this.setFilePath(filePath);
		this.setRepositoryItemKey(repositoryItemKey);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRepositoryItemKey() {
		return repositoryItemKey;
	}

	public void setRepositoryItemKey(String repositoryItemKey) {
		this.repositoryItemKey = repositoryItemKey;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public Descriptor<UploadFile> getDescriptor() {
		 return Hudson.getInstance().getDescriptorOrDie(getClass());
	}
	
	

}
