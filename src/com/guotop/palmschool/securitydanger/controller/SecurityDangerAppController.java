package com.guotop.palmschool.securitydanger.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guotop.palmschool.asset.entity.AssetServiceman;
import com.guotop.palmschool.asset.service.AssetService;
import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.constant.Cons.PUSHTYPE;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.rest.entity.PushItem;
import com.guotop.palmschool.securitydanger.entity.SecurityDanger;
import com.guotop.palmschool.securitydanger.entity.SecurityDangerAttachment;
import com.guotop.palmschool.securitydanger.entity.SecurityDangerDetail;
import com.guotop.palmschool.securitydanger.service.SecurityDangerService;
import com.guotop.palmschool.securitydanger.vo.SecurityDangerType;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.util.Pages;
import com.guotop.palmschool.util.StringUtil;
import com.richx.pojo.RichHttpResponse;

import dev.gson.GsonHelper;

/**
 * 安全隐患手机
 * 
 * @author chenyong
 *
 */
@Controller
@RequestMapping("/securityDangerApp")
public class SecurityDangerAppController {

	@Autowired
	private SecurityDangerService securityDangerService;

	@Autowired
	private UserService userService;

	@Autowired
	private AssetService assetService;


	@Autowired
	private CommonService commonService;

	/**
	 * 手机端获得安全隐患数据
	 * 
	 * @author chenyong
	 * @Time 2017年3月8日 下午4:25:55
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/getSecurityDangerPages.do")
	@ResponseBody
	public void getSecurityDangerPages(String schoolId, HttpServletResponse response, String apiKey,
			HttpSession sesison, Integer page, Integer pageSize, Integer type) {
		// 这边是利用apikey 进行模拟登录
		User user = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		RichHttpResponse<Pages> rhr = new RichHttpResponse<Pages>();
		Pages pages = null;
		if (user != null) {
			sesison.setAttribute("user", user);
			// 切换数据源
			DBContextHolder.setDBType(schoolId);
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("type", type);
			if (commonService.hasAdminPermission(user)) {// 管理员全部
				paramMap.put("userId", "");
			} else {
				paramMap.put("userId", user.getUserId());// 只看自己
			}
			pages = securityDangerService.getSecurityDangerPages(page, pageSize, paramMap);
			List<SecurityDanger> list = pages.getList();
			List<SecurityDangerType> listType = SecurityDangerType.getSecurityDangerType();
			for (SecurityDanger securityDanger : list) {
				for (SecurityDangerType sd : listType) {
					if (securityDanger.getType().intValue() == sd.getId()) {
						securityDanger.setTypeName(sd.getName());
						break;
					}
				}
			}
			rhr.ResponseCode = 0;
			rhr.ResponseResult = "获取成功";
		}else{
			rhr.ResponseCode =8000;
			rhr.ResponseResult = "apiKey过期";
		}
		rhr.ResponseObject = pages;
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(GsonHelper.toJson(rhr));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得排查人员
	 * 
	 * @author chenyong
	 * @Time 2017年3月9日 下午4:19:15
	 */
	@RequestMapping("/getSecurityServiceman.do")
	@ResponseBody
	public void getSecurityServiceman(HttpServletResponse response, String schoolId, Integer type) {
		List<User> userList = new ArrayList<>();
		List<AssetServiceman> list = new ArrayList<>();
		User user = null;
		RichHttpResponse<List<User>> rhr = new RichHttpResponse<List<User>>();
		DBContextHolder.setDBType(schoolId);
		if (type.intValue() == 1) {// 学生
			// 获得所有的班主任
			 list = assetService.getAllServicemanListByType(-1);
			 for (AssetServiceman assetServiceman : list) {
					user = new User();
					user.setUserId(assetServiceman.getServicemanId());
					user.setRealName(assetServiceman.getServiceman());
					user.setHeadImg(assetServiceman.getServicemanHeadImg());
					userList.add(user);
				}
//			userList = clazzService.getAllClazzLeader();
		} else if (type.intValue() == 2) {// 学校资产
			list = assetService.getAllServicemanListByType(-2);
			for (AssetServiceman assetServiceman : list) {
				user = new User();
				user.setUserId(assetServiceman.getServicemanId());
				user.setRealName(assetServiceman.getServiceman());
				user.setHeadImg(assetServiceman.getServicemanHeadImg());
				userList.add(user);
			}
		}
		rhr.ResponseCode = 0;
		rhr.ResponseResult = "获取成功";
		rhr.ResponseObject = userList;
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(GsonHelper.toJson(rhr));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得安全隐患详细
	 * 
	 * @author chenyong
	 * @Time 2017年3月8日 下午8:23:27
	 */
	@RequestMapping("/getSecurityDangers.do")
	@ResponseBody
	public void getSecurityDangers(HttpSession session, HttpServletResponse response, Integer id, String schoolId,
			String apiKey) {
		DBContextHolder.setDBType(schoolId);
		User user = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		SecurityDanger securityDanger = null;
		RichHttpResponse<SecurityDanger> rhr = new RichHttpResponse<SecurityDanger>();
		if (user != null) {
			securityDanger = securityDangerService.getByKey(id);
			List<SecurityDangerType> listType = SecurityDangerType.getSecurityDangerType();
			for (SecurityDangerType securityDangerType : listType) {
				if (securityDangerType.getId() == securityDanger.getType().intValue()) {
					securityDanger.setTypeName(securityDangerType.getName());
					break;
				}
			}
				List<SecurityDangerDetail> details = securityDangerService.getDetailsByParentId(id);
				securityDanger.setDetails(details);
			// 附件
			List<SecurityDangerAttachment> attachments = securityDangerService.getAttachments(id);
			securityDanger.setAttachments(attachments);
			rhr.ResponseCode = 0;
			rhr.ResponseResult = "获取成功";
		}else{
			rhr.ResponseCode = 8000;
			rhr.ResponseResult = "apiKey过期";	
		}
		rhr.ResponseObject = securityDanger;
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(GsonHelper.toJson(rhr));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 添加学校安全隐患数据
	 * 
	 * @author chenyong
	 * @Time 2017年3月8日 下午4:32:08
	 */
	@RequestMapping(value = "/addSecurityDangers.do", method = RequestMethod.POST)
	@ResponseBody
	public void addSecurityDangers(String schoolId, HttpServletResponse response, String apiKey, String imgs,
			String desc, Integer type, String userIdStr, HttpSession session) {
		int f = 0;
		int key =0;
		// 这边是利用apikey 进行模拟登录
		User user = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		RichHttpResponse<Integer> rhr = new RichHttpResponse<Integer>();
		if (user == null) {
			f = 8000;
			rhr.ResponseResult = "apiKey过期";
		} else {
			try {
				// 安全隐患信息
				SecurityDanger securityDanger = new SecurityDanger();
				securityDanger.setCreateUserId(user.getUserId());
				securityDanger.setDesc(desc);
				securityDanger.setType(type);
				// 排查人员
				String[] userIds = userIdStr.split(",");
				List<SecurityDangerDetail> details = new ArrayList<>();
				SecurityDangerDetail detail = null;
				List<Integer> lists = new ArrayList<>();
				for (String string : userIds) {
					detail = new SecurityDangerDetail();
					detail.setReceive(Integer.parseInt(string));
					details.add(detail);
					lists.add(Integer.parseInt(string));
				}
				List<SecurityDangerAttachment> attachments = new ArrayList<>();
				SecurityDangerAttachment attachment = null;
				// 图片处理
				if (!StringUtil.isEmpty(imgs)) {
					String[] imgArray = imgs.split(",");
					// 附件
					for (String string : imgArray) {
						attachment = new SecurityDangerAttachment();
						attachment.setUrl(string);
						attachments.add(attachment);
					}
				}
				// 切换数据源
				DBContextHolder.setDBType(schoolId);
				key = securityDangerService.insertBachAttachment(securityDanger, attachments, details);
				if (key > 0) {
					// 推送手机端消息
					List<PushItem> list = new ArrayList<PushItem>();
					List<User> listPushItem = commonService.getUserByUserIdsForPush(lists);
					if (desc.length() > 60) {
						desc = desc.substring(0, 60) + "...";
					}
					for (User receiver : listPushItem) {
						PushItem pi = new PushItem();
						pi.receiverId = receiver.getUserId();
						pi.channels = receiver.getBaiduChannelId();
						pi.deviceType = String.valueOf(receiver.getDeviceType());
						pi.PushContent = desc;
						if(type.intValue()==1){//学生行为
							pi.PushType = PUSHTYPE.SECURITYDANGER_STUDENT.getType();
							pi.PushContentType = PUSHTYPE.SECURITYDANGER_STUDENT.getContentType();
							pi.title = PUSHTYPE.SECURITYDANGER_STUDENT.getName();	
						}else{//学校资产
							pi.PushType = PUSHTYPE.SECURITYDANGER_SCHOOL.getType();
							pi.PushContentType = PUSHTYPE.SECURITYDANGER_SCHOOL.getContentType();
							pi.title = PUSHTYPE.SECURITYDANGER_SCHOOL.getName();		
						}
						pi.schoolId = String.valueOf(schoolId);
						pi.OperationapplyId = key;
						list.add(pi);
					}
					commonService.pushMsg(list, false);
				}
				f =0;
				rhr.ResponseResult = "添加成功";
			} catch (Exception e) {
				e.printStackTrace();
				rhr.ResponseResult = "添加失败";
				f =1;
			}
		}
		rhr.ResponseCode = f;
		rhr.ResponseObject =key;
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(GsonHelper.toJson(rhr));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获得安全隐患类型
	 * 
	 * @author chenyong
	 * @Time 2017年3月8日 下午7:36:20
	 */
	@RequestMapping("/getSecurityDangersType.do")
	@ResponseBody
	public void getSecurityDangersType(HttpServletResponse response) {
		RichHttpResponse<List<SecurityDangerType>> rhr = new RichHttpResponse<List<SecurityDangerType>>();

		rhr.ResponseCode = 0;
		rhr.ResponseResult = "获取成功";
		rhr.ResponseObject = SecurityDangerType.getSecurityDangerType();
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(GsonHelper.toJson(rhr));
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
