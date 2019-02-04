package com.guotop.palmschool.entity;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * 班级实体类
 * 
 * @author shengyinjiang
 */
public class Clazz
{
	/**
	 * ID
	 */
	private Integer id;

	/**
	 * 班级名称
	 */
	private String clazzName;

	/**
	 * 班主任id
	 */
	private Integer leaderId;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 班级创建时间
	 */
	private String createTime;

	/**
	 * 年级id
	 */
	private int gradeId;

	/**
	 * 班级编号
	 */
	private String code;

	/**
	 * ------------------------------------ 班主任姓名
	 */
	private String leaderName;

	/**
	 * 年级类型
	 */
	private String type;

	/**
	 * 年级名称
	 */
	private String gradeName;

	/**
	 * 入学年份
	 */
	private String createYear;

	private String phone;

	/**
	 * 班级ID
	 */
	@Expose
	private Integer clazzId;
	
	@Expose
	private String fullClazzName;
	
	private List<User> studentList;
	
	/**
	 * 福建资源云对应的clazzId
	 */
	private String fjClazzId;
	
	//福建资源云学校Id
	private String fjSchoolId;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		if(id!=null){
			setClazzId(id);
		}
		this.id = id;
	}

	public String getClazzName()
	{
		return clazzName;
	}

	public void setClazzName(String clazzName)
	{
		this.clazzName = clazzName;
	}

	public Integer getLeaderId()
	{
		return leaderId;
	}

	public void setLeaderId(Integer leaderId)
	{
		this.leaderId = leaderId;
	}

	public String getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(String updateTime)
	{
		this.updateTime = updateTime;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public int getGradeId()
	{
		return gradeId;
	}

	public void setGradeId(int gradeId)
	{
		this.gradeId = gradeId;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getLeaderName()
	{
		return leaderName;
	}

	public void setLeaderName(String leaderName)
	{
		this.leaderName = leaderName;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getGradeName()
	{
		return gradeName;
	}

	public void setGradeName(String gradeName)
	{
		this.gradeName = gradeName;
	}

	public List<User> getStudentList()
	{
		return studentList;
	}

	public void setStudentList(List<User> studentList)
	{
		this.studentList = studentList;
	}

	public String getCreateYear()
	{
		return createYear;
	}

	public void setCreateYear(String createYear)
	{
		this.createYear = createYear;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public Integer getClazzId()
	{
		return clazzId;
	}

	public void setClazzId(Integer clazzId)
	{
		this.clazzId = clazzId;
	}

	public String getFullClazzName()
	{
		return fullClazzName;
	}

	public void setFullClazzName(String fullClazzName)
	{
		this.fullClazzName = fullClazzName;
	}

	public String getFjClazzId()
	{
		return fjClazzId;
	}

	public void setFjClazzId(String fjClazzId)
	{
		this.fjClazzId = fjClazzId;
	}

	public String getFjSchoolId()
	{
		return fjSchoolId;
	}

	public void setFjSchoolId(String fjSchoolId)
	{
		this.fjSchoolId = fjSchoolId;
	}

}