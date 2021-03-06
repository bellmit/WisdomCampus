package com.guotop.palmschool.system.entity;


/**
 * 学生家长，教师导入类实体类
 * 
 * @author sheng
 */
public class Import
{
	//ID
	private Integer id;
	
	//文件名称
	private String filename;
	
	//导入类型  0:导入学生家长 1:导入教师
	private Integer type;
	
	//'总条数'
	private Integer total;
	
	//'成功导入条数',
	private Integer success;
	
	//'未成功导入条数'
	private Integer error;
	
	//'状态 0:未清空导入人员 1:已清空导入人员'
	private Integer status;
	
	private String updateTime;
	
	private String createTime;
	
	//'导入操作人员'
	private Integer importUserId;
	
	//导入人员姓名
	private String realName;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public Integer getType()
	{
		return type;
	}

	public void setType(Integer type)
	{
		this.type = type;
	}

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public Integer getSuccess()
	{
		return success;
	}

	public void setSuccess(Integer success)
	{
		this.success = success;
	}

	public Integer getError()
	{
		return error;
	}

	public void setError(Integer error)
	{
		this.error = error;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
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

	public Integer getImportUserId()
	{
		return importUserId;
	}

	public void setImportUserId(Integer importUserId)
	{
		this.importUserId = importUserId;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}
	
}
