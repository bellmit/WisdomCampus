package com.guotop.palmschool.check.entity;


public class AttendanceCurrentDateInout 
{
	//打卡时间
	private String inoutTime;
	//考勤状态名称
	private String statusName;
	//考勤状态
	private Integer status;

	//备注,用于考勤补录
	private String remark;
	
	public String getInoutTime()
	{
		return inoutTime;
	}

	public void setInoutTime(String inoutTime)
	{
		this.inoutTime = inoutTime;
	}

	public String getStatusName()
	{
		return statusName;
	}

	public void setStatusName(String statusName)
	{
		this.statusName = statusName;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}
	
	
}