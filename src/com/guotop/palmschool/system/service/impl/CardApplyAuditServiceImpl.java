package com.guotop.palmschool.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.guotop.palmschool.service.BaseService;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.system.entity.CardApplyAudit;
import com.guotop.palmschool.system.entity.CardApplyAudit_person;
import com.guotop.palmschool.system.service.CardApplyAuditService;
import com.guotop.palmschool.util.Pages;

/**
 * 在线审核业务实现类
 * @author zhou
 */
@Service("cardApplyAuditService")
public class CardApplyAuditServiceImpl extends BaseService implements CardApplyAuditService
{
	@Resource
	private UserService userService;

	@Override
	public void modifyStatus(HashMap<String, Object> map)
	{
		getBaseDao().updateObject("CardApplyAudit.updateCardApplyAudit", map);
		
	}
	@Override
	public void addCardApply(CardApplyAudit cardApply)
	{
		getBaseDao().addObject("CardApplyAudit.addCardApplyAudit", cardApply);
		
	}
	
	@Override
	public void addCardApplyPerson(CardApplyAudit_person cardApplyAudit_person)
	{
		getBaseDao().addObject("CardApplyAudit.addCardApplyAuditPerson", cardApplyAudit_person);
		
	}
	
	@Override
	public void updateCardApplyPerson(Integer id,String auditStatus,String remark)
	{
		HashMap<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("id", id);
		parmMap.put("auditstatus", auditStatus);
		parmMap.put("remark", remark);
		getBaseDao().updateObject("CardApplyAudit.updateCardApplyAuditPerson", parmMap);
		
	}
	/**
	 * 查询个人的补办卡申请信息
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CardApplyAudit> getReissuedcardPersonByStatus(HashMap<String, Object> map) {
		return getBaseDao().selectListByObject("CardApplyAudit.getReissuedcardPersonByStatus", map);
	}
	
	/**
	 * 查询补办卡申请 （分页）
	 * @param page
	 * @param pageSize
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Pages getCardApplyForPages(int page,int pageSize,HashMap<String, Object> map)
	{
		
		String status = (String)map.get("status");
		
		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		int currentPage = Pages.countCurrentPage(page);
		
		// 解决ibatis框架的分页问题
		// 起始数据坐标
		map.put("startIndex", offset);
		// 单页数据量
		map.put("length", length);
		
		int allRow = 0;
		List<CardApplyAudit> list = new ArrayList<CardApplyAudit>();
		
		List<String> nameList = null;
		
		if (status.equals("0"))
		{
			allRow = (Integer) this.getBaseDao().selectObjectByObject("CardApplyAudit.getCardApplyAuditListCount", map);
			list = getBaseDao().selectListByObject("CardApplyAudit.getCardApplyAuditList", map);
			for (CardApplyAudit caa : list)
			{
				nameList = new ArrayList<String>();
				String memeber = caa.getMemberUserId();
				if (memeber != null && !memeber.equals(""))
				{
					String[] members = memeber.split(",");
					for (String memberId : members)
					{
						nameList.add(userService.getUserNameByUserId(Integer.valueOf(memberId)));
					}
					caa.setUserNames(nameList);
				}
			}
		} else if (status.equals("1"))
		{
			allRow = (Integer) this.getBaseDao().selectObjectByObject("CardApplyAudit.getCardApplyListByStatusCount", map);
			list = getBaseDao().selectListByObject("CardApplyAudit.getCardApplyListByStatus", map);
			for (CardApplyAudit caa : list)
			{
				nameList = new ArrayList<String>();
				String memeber = caa.getMemberUserId();
				if (memeber != null && !memeber.equals(""))
				{
					String[] members = memeber.split(",");
					for (String memberId : members)
					{
						nameList.add(userService.getUserNameByUserId(Integer.valueOf(memberId)));
					}
					caa.setUserNames(nameList);
				}
			}
		} else
		{
			allRow = (Integer) this.getBaseDao().selectObjectByObject("CardApplyAudit.getCardApplyListByStatusCount", map);
			list = getBaseDao().selectListByObject("CardApplyAudit.getCardApplyListByStatus", map);
			for (CardApplyAudit caa : list)
			{
				nameList = new ArrayList<String>();
				String memeber = caa.getMemberUserId();
				if (memeber != null && !memeber.equals(""))
				{
					String[] members = memeber.split(",");
					for (String memberId : members)
					{
						nameList.add(userService.getUserNameByUserId(Integer.valueOf(memberId)));
					}
					caa.setUserNames(nameList);
				}
			}
		}

		Pages pages = new Pages();
		pages.setPageSize(pageSize);
		/**
		 * 如果总页数为0，当前页也应该为0
		 */
		int totalPage = Pages.countTotalPage(pageSize, allRow);
		
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
	
	@Override
	public CardApplyAudit_person getReissuedcardPersonById(Integer id)
	{
		return (CardApplyAudit_person) this.getBaseDao().selectObjectByObject("CardApplyAudit.getReissuedcardPersonById", id);
	}
	
	@Override
	public Integer getCountByTypeAndStatus(Long schoolId,Integer type,Integer auditstatus)
	{
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("auditstatus", auditstatus);
		parmMap.put("type", type);
		parmMap.put("schoolId", schoolId);
		return (Integer) this.getBaseDao().selectObjectByObject("CardApplyAudit.getCountByTypeAndStatus", parmMap);
	}
	@Override
	public Double getAmountCountByTypeAndStatus(Long schoolId,Integer type, Integer auditstatus)
	{
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("auditstatus", auditstatus);
		parmMap.put("type", type);
		parmMap.put("schoolId", schoolId);
		return (Double) this.getBaseDao().selectObjectByObject("CardApplyAudit.getAmountCountByTypeAndStatus", parmMap);
	}
}
