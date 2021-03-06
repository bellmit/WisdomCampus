package com.guotop.palmschool.teachingResources.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guotop.palmschool.service.BaseService;
import com.guotop.palmschool.teachingResources.entity.TeachingResources;
import com.guotop.palmschool.teachingResources.service.TeachingResourcesService;
import com.guotop.palmschool.util.Pages;

/**
 * 教学资源业务类实现类
 */
@Service("teachingResourcesService")
public class TeachingResourcesServiceImpl extends BaseService implements TeachingResourcesService
{
	/**
	 * 分页获得教学资源
	 * 
	 * @author chenyong
	 * @date 2016年6月27日 下午4:10:32
	 * @param pageSize
	 * @param page
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Pages getPageTeachingResources(int pageSize, int page, Map<String, Object> paramMap)
	{
		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<TeachingResources> teachingResourcesList = new ArrayList<TeachingResources>();
		allRow = (int) this.getBaseDao().selectObjectByObject("TeachingResources.getTeachingResourcesCount", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);
		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		paramMap.put("startIndex", offset);
		paramMap.put("length", length);
		teachingResourcesList = this.getBaseDao().selectListByObject("TeachingResources.getTeachingResources", paramMap);
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
		pages.setList(teachingResourcesList);
		pages.init();
		return pages;
	}

	/**
	 * 添加教学资源
	 * 
	 * @author chenyong
	 * @date 2016年6月27日 下午4:49:41
	 * @param teachingResources
	 * @return
	 */
	@Override
	public int addTeachingResources(TeachingResources teachingResources,Map<String,Object> resourceMap)
	{
		int key=this.getBaseDao().addObject("TeachingResources.addTeachingResources", teachingResources);
		addResourceDetail(resourceMap);//将资源加入到RichClound
		return key;
	}
	/**
	 * 删除教学资源
	  @author chenyong
	  @date 2016年6月27日 下午7:13:42
	 * @param id
	 */
	@Override
	public void deleteTeachingResources(Integer id)
	{
	 TeachingResources teachingResources=getBykey(id);
     this.getBaseDao().deleteObjectById("TeachingResources.deleteTeachingResources", id);
     //删除richcloud资源表中的数据
     if(teachingResources!=null){
    	 this.getBaseDao().deleteObject("TeachingResources.deleteCloudResources", teachingResources.getFileUrl());	 
     }
	}
	/**
	 * 添加RichClound教学资源
	  @author chenyong
	  @date 2016年6月27日 下午4:49:41
	 * @param teachingResources
	 * @return
	 */
	@Override
	public int addResourceDetail(Map<String, Object> ResourceMap)
	{
		Map<String,Integer> mapCharge=new HashMap<String, Integer>();
		mapCharge.put("chargeMoney", 0);
		mapCharge.put("chargeType", 2);
		int keyCharge=this.getBaseDao().addObject("TeachingResources.addResourceCharge", mapCharge);
		ResourceMap.put("chargeId", keyCharge);
		int key=this.getBaseDao().addObject("TeachingResources.addResourceDetail", ResourceMap);
		return key;
	}
     /**
      * 更新资源的链接
       @author chenyong
       @date 2016年8月23日 下午1:29:27
      * @param key
      */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateTeacherResource(Map<String,Object> map)
	{
		this.getBaseDao().updateObject("TeachingResources.updateTeachingResourcesByFileUrl", map);
		this.getBaseDao().updateObject("TeachingResources.updateRichCloundResourcesByurl", map);
	}
    /**
     * 根据主键获得数据
      @author chenyong
      @date 2016年9月1日 上午10:51:58
     * @param id
     * @return
     */
	@Override
	public TeachingResources getBykey(Integer id)
	{
		return (TeachingResources) this.getBaseDao().selectObject("TeachingResources.getBykey",id);
	}


}
