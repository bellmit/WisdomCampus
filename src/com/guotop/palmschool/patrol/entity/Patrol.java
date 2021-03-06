package com.guotop.palmschool.patrol.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 校务巡查巡更设置实体
 * 
 * @author
 */

public class Patrol implements Serializable
{

	private static final long serialVersionUID = 1L;
	/**
	 * ID
	 */
	private Integer id;

	/**
	 * 用户id
	 */
	private Integer userId;

	/**
	 * 工号
	 */
	private String code;

	/**
	 * 用户姓名
	 */
	private String realName;

	/**
	 * 用户手机号
	 */
	private String phone;

	/**
	 * 部门Id
	 */
	private Integer departmentId;

	/**
	 * 用户所属部门名称
	 */
	private String departmentName;

	/**
	 * 巡查开始日期 YYYY-MM-DD
	 */
	private String startDate;

	/**
	 * 开始时间 YYYY-MM-DD HH:mm:ss
	 */
	private String startTime;

	/**
	 * 结束日期 YYYY-MM-DD
	 */
	private String endDate;

	/**
	 * 结束时间 YYYY-MM-DD HH:mm:ss
	 */
	private String endTime;

	/**
	 * 地点ID
	 */
	private Integer placeId;

	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 巡更点名称
	 */
	private String placeName;

	/**
	 * 巡查设置时间list
	 */
	private List<PatrolTime> timeList = new ArrayList<PatrolTime>();
	/**
	 * 巡更打开记录list
	 */
	private List<PatrolInout> inoutList = new ArrayList<PatrolInout>();

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Integer getUserId()
	{
		return userId;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}

	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public Integer getDepartmentId()
	{
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId)
	{
		this.departmentId = departmentId;
	}

	public String getDepartmentName()
	{
		return departmentName;
	}

	public void setDepartmentName(String departmentName)
	{
		this.departmentName = departmentName;
	}

	public String getStartDate()
	{
		return startDate;
	}

	public void setStartDate(String startDate)
	{
		this.startDate = startDate;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndDate()
	{
		return endDate;
	}

	public void setEndDate(String endDate)
	{
		this.endDate = endDate;
	}

	public String getEndTime()
	{
		return endTime;
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	public Integer getPlaceId()
	{
		return placeId;
	}

	public void setPlaceId(Integer placeId)
	{
		this.placeId = placeId;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public String getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(String updateTime)
	{
		this.updateTime = updateTime;
	}

	public String getPlaceName()
	{
		return placeName;
	}

	public void setPlaceName(String placeName)
	{
		this.placeName = placeName;
	}

	public List<PatrolTime> getTimeList()
	{
		return timeList;
	}

	public void setTimeList(List<PatrolTime> timeList)
	{
		this.timeList = timeList;
	}

	public List<PatrolInout> getInoutList()
	{
		return inoutList;
	}

	public void setInoutList(List<PatrolInout> inoutList)
	{
		this.inoutList = inoutList;
	}

}
