package com.ibm;

public interface Constants {

	//列簇名
	public static final String FAMILY_NAME="FAMILY_NAME";
	
	//列名
	public static final String COL_NAME="COL_NAME";
	
	public static final String WRITE_HBASE_TYPE_NAME="WRITE_HBASE_TYPE_NAME";
	
	public static final int ANALOG_START_NO=352;
	public static final int ANALOG_END_NO=501;
	
	public static final int SWITCH_START_NO=2;//352;
	public static final int SWITCH_END_NO=351;//501;
	
	public static final String DELMIT_STRING=",";
	
	//间隔
	public static final String OUTPUT_INTERVAL_INT_NAME="OUTPUT_INTERVAL_INT_NAME";
	
	//模拟量排序列号
	public static final String ANALOG_SORT_NAME="ANALOG_SORT_NAME";
	
	//public static final int OUTPUT_START_NO=99;
	public static final String OUTPUT_START_NO_NAME="OUTPUT_START_NO_NAME";
	
	//是否写排序结果到文件
	public static final String WRITE_SORT_RESULT_NAME="WRITE_SORT_RESULT_NAME";
	
	//开始时间
	public static final String START_TIME_NAME="START_TIME_NAME";
		
	//结束时间
	public static final String END_TIME_NAME="END_TIME_NAME";	
}
