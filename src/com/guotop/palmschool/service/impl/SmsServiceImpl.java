package com.guotop.palmschool.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.entity.AuthCodeEmail;
import com.guotop.palmschool.entity.Sms;
import com.guotop.palmschool.entity.SmsDetail;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.service.BaseService;
import com.guotop.palmschool.service.SmsService;
import com.guotop.palmschool.util.Pages;
import com.guotop.palmschool.util.TimeUtil;

/**
 * 短信业务类实现类
 * 
 * @author zhou
 * 
 */
@Service("smsService")
public class SmsServiceImpl extends BaseService implements SmsService
{

	@Resource
	private CommonService commonService;

	/**
	 * 加载短信列表信息
	 * 
	 * @param pageSize
	 * @param page
	 * @param paramMap
	 *            参数列表
	 * @return 根据不同权限加载短信列表数据(分页) 20151203
	 */
	@SuppressWarnings("unchecked")
	public Pages loadSmsList(int pageSize, int page, Map<String, Object> paramMap, User user)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;

		List<Sms> list = new ArrayList<Sms>();

		/*
		 * 校长,学校管理员,懂事长（全校所有人）
		 */
		if (commonService.hasAdminPermission(user))
		{

			allRow = (Integer) this.getBaseDao().selectObjectByObject("Sms.loadSmsCountAsMaster", paramMap);

			totalPage = Pages.countTotalPage(pageSize, allRow);

			final int offset = Pages.countOffset(pageSize, page);
			final int length = pageSize;
			currentPage = Pages.countCurrentPage(page);

			// 解决ibatis框架的分页问题
			// 起始数据坐标
			paramMap.put("startIndex", offset);
			// 单页数据量
			paramMap.put("length", length);
			list = this.getBaseDao().selectListByObject("Sms.loadSmsListAsMaster", paramMap);

		}

