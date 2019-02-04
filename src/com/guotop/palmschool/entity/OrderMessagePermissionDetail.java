package com.guotop.palmschool.entity;


/**
 * 未购买服务费对应的菜单限制详情表
 * 
 */
public class OrderMessagePermissionDetail 
{
	/**
	 * 主键
	 */
	private Integer id;
	
	/**
	 * 菜单Id
	 */
	private String menuId;
	
	/**
	 * 班级ID
	 */
	private Integer clazzId;
	
	/**
	 * 年级ID
	 */
	private Integer gradeId;
	
	/**
	 * 年级名称
	 */
	private String gradeName;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getMenuId()
	{
		return menuId;
	}

	public void setMenuId(String menuId)
	{
		this.menuId = menuId;
	}

	public Integer getClazzId()
	{
		return clazzId;
	}

	public void setClazzId(Integer clazzId)
	{
		this.clazzId = clazzId;
	}

	public Integer getGradeId()
	{
		return gradeId;
	}

	public void setGradeId(Integer gradeId)
	{
		this.gradeId = gradeId;
	}

	public String getGradeName()
	{
		return gradeName;
	}

	public void setGradeName(String gradeName)
	{
		this.gradeName = gradeName;
	}
}