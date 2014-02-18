package com.perfectomobile.perfectomobilejenkins;

public class Constants {
	
	
	public static final int EXEC_STATUS_WAIT_TIME_IN_SECONDS=10;
	public static final int PM_RESPONSE_STATUS_SUCCESS=200;
	
	public static final String PM_EXEC_STATUS_COMPLETED="Completed";
	public static final String PM_EXEC_FLOW_END_CODE_SUCCESS="Success";
	
	//PM Response nodes
	public static final String PM_RESPONSE_NODE_STATUS="status";
	public static final String PM_RESPONSE_NODE_FLOW_END_CODE="flowEndCode";
	public static final String PM_RESPONSE_NODE_EXEC_ID="executionId";
	public static final String PM_RESPONSE_NODE_REPORT_KEY="reportKey";
	
	public static final String PM_EXEC_PARAMETER_PREFIX = "param.";
	
	public static final String PARAM_TYPE_START_TAG = "(";
	public static final String PARAM_TYPE_END_TAG = ")";
	public static final String PARAM_NAME_VALUE_SEPARATOR = "=";
	public static final String PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR = ";";
	public static final String PARAM_TYPE_MEDIA = "media";
	public static final String PARAM_TYPE_DATATABLES = "datatables";
	public static final int PARAM_TYPE_DATA_LENGTH = 5;
	
	
}
