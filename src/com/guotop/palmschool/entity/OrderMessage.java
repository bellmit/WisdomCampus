package com.guotop.palmschool.entity;

import com.google.gson.annotations.Expose;

/**
 * 短信套餐实体类
 * 
 */
public class OrderMessage 
{
	/**
	 * ID
	 */
	@Expose
	private Integer productId;
	
	/**
	 * ID
	 */
	private Integer id;
	
	/**
	 * 名称
	 */
	@Expose
	private String name;

	/**
	 * 价格（servicePrice与cardPrice之和）
	 */
	@Expose
	private Double price;
	
	/**
	 * 有效开始时间
	 */
	@Expose
	private String startTime;
	
	/**
	 * 有效结束时间
	 */
	@Expose
	private String endTime;
	
	/**
	 * 类型0包时间段 1 单价/每条
	 */
	@Expose
	private Integer type;
	
	/**
	 * 状态 0可用 1失
	 * 该状态仅仅是用于是否能够在购买页面显示
	 */
	private Integer status;
	
	/**
	 * 创建时间
	 */
	private String createTime;
	
	/**
	 * 更新时间
	 */
	private String updateTime;
	
	/**
	 * 是否是虚拟套餐
	 * 0:是 1:否
	 */
	private String isVirtual;
	
	/**
	 * 套餐范围0:学校,1:个人
	 */
	private Integer rangeObject;
	
	/**
	 * 学生姓名
	 */
	private String realName;
	
	/**
	 * 学生id
	 */
	private Integer studentId;
	
	/**
	 * 学生班级
	 */
	private String clazzName;
	/**
	 * 班级
	 */
	private Integer clazzId;
	
	/**
	 * 学生信息(手机端使用)
	 */
	@Expose
	private User user;
	/**
	 * 是否是教师或教师子女（0：不是，1：是）
	 */
	@Expose
	private Integer isTeacher;

	/**
	 * 卡押金
	 */
	@Expose
	private Double cardPrice;
    
	/**
	 * 服务费
	 */
	@Expose
	private Double servicePrice;
	/**
	 *  服务费支付方式 0：学校统一缴费 1：线上缴费
	 */
	@Expose
	private Integer payType;

	/**
	 * 自定义属性
	 * @return
	 */
	/**
	 * 已缴费人数
	 */
	private Integer alreadyPayCount;
	
	/**
	 * 未缴费人数
	 */
	private Integer unPayCount;
	
	/**
	 * 免缴费人数
	 */
	private Integer freePayCount;
	
	/**
	 * 总学生人数
	 */
	private Integer studentCount;
	
	public Double getCardPrice() {
		return cardPrice;
	}

	public void setCardPrice(Double cardPrice) {
		if(cardPrice==null){
			cardPrice=0d;
		}
		this.cardPrice = cardPrice;
	}

	public Double getServicePrice() {
		return servicePrice;
	}

	public Integer getClazzId() {
		return clazzId;
	}

	public void setClazzId(Integer clazzId) {
		this.clazzId = clazzId;
	}

	public void setServicePrice(Double servicePrice) {
               if(servicePrice==null){
            	   servicePrice=0d;
               }
		this.servicePrice = servicePrice;
	}
	
	public Integer getIsTeacher() {
		return isTeacher;
	}

	public void setIsTeacher(Integer isTeacher) {
		this.isTeacher = isTeacher;
	}

	public Integer getProductId()
	{
		return productId;
	}

	public void setProductId(Integer productId)
	{
		this.productId = productId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndTime()
	{
		return endTime;
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	public Integer getType()
	{
		return type;
	}

	public void setType(Integer type)
	{
		this.type = type;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
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

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getIsVirtual()
	{
		return isVirtual;
	}

	public void setIsVirtual(String isVirtual)
	{
		this.isVirtual = isVirtual;
	}

	public Integer getRangeObject()
	{
		return rangeObject;
	}

	public void setRangeObject(Integer rangeObject)
	{
		this.rangeObject = rangeObject;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}

	public Integer getStudentId()
	{
		return studentId;
	}

	public void setStudentId(Integer studentId)
	{
		this.studentId = studentId;
	}

	public String getClazzName()
	{
		return clazzName;
	}

	public void setClazzName(String clazzName)
	{
		this.clazzName = clazzName;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Integer getAlreadyPayCount()
	{
		return alreadyPayCount;
	}

	public void setAlreadyPayCount(Integer alreadyPayCount)
	{
		this.alreadyPayCount = alreadyPayCount;
	}

	public Integer getUnPayCount()
	{
		return unPayCount;
	}

	public void setUnPayCount(Integer unPayCount)
	{
		this.unPayCount = unPayCount;
	}

	public Integer getFreePayCount()
	{
		return freePayCount;
	}

	public void setFreePayCount(Integer freePayCount)
	{
		this.freePayCount = freePayCount;
	}

	public Integer getStudentCount()
	{
		return studentCount;
	}

	public void setStudentCount(Integer studentCount)
	{
		this.studentCount = studentCount;
	}
	public Integer getPayType() {
		return payType;
	}

	public void setPayType(Integer payType) {
		this.payType = payType;
	}
}