		/*
		 * 其他人只能看到自己发送的信息
		 */
		else
		{
			allRow = (Integer) this.getBaseDao().selectObjectByObject("Sms.loadSmsCountAsSelf", paramMap);

			totalPage = Pages.countTotalPage(pageSize, allRow);

			final int offset = Pages.countOffset(pageSize, page);
			final int length = pageSize;
			currentPage = Pages.countCurrentPage(page);

			// 解决ibatis框架的分页问题
			// 起始数据坐标
			paramMap.put("startIndex", offset);
			// 单页数据量
			paramMap.put("length", length);
			list = this.getBaseDao().selectListByObject("Sms.loadSmsListAsSelf", paramMap);
		}

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;

	}

	/**
	 * 加载平台库发送短信列表信息
	 * 
	 * @param pageSize
	 * @param page
	 * @param paramMap
	 *            参数列表
	 * @return 根据不同权限加载短信列表数据(分页) 20160120
	 */
	@SuppressWarnings("unchecked")
	public Pages loadSmsListFromPlatform(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;

		List<Sms> list = new ArrayList<Sms>();

		allRow = (Integer) this.getBaseDao().selectObjectByObject("Sms.loadSmsCountAsSelfFromPlatform", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);

		// 解决ibatis框架的分页问题
		// 起始数据坐标
		paramMap.put("startIndex", offset);
		// 单页数据量
		paramMap.put("length", length);
		list = this.getBaseDao().selectListByObject("Sms.loadSmsListAsSelfFromPlatform", paramMap);

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;

	}

	/**
	 * 根据senderId查询详细信息
	 * 
	 * tao 20151204
	 */
	@SuppressWarnings("unchecked")
	public List<SmsDetail> loadSmsDetail(Integer id)
	{
		return this.getBaseDao().selectListByObject("SmsDetail.loadSmsDetail", id);
	}
	/**
	 * 根据senderId查询详细信息
	 * 
	 * sheng 20160120
	 */
	@SuppressWarnings("unchecked")
	public List<SmsDetail> loadSmsDetailFromPlatform(Integer id)
	{
		return this.getBaseDao().selectListByObject("SmsDetail.loadSmsDetailFromPlatform", id);
	}
	
	/**
	 * 根据ReceiverId 和 content 查出信息详情 update by shengyinjiang 20151206
	 */
	public Integer loadSmsDetailByReceiverIdAndContent(Map<String, Object> paramMap)
	{
		return (Integer) this.getBaseDao().selectObjectByObject("SmsDetail.loadSmsDetailByReceiverIdAndContent", paramMap);
	}

	/**
	 * 根据ReceiverId 和 content 和家长手机号码 查出信息详情 update by shengyinjiang 20151206
	 */
	public Integer loadSmsDetailByReceiverIdAndContentInScoreSending(Map<String, Object> paramMap)
	{
		return (Integer) this.getBaseDao().selectObjectByObject("SmsDetail.loadSmsDetailByReceiverIdAndContentInScoreSending", paramMap);
	}

	/**
	 * 加载接收的短信列表信息
	 * 
	 * @param pageSize
	 * @param page
	 * @param paramMap
	 *            参数列表
	 * @return 加载接收的短信数据(分页)
	 * 
	 *         update by shengyinjiang 20151202
	 */
	@SuppressWarnings("unchecked")
	public Pages getAcceptSmsList(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<SmsDetail> list = new ArrayList<SmsDetail>();

		allRow = this.getBaseDao().getAllRowCountByCondition("SmsDetail.getSmsByUserId", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		list = this.getBaseDao().queryForPageByCondition("SmsDetail.getSmsByUserId", paramMap, offset, length);

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;
	}
	/**
	 * 加载接收的短信列表信息【福建专用】
	 * 
	 * @param pageSize
	 * @param page
	 * @param paramMap
	 *            参数列表
	 * @return 加载接收的短信数据(分页)
	 * 
	 *         update by shengyinjiang 20151202
	 */
	@SuppressWarnings("unchecked")
	public Pages getAcceptSmsListFj(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<SmsDetail> list = new ArrayList<SmsDetail>();

		allRow = this.getBaseDao().getAllRowCountByCondition("SmsDetail.getSmsByUserId", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		list = this.getBaseDao().queryForPageByCondition("SmsDetail.getSmsByUserId", paramMap, offset, length);

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;
	}

	@SuppressWarnings("unchecked")
	public Pages getAcceptSmsListFromPlatform(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<SmsDetail> list = new ArrayList<SmsDetail>();

		allRow = this.getBaseDao().getAllRowCountByCondition("SmsDetail.getSmsByUserIdFromPlatform", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		list = this.getBaseDao().queryForPageByCondition("SmsDetail.getSmsByUserIdFromPlatform", paramMap, offset, length);

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;
	}

	/**
	 * 新增短信内容
	 * 
	 * @param sms
	 *            待新增的短信实体 update by shengyinjiang 20151203
	 */
	public int addSms(Sms sms)
	{
		int smsId = getBaseDao().addObject("Sms.addSms", sms);
		return smsId;
	}

	/**
	 * 新增在线留言内容详情
	 * 
	 * @param sms
	 * 
	 */
	public void addSmsDetail(SmsDetail smsDetail)
	{
		getBaseDao().addObject("SmsDetail.addSmsDetail", smsDetail);
	}

	/**
	 * 页面加载成绩发送详细列表 20151203
	 * 
	 * @param paramMap
	 *            参数map
	 * @return 检测导入记录/分页
	 */
	@SuppressWarnings("unchecked")
	public Pages loadScoreSendingSmsDetail(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<SmsDetail> list = new ArrayList<SmsDetail>();
		allRow = this.getBaseDao().getAllRowCountByCondition("SmsDetail.selectScoreSendingSmsDetail", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		list = this.getBaseDao().queryForPageByCondition("SmsDetail.selectScoreSendingSmsDetail", paramMap, offset, length);

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		if (0 == totalPage)
		{
			currentPage = 0;
		}
		pages.setCurrentPage(currentPage);
		pages.setAllRow(allRow);
		pages.setTotalPage(totalPage);
		pages.setList(list);
		pages.init();
		return pages;
	}

	/**
	 * 根据 content 查出信息详情 update by syj 20151206
	 */
	public Integer loadSmsByContent(Map<String, Object> paramMap)
	{
		return (Integer) this.getBaseDao().selectObjectByObject("Sms.loadSmsByContent", paramMap);
	}

	/**
	 * 获取sms表中最后一条数据
	 * 
	 * update by syj 20151210
	 */
	public Sms getLastOneFromSms(Map<String, Object> paramMap)
	{

		return (Sms) this.getBaseDao().selectObjectByObject("Sms.getLastOneFromSms", paramMap);
	}

	/**
	 * 更新sms的smsSum 和 errorSmsSum
	 * 
	 * update by syj 20151210
	 */
	public void updateSms(Map<String, Object> paramMap)
	{
		this.getBaseDao().updateObject("Sms.updateSms", paramMap);

	}
	
	public void saveAuthCodeEmail(AuthCodeEmail authCodeEmail){
		this.getBaseDao().addObject("Sms.addEmailAuthCode", authCodeEmail);
	}
	
	/**
	 * 获取学校是否有短信套餐   虚拟套餐是否开启
	 * @return key:schoolHasMessage value:false没有短信套餐，true有短信套餐
	 *   key:isVirtualOpen value: false学校虚拟套餐关闭 ，true学校虚拟套餐开启
	 *   key:isSchoolSingleMsg value: false没有学校单条套餐 ，true有学校单条套餐 
	 */
	public Map<String,Object>  getSmsServiceStatus()
	{
		boolean schoolHasMessage = false;//学校是否有短信套餐

		boolean isVirtualOpen = false;//学校虚拟套餐是否开启
		
		boolean isSchoolSingleMsg = false;//是否有学校单条类型的短信套餐
		
		//1.先判断学校有无套餐 ： 无--->则所有人都发送 ，有--->判断学校虚拟套餐是否开启
		Integer schoolMessageCount = (Integer) getBaseDao().selectObjectByObject("OrderMessage.getPalmOrderMessageCount", null);;

		Map<String,Object> map = new HashMap<String,Object>();
		
		if (schoolMessageCount == 0)
		{
			//学校有无套餐 所有人正常发送短信（可能学校线下收费）
			schoolHasMessage = false;
		} else
		{
			//学校有套餐 
			schoolHasMessage = true;
			// 0:可用 1：失效  判断学校虚拟套餐是否开启：开启-->所有人正常发送 ，未开启--->判断面向学校且类型是单条且条数有无剩余
			String status = (String) getBaseDao().selectObjectByObject("OrderMessage.getVirtualMessageStatus", null);
			if("0".equals(status))//开启
			{
				isVirtualOpen = true;
			}
			else
			{
				isVirtualOpen = false;
				Map<String, Object> paraMap = new HashMap<String, Object>();
				paraMap.put("type", 1);
				paraMap.put("rangeObject",0);
				//判读学校是否有面向学校的单条短信套餐
				Integer schoolCount = (Integer) getBaseDao().selectObjectByObject("OrderMessage.getSchoolMessageByTypeAndRangeObject", paraMap);
				if(schoolCount == 0)
				{
					isSchoolSingleMsg = false;
				}
				else
				{
					isSchoolSingleMsg = true;
				}
				
			}
		}
		map.put("schoolHasMessage", schoolHasMessage);
		map.put("isVirtualOpen", isVirtualOpen);
		map.put("isSchoolSingleMsg", isSchoolSingleMsg);
		return map;
		
	}
	
	/**
	 * 添加短信详情 （发短信时调用）
	 * @param content
	 * @param user
	 * @param parent
	 * @param stauts
	 */
	public void saveSmsDetail(String content, User user, User parent, int stauts,String type)
	{
		SmsDetail smsDetail = new SmsDetail();
		smsDetail.setContent(content);
		smsDetail.setSenderId(1);
		smsDetail.setSenderName("学校管理员");
		smsDetail.setReceiverId(user.getUserId());
		smsDetail.setReceiverName(user.getRealName());
		smsDetail.setPhone(parent.getPhone());
		smsDetail.setType(type);
		smsDetail.setStatus(stauts);
		smsDetail.setCreateTime(TimeUtil.getInstance().now());
		smsDetail.setSentTime(TimeUtil.getInstance().now());
		getBaseDao().addObject("SmsDetail.addSmsDetail", smsDetail);
	}
}
