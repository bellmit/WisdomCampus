package com.guotop.palmschool.rest.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.reflect.TypeToken;
import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.constant.Cons;
import com.guotop.palmschool.constant.Cons.PUSHTYPE;
import com.guotop.palmschool.controller.BaseController;
import com.guotop.palmschool.entity.Department;
import com.guotop.palmschool.entity.OrderMessageUser;
import com.guotop.palmschool.entity.OrderUserMessageDetail;
import com.guotop.palmschool.entity.Role;
import com.guotop.palmschool.entity.Sms;
import com.guotop.palmschool.entity.SmsDetail;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.rest.entity.PushItem;
import com.guotop.palmschool.service.OrderMessageService;
import com.guotop.palmschool.service.SmsService;
import com.guotop.palmschool.service.StudentService;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.util.Pages;
import com.guotop.palmschool.util.PropertiesUtil;
import com.guotop.palmschool.util.StringUtil;
import com.guotop.palmschool.util.TimeUtil;
import com.richx.pojo.Clazz;
import com.richx.pojo.Grade;
import com.richx.pojo.Message;
import com.richx.pojo.PalmUser;
import com.richx.pojo.RichHttpResponse;
import common.Logger;

import dev.gson.GsonHelper;

/**
 * 手机端信息平台
 * 
 * @author Administrator
 *
 */
@RequestMapping("/message")
@Controller
public class MessageRestController extends BaseController
{
	private Logger log = Logger.getLogger(MessageRestController.class);
	@Resource
	private UserService userService;

	@Resource
	private SmsService smsService;

	@Resource
	private CommonService commonService;

	@Resource
	private StudentService studentService;

	@Resource
	private OrderMessageService orderMessageService;
	
	@Autowired
	private ThreadPoolTaskExecutor poolTaskExecutor;

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 查询所有学生，姓名， 年级， 班级
	 * 
	 * @param type
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/getStudentList.do")
	public String getStudentList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		RichHttpResponse<Collection<Grade>> richHttpResponse = new RichHttpResponse<Collection<Grade>>();
		List<User> studentList = null;
		try
		{
			HashMap<Integer, Grade> gradeMap = new HashMap<Integer, Grade>();
			HashMap<Integer, Clazz> clazzMap = new HashMap<Integer, Clazz>();

			String apiKey = request.getParameter("apiKey");
			String schoolId = request.getParameter("schoolId");
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
			if (loginUser != null)
			{
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("permissionCode", "studentMessageManager");

				studentList = userService.selectStudentListByRole(paramMap, loginUser);
				for (User student : studentList)
				{
					if (student.getClazzId() != null)
					{
						Clazz clz;
						Grade grade;
						if (!clazzMap.containsKey(student.getClazzId()))
						{
							clz = new Clazz();
							clz.ClazzId = student.getClazzId() + "";
							clz.ClazzName = student.getClazzName();
							clz.UserList = new ArrayList<PalmUser>();
							clazzMap.put(student.getClazzId(), clz);
							if (!gradeMap.containsKey(student.getGradeId()))
							{
								grade = new Grade();
								grade.GradeId = student.getGradeId() + "";
								grade.GradeName = student.getGradeName();
								grade.ClazzList = new ArrayList<Clazz>();
								gradeMap.put(student.getGradeId(), grade);
							} else
							{
								grade = gradeMap.get(student.getGradeId());
							}
							grade.ClazzList.add(clz);
						} else
						{
							clz = clazzMap.get(student.getClazzId());
							grade = gradeMap.get(student.getGradeId());
						}

						if (student.getUserId() != null)
						{
							PalmUser pu = new PalmUser();
							pu.UserId = student.getUserId() + "";
							pu.UserName = student.getRealName();
							clz.UserList.add(pu);

							grade.Total++;
						}
					}
				}
				Collection<Grade> gradelist = gradeMap.values();
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "获取成功";
				richHttpResponse.ResponseObject = gradelist;
			} else
			{
				richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			}
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 查询所有教师，姓名， 部门
	 * 
	 * @param type
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/getTeacherList.do")
	public String getTeacherList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		RichHttpResponse<List<Clazz>> richHttpResponse = new RichHttpResponse<List<Clazz>>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		try
		{
			String apiKey = request.getParameter("apiKey");
			String schoolId = request.getParameter("schoolId");
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
			if (loginUser != null)
			{

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("user", loginUser);
				paramMap.put("userId", loginUser.getUserId());
				paramMap.put("permissionCode", "teacherMessageManager");

				// 这里的clazz就是部门
				List<Clazz> clazzlist = new ArrayList<Clazz>();
				// 获取部门列表（不同的人看到不同的部门列表）
				List<Department> departmentList = commonService.getDepartList(paramMap);
				// 对象部门循环，获取每个门下所有老师
				for (Department department : departmentList)
				{
					Clazz clz = new Clazz();
					clz.ClazzId = String.valueOf(department.getId());
					clz.ClazzName = department.getDepartmentName();
					List<PalmUser> palmUserlist = new ArrayList<PalmUser>();
					List<User> teacherList = commonService.getTeacherDetailByDepartmentIdInSms(department.getId());
					for (User user : teacherList)
					{
						PalmUser palmUser = new PalmUser();
						palmUser.UserId = String.valueOf(user.getUserId());
						palmUser.UserName = user.getRealName();
						palmUser.Phone = user.getPhone();
						palmUserlist.add(palmUser);
					}
					clz.UserList = (ArrayList<PalmUser>) palmUserlist;

					clazzlist.add(clz);
				}
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "获取成功";
				richHttpResponse.ResponseObject = clazzlist;

			} else
			{
				richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			}
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 新增短信发送相关 mode
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	@RequestMapping(value = "/doAddSms.do")
	public String doAddSms(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws JSONException, IOException
	{
		RichHttpResponse<Collection<Grade>> richHttpResponse = new RichHttpResponse<Collection<Grade>>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		if (loginUser != null)
		{
			// 接收前台发送过来的人员列表
			String receiverListJson = request.getParameter("receiverList");
			// 解析前台发送过来的接收人列表
			ArrayList<PalmUser> receiverList = GsonHelper.<ArrayList<PalmUser>>fromJson(receiverListJson, new TypeToken<ArrayList<PalmUser>>()
			{
			}.getType());

			// 由于老师短信可以直接发送，而学生短信是通过学生查找家长手机号码之后方可发送
			ArrayList<PalmUser> receiverList_parse = new ArrayList<PalmUser>();
			// 发送类型 教师：mode == 0 学生：mode == 1
			Integer mode = Integer.valueOf(request.getParameter("mode"));
			
			if (mode == 0)
			{
				// 老师就不用解析直接可以使用
				if(!CollectionUtils.isEmpty(receiverList)){
					//去重
					Map<String, PalmUser> userMap = new HashMap<String, PalmUser>();
 					for(PalmUser palmUser:receiverList){
						if(!userMap.containsKey(palmUser.UserId)){
							receiverList_parse.add(palmUser);
							userMap.put(palmUser.UserId, palmUser);
						}
					}
				}
			} else
			{
				// 学生的话需要获得他的家长手机号码
				for (PalmUser palmUser : receiverList)
				{
//					List<User> studentList = studentService.getStudentListWithPartentPhoneByIdInStudentSMS(Integer.valueOf(palmUser.UserId));
					List<User> parentList = commonService.getParentByStudentId(Integer.valueOf(palmUser.UserId));
					/*
					 * TODO 目前这个功能只对蕉城实验小学和RICHX使用
					 * 	需要增加判断如果是两个家长的话，只发其中登录过亦信家长的短信(如果家长要求两个号码都能接收，在页面中设置即可)
					 *  需要判断条件是
					 *  1.如果只有一个家长的话正常发送
					 *  2.如果两个家长的话选择其中一个家长
					 *  	2.1如果两家长中只要有一个是教职工，那两个家长都发送
					 *  	2.2如果两个都是普通家长，选择登录亦信次数超过0,两个家长没有登录的选择其中一个家长
					 */
					if(!CollectionUtils.isEmpty(parentList)){
						if("3509020027".equals(schoolId)  || "3201140009".equals(schoolId)){
							List<User> parentTmpList = new ArrayList<User>(); 
							List<PalmUser> parentPalmUserList = new ArrayList<PalmUser>();
							boolean isParent1Send = false;
							boolean isParent2Send = false;
							boolean isParent1SetRecive = false;//家长是否开启接收短信
							boolean isParent2SetRecive = false;//家长是否开启接收短信
							int parent1Count = 0;//家长1登录次数
							int parent2Count = 0;//家长2登录次数
							int parent1IsDefalutUserName = 0;//家长1是否是默认亦信号0:是 1:不是
							int parent2IsDefalutUserName = 0;//家长2是否是默认亦信号0:是 1:不是
							String parent1UserName = "";//家长1亦信号
							String parent2UserName = "";//家长2亦信号
							for(int i=0;i<parentList.size();i++){
								User parent = parentList.get(i);
								PalmUser parentPalmUser = new PalmUser();
								parentPalmUser.ParentId =parent.getUserId()+"";
								parentPalmUserList.add(parentPalmUser);
								
								if(i == 0){
									parent1Count = parent.getCount();
									parent1IsDefalutUserName = parent.getIsDefalutUserName();
									parent1UserName = parent.getUserName();
									isParent1SetRecive = parent.getIsReciveSms() == 0?true:false;
								}else if(i == 1){
									parent2Count = parent.getCount();
									parent2IsDefalutUserName = parent.getIsDefalutUserName();
									parent2UserName = parent.getUserName();
									isParent2SetRecive = parent.getIsReciveSms() == 0?true:false;
								}
								if((!StringUtil.isEmpty(parent.getBaiduChannelId())) || parent.getCount() > 0){
									if(i == 0){
										isParent1Send = true;
									}else if(i == 1){
										isParent2Send = true;
									}
								}
							}
							if(isParent1SetRecive || isParent2SetRecive){
								if(isParent1SetRecive){
									parentTmpList.add(parentList.get(0));
								}
								if(isParent2SetRecive){
									parentTmpList.add(parentList.get(1));
								}
							}else{
								if(isParent2Send && isParent1Send){
									//两个家长同时能接收短信的情况下
									if(parent1IsDefalutUserName !=0 && parent2IsDefalutUserName !=0){
										if((!StringUtil.isEmpty(parent1UserName)) && (!StringUtil.isEmpty(parent2UserName))){
											if(parent1Count > parent2Count){
												parentTmpList.add(parentList.get(0));
											}else{
												parentTmpList.add(parentList.get(1));
											}
										}else{
											if(!StringUtil.isEmpty(parent1UserName)){
												parentTmpList.add(parentList.get(0));
											}
											if(!StringUtil.isEmpty(parent2UserName)){
												parentTmpList.add(parentList.get(1));
											}
											if(!((!StringUtil.isEmpty(parent1UserName)) || (!StringUtil.isEmpty(parent2UserName)))){
												parentTmpList.add(parentList.get(0));
											}
										}
									}else{
										if(parent1IsDefalutUserName !=0){
											parentTmpList.add(parentList.get(0));
										}
										if(parent2IsDefalutUserName !=0){
											parentTmpList.add(parentList.get(1));
										}
										if(!(parent1IsDefalutUserName !=0 || parent2IsDefalutUserName !=0)){
											parentTmpList.add(parentList.get(0));
										}
									}
								}else
								{
									if(isParent1Send){
										parentTmpList.add(parentList.get(0));
									}
									if(isParent2Send){
										parentTmpList.add(parentList.get(1));
									}
									
									if(!(isParent1Send || isParent2Send)){
										parentTmpList.add(parentList.get(0));
									}
								}
							}
							
							for (User parentTmp : parentTmpList)
							{
								//是否是教职工子女
								boolean isStudentForSchool = false;
								List<Role> roleList = parentTmp.getRoleList();
								for(Role role : roleList){
									if((!"student".equals(role.getRoleCode())) && (!"parent".equals(role.getRoleCode()))){
										isStudentForSchool = true;
										break;
									}
								}
								
								PalmUser palmUserWithParentPhone = new PalmUser();
								palmUserWithParentPhone.UserId = palmUser.UserId;
								palmUserWithParentPhone.UserName = palmUser.UserName;
								palmUserWithParentPhone.Phone = parentTmp.getPhone();
								palmUserWithParentPhone.ParentId = String.valueOf(parentTmp.getUserId());
								palmUserWithParentPhone.parentList = parentPalmUserList;
								palmUserWithParentPhone.isStudentForSchool = isStudentForSchool;
								receiverList_parse.add(palmUserWithParentPhone);
							}
						}else{
							List<PalmUser> parentPalmUserList = new ArrayList<PalmUser>();
							for (User parent : parentList)
							{
								PalmUser parentPalmUser = new PalmUser();
								parentPalmUser.ParentId =parent.getUserId()+"";
								parentPalmUserList.add(parentPalmUser);
								
								boolean isStudentForSchool = false;
								List<Role> roleList = parent.getRoleList();
								for(Role role : roleList){
									if((!"student".equals(role.getRoleCode())) && (!"parent".equals(role.getRoleCode()))){
										isStudentForSchool = true;
										break;
									}
								}
								
								PalmUser palmUserWithParentPhone = new PalmUser();
								palmUserWithParentPhone.UserId = palmUser.UserId;
								palmUserWithParentPhone.UserName = palmUser.UserName;
								palmUserWithParentPhone.Phone = parent.getPhone();
								palmUserWithParentPhone.ParentId = String.valueOf(parent.getUserId());
								palmUserWithParentPhone.parentList = parentPalmUserList;
								palmUserWithParentPhone.isStudentForSchool = isStudentForSchool;
								receiverList_parse.add(palmUserWithParentPhone);
							}
						}
					}
				}
			}
			// 接受前台发送的短信内容
			String content = request.getParameter("content").trim();
			Properties pro = PropertiesUtil.getInstance().read("wisdomcampus.properties");
			int smsWordLimit = PropertiesUtil.getInstance().getSmsWordLimit(pro, schoolId);
			if (!StringUtil.isEmpty(content))
			{
				if (content.length() > smsWordLimit)
				{
					content.substring(0, smsWordLimit);
				}
			}
			// 获取定时时间
			String sendTime = null;
			try
			{
				sendTime = request.getParameter("sendTime");
				if (sendTime == "")
				{
					Calendar c = Calendar.getInstance();
					c.setTime(new Date()); // 设置当前日期
					c.add(Calendar.MINUTE, 1); // 日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
					Date date = c.getTime(); // 结果
					sendTime = formatter.format(date);
				}
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "提交成功";

				String json = GsonHelper.toJson(richHttpResponse);
				response.getWriter().write(json);
				response.getWriter().flush();
			} catch (Exception e)
			{
			}

			// 下面启用线程发送正常短息
			final Integer modeFinal = mode;
			final ArrayList<PalmUser> receiverListFinal = receiverList_parse;
			final String contentFinal = content;
			final String sendTimeFinal = sendTime;
			final User userFinal = loginUser;
			poolTaskExecutor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						User user = userFinal;
						DBContextHolder.setDBType(user.getSchoolId());
						send(user, contentFinal, receiverListFinal, modeFinal, sendTimeFinal);

					} catch (Exception e)
					{
						log.error("发送短信失败,接收人id：" + userFinal.getUserId() + "\r\n" + e);
					}
				}
			});
		} else
		{
			richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
			richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		}

		return null;
	}

	private void send(User user, String content, ArrayList<PalmUser> receiverList, Integer mode, String sendTime)
	{
		boolean schoolHasMessage = (boolean) smsService.getSmsServiceStatus().get("schoolHasMessage");// 学校是否有短信套餐
		boolean isVirtualOpen = (boolean) smsService.getSmsServiceStatus().get("isVirtualOpen");// 学校虚拟套餐是否开启
		boolean isSchoolSingleMsg = (boolean) smsService.getSmsServiceStatus().get("isSchoolSingleMsg");// 是否有学校单条类型套餐

		// 保存到sms表中
		Sms sms = new Sms();
		sms.setContent(content);
		sms.setSenderId(user.getUserId());
		sms.setSenderName(user.getRealName());
		sms.setSmsLength(content.length()); // 短信长度
		sms.setPhoneCount(receiverList.size()); // 发送人数
		sms.setResult(0);// 短信回执状态
		sms.setReportAmount(0);
		sms.setSuccAmount(0);
		sms.setCreateTime(TimeUtil.getInstance().now());

		if (StringUtil.isEmpty(sendTime))
		{
			sendTime = formatter.format(new Date());
			sms.setIsRealTime(Cons.SMS_REALTIME);// 1:即时发送
		} else
		{
			sms.setIsRealTime(Cons.SMS_TIMING);// 0:定时发送
		}
		sms.setSentTime(sendTime);

		if (mode == 0)
		{
			sms.setType("0"); // 发送短信类型，0:教师短信服务 1:学生短信服务 2:成绩下发 3:会议通知
								// 4:校车5:作业发送
			sms.setModule(Cons.MODULE_SCHOOLAFFAIRS); // 教师短信服务
		} else if (mode == 1)
		{
			sms.setType("1"); // 发送短信类型，0:教师短信服务 1:学生短信服务 2:成绩下发 3:会议通知
								// 4:校车5:作业发送
			sms.setModule(Cons.MODULE_STUDENTSERVICE); // 学生短信服务
		}

		Integer smsCount = 0;
		Map<String, Object> paramMap_sms = new HashMap<String, Object>();
		try
		{
			// 先查找24小时内是否发过该短信,超过可以重发, 要是不超过判断如果是同一个人就发送短信
			paramMap_sms.put("content", content);
			paramMap_sms.put("time", formatter.format((formatter.parse(sendTime).getTime() - 24 * 60 * 60 * 1000)));
			smsCount = smsService.loadSmsByContent(paramMap_sms);
		} catch (ParseException e1)
		{
			e1.printStackTrace();
		}

		// 获取数据库中最新一条数据的短信总数smsSum 和 errorSmsSum
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Sms lastSms = smsService.getLastOneFromSms(paramMap);
		int smsSum = 0; // 短信发送总数
		try
		{
			smsSum = lastSms.getSmsSum();
		} catch (Exception e)
		{

		}
		int errorSmsSum = 0; // 短信发送总数
		try
		{
			errorSmsSum = lastSms.getErrorSmsSum();
		} catch (Exception e)
		{

		}

		sms.setSmsSum(smsSum);
		sms.setErrorSmsSum(errorSmsSum);
		// 保存到数据库, 并且返回smsId
		int smsId = smsService.addSms(sms);
		// 本次短信的条数（65个字算一条短信）
		int count = ((int) content.length() / 65) + 1;
		List<PushItem> piList = new ArrayList<PushItem>();
		Map<String, Object> piMap = new HashMap<String, Object>();
		String schoolId = user.getSchoolId();
		
		try{
			//蕉城实小schoolId="3509020027" ,发送人自己也需要收到短信
			if("3509020027".equals(schoolId) && (mode == 1)){
				if(!commonService.hasAdminPermission(user)){
					SmsDetail loginSmsDetail = new SmsDetail();
					loginSmsDetail.setContent(content);
					loginSmsDetail.setSenderId(user.getUserId());
					loginSmsDetail.setSenderName(user.getRealName());
					loginSmsDetail.setReceiverId(user.getUserId());
					loginSmsDetail.setPhone(user.getPhone());
					loginSmsDetail.setReceiverName(user.getRealName());
					loginSmsDetail.setSeq("");
					loginSmsDetail.setReport("");
					loginSmsDetail.setSmsId(smsId);
					loginSmsDetail.setReportTime(TimeUtil.getInstance().now());
					loginSmsDetail.setCreateTime(TimeUtil.getInstance().now());
					loginSmsDetail.setType(sms.getType());
					loginSmsDetail.setSentTime(sendTime);
					
					PalmUser teacherReceiver = new PalmUser();
					teacherReceiver.UserId = user.getUserId()+"";
					teacherReceiver.Phone = user.getPhone();
					
					Map<String, Object> result_paramMap = sendSmsByOrdermessage(
							0,schoolHasMessage,isVirtualOpen,isSchoolSingleMsg,user,teacherReceiver,smsId,content,sendTime,loginSmsDetail,smsSum, count,errorSmsSum);
					loginSmsDetail = (SmsDetail) result_paramMap.get("smsDetail");
					smsSum = (Integer) result_paramMap.get("smsSum");
					errorSmsSum = (Integer) result_paramMap.get("errorSmsSum");
					addSmsDetail(loginSmsDetail,smsId,smsSum,errorSmsSum);
				}
			}
		}catch(Exception e){
			//这边异常不处理
			log.error("Mobile TEACHER private void send2(" + e.getMessage());
		}
		
		for (PalmUser palmReceiver : receiverList)
		{
			try{
				
				if(mode == 0) //教师
				{
					PushItem pi = new PushItem();
					User receiver = commonService.getUserByUserIdForPush(Integer.valueOf(palmReceiver.UserId));
					palmReceiver.ChannelId = receiver.getBaiduChannelId();
					palmReceiver.DeviceType = receiver.getDeviceType();
					pi.title = PUSHTYPE.TEACHERPUSH.getName();
					pi.PushContent = content;
					pi.PushType = PUSHTYPE.TEACHERPUSH.getType();
					pi.PushContentType = PUSHTYPE.TEACHERPUSH.getContentType();
					String channelId = palmReceiver.ChannelId;
					Integer deviceType = palmReceiver.DeviceType;
					if (!StringUtil.isEmpty(channelId) && deviceType != null && deviceType != 0)
					{
						pi.channels = channelId;
						pi.deviceType = String.valueOf(deviceType);
					}
					pi.receiverId = Integer.valueOf(palmReceiver.UserId);
					pi.schoolId = schoolId;
					if(!piMap.containsKey(palmReceiver.UserId)){
						piList.add(pi);
						piMap.put(palmReceiver.UserId, pi);
					}
					piList.add(pi);
				}else if (mode == 1) //学生
				{
					
					for(PalmUser parentPalmUser : palmReceiver.parentList){
						PushItem pi = new PushItem();
						User receiver = commonService.getUserByUserIdForPush(Integer.valueOf(parentPalmUser.ParentId));
						palmReceiver.ChannelId = receiver.getBaiduChannelId();
						palmReceiver.DeviceType = receiver.getDeviceType();
						
						pi.title = PUSHTYPE.STUDENTPUSH.getName();
						//睢县育才学校不要加人名
						if("4114220090".equals(schoolId)||"3201140009".equals(schoolId)){
							pi.PushContent = content;
						}else{
							pi.PushContent = "["+palmReceiver.UserName+"]"+ content;
						}
						pi.PushType = PUSHTYPE.STUDENTPUSH.getType();
						pi.PushContentType = PUSHTYPE.STUDENTPUSH.getContentType();
						String channelId = palmReceiver.ChannelId;
						Integer deviceType = palmReceiver.DeviceType;
						if (!StringUtil.isEmpty(channelId) && deviceType != null && deviceType != 0)
						{
							pi.channels = channelId;
							pi.deviceType = String.valueOf(deviceType);
						}
						pi.receiverId = Integer.valueOf(palmReceiver.ParentId);
						pi.schoolId = schoolId;
						if(!piMap.containsKey(palmReceiver.ParentId)){
							piList.add(pi);
							piMap.put(palmReceiver.ParentId, pi);
						}
					}
				}
				
				SmsDetail smsDetail = new SmsDetail();
				smsDetail.setContent(content);
				smsDetail.setSenderId(user.getUserId());
				smsDetail.setSenderName(user.getRealName());
				smsDetail.setReceiverId(Integer.valueOf(palmReceiver.UserId));
				smsDetail.setPhone(palmReceiver.Phone);
				smsDetail.setReceiverName(palmReceiver.UserName);
				smsDetail.setSeq("");
				smsDetail.setReport("");
				smsDetail.setSmsId(smsId);
				smsDetail.setReportTime(TimeUtil.getInstance().now());
				smsDetail.setCreateTime(TimeUtil.getInstance().now());
				smsDetail.setType(sms.getType());
				smsDetail.setSentTime(sendTime);
				
				Map<String, Object> result_paramMap = new HashMap<String, Object>();
				if (smsCount < 1)
				{
					result_paramMap = sendSmsByOrdermessage(
							mode,schoolHasMessage,isVirtualOpen,isSchoolSingleMsg,user, palmReceiver,smsId,content,sendTime,smsDetail,smsSum, count,errorSmsSum);
					smsDetail = (SmsDetail) result_paramMap.get("smsDetail");
					smsSum = (Integer) result_paramMap.get("smsSum");
					errorSmsSum = (Integer) result_paramMap.get("errorSmsSum");
				} else
				{
					// 发送短信 如果2小时内已经发送成功， 就不用发送了，
					Map<String, Object> paramMap_smsDetail = new HashMap<String, Object>();
					paramMap_smsDetail.put("phone", palmReceiver.Phone);
					paramMap_smsDetail.put("content", content);
					paramMap_smsDetail.put("status", 0);// 查询成功的短信
					paramMap_smsDetail.put("time", paramMap_sms.get("time"));
					Integer smsDetailCount = smsService.loadSmsDetailByReceiverIdAndContent(paramMap_smsDetail);
					if (smsDetailCount < 1)
					{
						result_paramMap = sendSmsByOrdermessage(
								mode,schoolHasMessage,isVirtualOpen,isSchoolSingleMsg,user, palmReceiver,smsId,content,sendTime,smsDetail,smsSum, count,errorSmsSum);
						smsDetail = (SmsDetail) result_paramMap.get("smsDetail");
						smsSum = (Integer) result_paramMap.get("smsSum");
						errorSmsSum = (Integer) result_paramMap.get("errorSmsSum");
					} else
					{
						smsDetail.setStatus(0);// 发送成功
					}
				}
				
				addSmsDetail(smsDetail,smsId,smsSum,errorSmsSum);
			}catch(Exception e){
				continue;
			}
		}
		//批量推送 
		commonService.pushMsg(piList, false);
	}

	/**
	 * 封装保存到发送详情方法，并且更新实时更新发送总数
	 * @param smsDetail
	 * @param smsId
	 * @param smsSum
	 * @param errorSmsSum
	 */
	private void addSmsDetail(SmsDetail smsDetail, int smsId, int smsSum, int errorSmsSum)
	{
		try
		{
			smsService.addSmsDetail(smsDetail);

			// 更新总共发送条数和发送失败总条数
			Map<String, Object> paramMap_update = new HashMap<String, Object>();
			paramMap_update.put("id", smsId);
			paramMap_update.put("smsSum", smsSum);
			paramMap_update.put("errorSmsSum", errorSmsSum);
			smsService.updateSms(paramMap_update);
		} catch (Exception e)
		{
			log.error("SmsControl.addSmsDetail:" + e.getMessage());
		}
	}
	
	/**
	 * 根据每个人所属服务费套餐有效期来发送信息
	 * @param mode 0:教师  1:学生
	 * @param schoolHasMessage
	 * @param isVirtualOpen
	 * @param isSchoolSingleMsg
	 * @param user
	 * @param receiver
	 * @param parent (仅仅在mode==1的时候使用)
	 * @param smsId
	 * @param content
	 * @param sendTime
	 * @param smsDetail
	 * @param smsSum
	 * @param count
	 * @param errorSmsSum
	 */
	public Map<String, Object> sendSmsByOrdermessage(
			int mode,
			boolean schoolHasMessage,
			boolean isVirtualOpen,
			boolean isSchoolSingleMsg,
			User user, PalmUser receiver,
			int smsId,String content, String sendTime, 
			SmsDetail smsDetail, int smsSum, 
			int count, int errorSmsSum){

		if (mode == 0)
		{
			if (schoolHasMessage)// 学校有短信套餐
			{
				if (isVirtualOpen)// 虚拟套餐开启直接发送
				{
					// 调用短信接口
					try
					{
						boolean flag = false;
						if(!StringUtil.isEmpty(receiver.Phone)){
							flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.TEACHERPUSH, receiver.Phone,
									Integer.valueOf(receiver.UserId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
						}
						if (flag)
						{
							smsDetail.setStatus(0);// 发送成功
							smsSum += count;
						} else
						{
							smsDetail.setStatus(1);// 发送失败
							errorSmsSum += count;// 发送失败+1
						}

					} catch (Exception e)
					{
						log.error("MessageRest private void send(" + e.getMessage());
						smsDetail.setStatus(1);// 发送失败
						errorSmsSum += count;// 发送失败+1
					}
				} else// 虚拟套餐关闭
				{
					if (isSchoolSingleMsg)
					{
						// 学校条数套餐剩余条数
						Integer remainCount = orderMessageService.getRemainCountBySchoolIdForSchoolRange(user.getSchoolId());
						if (null != remainCount && remainCount > 0)// 有条数且大于0
																	// 直接发送
						{
							// 调用短信接口
							try
							{
								boolean flag = false;
								if(!StringUtil.isEmpty(receiver.Phone)){
									flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.TEACHERPUSH, receiver.Phone,
											Integer.valueOf(receiver.UserId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
								}
								if (flag)
								{
									smsDetail.setStatus(0);// 发送成功
									smsSum += count;
								} else
								{
									smsDetail.setStatus(1);// 发送失败
									errorSmsSum += count;// 发送失败+1
								}

							} catch (Exception e)
							{
								log.error("MessageRest private void send(" + e.getMessage());
								smsDetail.setStatus(1);// 发送失败
								errorSmsSum += count;// 发送失败+1
							}
							orderMessageService.updateOrderMessageSchoolById(user.getSchoolId());
						} else
						{
							// "1":发送失败 "2":没有缴纳服务费
							smsDetail.setStatus(2);// 发送失败
							errorSmsSum += count;// 发送失败+1
						}
					} else
					{
						// 调用短信接口
						try
						{
							boolean flag = false;
							if(!StringUtil.isEmpty(receiver.Phone)){
								flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.TEACHERPUSH, receiver.Phone,
										Integer.valueOf(receiver.UserId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
							}
							if (flag)
							{
								smsDetail.setStatus(0);// 发送成功
								smsSum += count;
							} else
							{
								smsDetail.setStatus(1);// 发送失败
								errorSmsSum += count;// 发送失败+1
							}

						} catch (Exception e)
						{
							log.error("MessageRest private void send(" + e.getMessage());
							smsDetail.setStatus(1);// 发送失败
							errorSmsSum += count;// 发送失败+1
						}
					}
				}
			} else
			{
				// 调用短信接口
				try
				{
					boolean flag = false;
					if(!StringUtil.isEmpty(receiver.Phone)){
						flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.TEACHERPUSH, receiver.Phone, Integer.valueOf(receiver.UserId),
								content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
					}
					if (flag)
					{
						smsDetail.setStatus(0);// 发送成功
						smsSum += count;
					} else
					{
						smsDetail.setStatus(1);// 发送失败
						errorSmsSum += count;// 发送失败+1
					}

				} catch (Exception e)
				{
					log.error("MessageRest private void send(" + e.getMessage());
					smsDetail.setStatus(1);// 发送失败
					errorSmsSum += count;// 发送失败+1
				}
			}

		} else if (mode == 1)
		{
			//睢县育才学校不要加人名
			if("4114220090".equals(user.getSchoolId())||"3201140009".equals(user.getSchoolId())){
				content = content;
			}else{
				content = "["+receiver.UserName+"]"+ content;
			}
			if (schoolHasMessage)// 学校有短信套餐
			{
				if (isVirtualOpen)// 虚拟套餐开启直接发送
				{
					// 调用短信接口
					try
					{
						boolean flag = false;
						if(!StringUtil.isEmpty(receiver.Phone)){
							flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.STUDENTPUSH, receiver.Phone,
									Integer.valueOf(receiver.ParentId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
						}
						if (flag)
						{
							smsDetail.setStatus(0);// 发送成功
							smsSum += count;
						} else
						{
							smsDetail.setStatus(1);// 发送失败
							errorSmsSum += count;// 发送失败+1
						}

					} catch (Exception e)
					{
						log.error("MessageRest private void send(" + e.getMessage());
						smsDetail.setStatus(1);// 发送失败
						errorSmsSum += count;// 发送失败+1
					}
				} else// 虚拟套餐关闭
				{
					if (isSchoolSingleMsg)// 是否有学校单条套餐 有判断条数
					{
						// 学校条数套餐剩余条数
						Integer remainCount = orderMessageService.getRemainCountBySchoolIdForSchoolRange(user.getSchoolId());
						if (null != remainCount && remainCount > 0)// 有条数且大于0
						{
							// 调用短信接口
							try
							{
								boolean flag = false;
								if(!StringUtil.isEmpty(receiver.Phone)){
									flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.STUDENTPUSH, receiver.Phone,
											Integer.valueOf(receiver.ParentId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
								}
								if (flag)
								{
									smsDetail.setStatus(0);// 发送成功
									smsSum += count;
								} else
								{
									smsDetail.setStatus(1);// 发送失败
									errorSmsSum += count;// 发送失败+1
								}

							} catch (Exception e)
							{
								log.error("MessageRest private void send(" + e.getMessage());
								smsDetail.setStatus(1);// 发送失败
								errorSmsSum += count;// 发送失败+1
							}

							orderMessageService.updateOrderMessageSchoolById(user.getSchoolId());
						} else// 没有学校条数套餐 判断学生是否购买短信套餐
						{
							// 先判断学生有没有购买过
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("userId", receiver.UserId);
							map.put("status", 0);
							OrderMessageUser messageUser = orderMessageService.getOrderMessageUserByUserId(map);
							boolean isCharge = false;
							if (messageUser != null)
							{
								if (messageUser.getType() == 0)
								{
									TimeUtil.getInstance();
									if (TimeUtil.nowDateIsBetween(messageUser.getStartTime(), messageUser.getEndTime()))
									{
										isCharge = true;
									} else
									{
										isCharge = false;
									}

								} else
								{
									if (messageUser.getCount() > 0)
									{
										isCharge = true;
									} else
									{
										isCharge = false;
									}
								}
							}

							if(!isCharge){
								if(receiver.isStudentForSchool){
									isCharge = true;
								}
							}
							
							if (isCharge)
							{
								// 调用短信接口
								try
								{
									boolean flag = false;
									if(!StringUtil.isEmpty(receiver.Phone)){
										flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.STUDENTPUSH, receiver.Phone,
												Integer.valueOf(receiver.ParentId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
									}
									
									if (flag)
									{
										smsDetail.setStatus(0);// 发送成功
										smsSum += count;
									} else
									{
										smsDetail.setStatus(1);// 发送失败
										errorSmsSum += count;// 发送失败+1
									}

								} catch (Exception e)
								{
									log.error("MessageRest private void send(" + e.getMessage());
									smsDetail.setStatus(1);// 发送失败
									errorSmsSum += count;// 发送失败+1
								}
								int smscount = ((int) content.length() / 65) + 1;
								int messcount = 0;
								if (messageUser != null)
								{
									if (messageUser.getType() == 1)
									{
										// 如果短信剩余条数小于本次短信条说 剩余条数为0
										// 如果大于等于本次短信条说
										// 则本次剩余条数为原剩余条数减本次短信条数
										if (messageUser.getCount() < smscount)
										{
											messcount = 0;
										} else
										{
											messcount = messageUser.getCount() - smscount;
										}
										map.put("count", messcount);
										map.put("id", messageUser.getId());
										orderMessageService.updateOrederMessageUserCount(map);

									}

									OrderUserMessageDetail messDetail = new OrderUserMessageDetail();
									messDetail.setCount(count);
									messDetail.setMessageId(messageUser.getMessageId());
									messDetail.setMessUserId(messageUser.getId());
									messDetail.setNowCount(messcount);
									messDetail.setOrgCount(messageUser.getCount());
									messDetail.setType(messageUser.getType());
									messDetail.setUserId(messageUser.getUserId());
									orderMessageService.addOrderUserMessageDetail(messDetail);
								}
								
							} else
							{
								// "1":发送失败 "2":没有缴纳服务费
								smsDetail.setStatus(2);// 发送失败
								errorSmsSum += count;// 发送失败+1
							}
						}

					} else// 没有判断学生是否购买服务费
					{
						// 先判断学生有没有购买过
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("userId", receiver.UserId);
						map.put("status", 0);
						OrderMessageUser messageUser = orderMessageService.getOrderMessageUserByUserId(map);
						boolean isCharge = false;
						if (messageUser != null)
						{
							if (messageUser.getType() == 0)
							{
								TimeUtil.getInstance();
								if (TimeUtil.nowDateIsBetween(messageUser.getStartTime(), messageUser.getEndTime()))
								{
									isCharge = true;
								} else
								{
									isCharge = false;
								}

							} else
							{
								if (messageUser.getCount() > 0)
								{
									isCharge = true;
								} else
								{
									isCharge = false;
								}
							}
						}

						if(!isCharge){
							if(receiver.isStudentForSchool){
								isCharge = true;
							}
						}
						
						if (isCharge)
						{
							// 调用短信接口
							// 调用短信接口
							try
							{
								boolean flag = false;
								if(!StringUtil.isEmpty(receiver.Phone)){
									flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.STUDENTPUSH, receiver.Phone, Integer.valueOf(receiver.ParentId),
											content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
								}
								
								if (flag)
								{
									smsDetail.setStatus(0);// 发送成功
									smsSum += count;
								} else
								{
									smsDetail.setStatus(1);// 发送失败
									errorSmsSum += count;// 发送失败+1
								}

							} catch (Exception e)
							{
								log.error("MessageRest private void send(" + e.getMessage());
								smsDetail.setStatus(1);// 发送失败
								errorSmsSum += count;// 发送失败+1
							}
							
							int smscount = ((int) content.length() / 65) + 1;
							int messcount = 0;
							if (messageUser != null)
							{
								if (messageUser.getType() == 1)
								{
									// 如果短信剩余条数小于本次短信条说 剩余条数为0
									// 如果大于等于本次短信条说
									// 则本次剩余条数为原剩余条数减本次短信条数
									if (messageUser.getCount() < smscount)
									{
										messcount = 0;
									} else
									{
										messcount = messageUser.getCount() - smscount;
									}
									map.put("count", messcount);
									map.put("id", messageUser.getId());
									orderMessageService.updateOrederMessageUserCount(map);

								}

								OrderUserMessageDetail messDetail = new OrderUserMessageDetail();
								messDetail.setCount(count);
								messDetail.setMessageId(messageUser.getMessageId());
								messDetail.setMessUserId(messageUser.getId());
								messDetail.setNowCount(messcount);
								messDetail.setOrgCount(messageUser.getCount());
								messDetail.setType(messageUser.getType());
								messDetail.setUserId(messageUser.getUserId());
								orderMessageService.addOrderUserMessageDetail(messDetail);
							}
						} else
						{
							// "1":发送失败 "2":没有缴纳服务费
							smsDetail.setStatus(2);// 发送失败
							errorSmsSum += count;// 发送失败+1
						}
					}
				}
			} else// 学校无短信套餐 直接发送
			{
				// 调用短信接口
				try
				{
					boolean flag = false;
					if(!StringUtil.isEmpty(receiver.Phone)){
						flag = commonService.sendSmsByDB(user.getSchoolId(), PUSHTYPE.STUDENTPUSH, receiver.Phone,
								Integer.valueOf(receiver.ParentId), content, sendTime, 2);//原先是3-->2 不发推送,下面批量推送
					}
					
					if (flag)
					{
						smsDetail.setStatus(0);// 发送成功
						smsSum += count;
					} else
					{
						smsDetail.setStatus(1);// 发送失败
						errorSmsSum += count;// 发送失败+1
					}

				} catch (Exception e)
				{
					log.error("MessageRest private void send(" + e.getMessage());
					smsDetail.setStatus(1);// 发送失败
					errorSmsSum += count;// 发送失败+1
				}
			}
		}
		// 更新总共发送条数和发送失败总条数
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("smsDetail", smsDetail);
		paramMap.put("smsSum", smsSum);
		paramMap.put("errorSmsSum", errorSmsSum);

		return paramMap;
	}
	/**
	 * 信息收件箱
	 */
	@RequestMapping(value = "/getMySmsList.do")
	public String getMySmsList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		RichHttpResponse<List<Message>> richHttpResponse = new RichHttpResponse<List<Message>>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 分页信息
		Integer currentPage = 1;
		try
		{
			currentPage = Integer.valueOf(request.getParameter("cPage"));
		} catch (Exception e)
		{
			currentPage = 1;
		}
		try
		{
			String apiKey = request.getParameter("apiKey");
			List<User> userList = userService.getUserByApiKey(apiKey);
			if (userList != null & userList.size() > 0)
			{
				/*
				 * 1.智慧校园用户 2.亦信用户 绑定了智慧校园用户 3.亦信用户没有绑定智慧校园用户
				 */
				List<Message> messages = new ArrayList<Message>();
				// 只有当集合长度为1，并且schoolId为空的时候，说明该用户没有绑定学校，需要找绑定关系
				if (userList.size() == 1 && StringUtil.isEmpty(userList.get(0).getSchoolId()))
				{
					Integer relate_userId = userService.getPalmUserIdbyUserId(userList.get(0).getUserId());
					if (relate_userId != null)
					{
						// 亦信用户 绑定了智慧校园用户,如下执行过程↓
						List<User> bindUserList = userService.getUserByUserId(relate_userId);
						List<Message> messages_WISDOMCAMPUS = getMySmsListInWISDOMCAMPUS(bindUserList, currentPage);
						List<Message> messages_RICHBOOK = getMySmsListInRICHBOOK(userList.get(0), currentPage);
						messages.addAll(messages_WISDOMCAMPUS);
						messages.addAll(messages_RICHBOOK);
					} else
					{
						// 亦信用户没有绑定智慧校园用户,如下执行过程↓
						List<Message> messages_RICHBOOK = getMySmsListInRICHBOOK(userList.get(0), currentPage);
						messages.addAll(messages_RICHBOOK);
					}
				} else
				{
					// 智慧校园用户 如下执行过程↓
					List<Message> messages_WISDOMCAMPUS = getMySmsListInWISDOMCAMPUS(userList, currentPage);
					List<Message> messages_RICHBOOK = getMySmsListInRICHBOOK(userList.get(0), currentPage);
					messages.addAll(messages_WISDOMCAMPUS);
					messages.addAll(messages_RICHBOOK);
				}
				// 按短信发送时间排序
				Collections.sort(messages, new Comparator<Message>()
				{
					@Override
					public int compare(Message o1, Message o2)
					{
						return o2.ReceiveTime.compareTo(o1.ReceiveTime);
					}

				});
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "获取成功";
				richHttpResponse.ResponseObject = messages;
			} else
			{
				richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			}
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/** 获取智慧校园这一侧的收件箱 */
	@SuppressWarnings("unchecked")
	private List<Message> getMySmsListInWISDOMCAMPUS(List<User> userList, Integer currentPage)
	{
		List<Message> messages = new ArrayList<Message>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 智慧校园这一侧：获取自己的接受的短信，假如是家长就需要获取小孩接受的所有短信
		for (User loginUser : userList)
		{
			if (loginUser.getSchoolId() != null)
			{
				// 这边是利用apikey 进行模拟登录
				loginUser = userService.setRoleAndPermissionToUser(loginUser);
				List<Role> roleList = loginUser.getRoleList();

				List<Integer> ids = new ArrayList<Integer>();
				for (Role role : roleList)
				{
					if (role.getRoleCode().equals("parent"))
					{
						// 家长通过userId,查询他所有小孩的id
						List<User> allChildren = commonService.getAllChildrenByUserId(loginUser.getUserId());
						for (User children : allChildren)
						{
							// 添加小孩的userId，因为给家长发的短息都是存的小孩userId
							ids.add(children.getUserId());
						}
						break;
					}
				}
				// 其余人员都已userId
				ids.add(loginUser.getUserId());
				paramMap.put("receiverIdList", ids);
				this.getPages().setPageSize(6);
				Pages<SmsDetail> pages = smsService.getAcceptSmsList(this.getPages().getPageSize(), currentPage, paramMap);
				List<SmsDetail> smsDetaillist = pages.getList();
				for (SmsDetail smsDetail : smsDetaillist)
				{
					Message message = new Message();
					message.MessageId = String.valueOf(smsDetail.getId());
					message.Content = smsDetail.getContent();
					message.SenderName = smsDetail.getSenderName();
					message.ReceiveTime = smsDetail.getSentTime();
					messages.add(message);
				}
			}
		}
		return messages;
	}

	/** 获取亦信这一侧的收件箱 */
	@SuppressWarnings("unchecked")
	private List<Message> getMySmsListInRICHBOOK(User user, Integer currentPage)
	{
		List<Message> messages = new ArrayList<Message>();
		Map<String, Object> paramMap = new HashMap<String, Object>();

		List<Integer> ids_richbook = new ArrayList<Integer>();
		ids_richbook.add(user.getUserId());
		paramMap.put("receiverIdList", ids_richbook);
		this.getPages().setPageSize(4);
		Pages<SmsDetail> pages = smsService.getAcceptSmsListFromPlatform(this.getPages().getPageSize(), currentPage, paramMap);
		List<SmsDetail> smsDetaillist = pages.getList();
		for (SmsDetail smsDetail : smsDetaillist)
		{
			Message message = new Message();
			message.MessageId = String.valueOf(smsDetail.getId());
			message.Content = smsDetail.getContent();
			message.SenderName = smsDetail.getSenderName();
			message.ReceiveTime = smsDetail.getSentTime();
			messages.add(message);
		}
		return messages;
	}

	/**
	 * 信息发件箱
	 */
	@RequestMapping(value = "/getSendSmsList.do")
	public String getSendSmsList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		RichHttpResponse<List<Message>> richHttpResponse = new RichHttpResponse<List<Message>>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		try
		{
			String apiKey = request.getParameter("apiKey");
			List<User> userList = userService.getUserByApiKey(apiKey);
			// 分页信息
			Integer currentPage = 1;
			try
			{
				currentPage = Integer.valueOf(request.getParameter("cPage"));
			} catch (Exception e)
			{
				currentPage = 1;
			}

			if (userList != null & userList.size() > 0)
			{
				List<Message> messages = new ArrayList<Message>();
				// 只有当集合长度为1，并且schoolId为空的时候，说明该用户没有绑定学校，需要找绑定关系
				if (userList.size() == 1 && StringUtil.isEmpty(userList.get(0).getSchoolId()))
				{
					Integer relate_userId = userService.getPalmUserIdbyUserId(userList.get(0).getUserId());
					if (relate_userId != null)
					{
						// 亦信用户 绑定了智慧校园用户,如下执行过程↓
						List<User> bindUserList = userService.getUserByUserId(relate_userId);
						List<Message> messages_WISDOMCAMPUS = getSendSmsInWISDOMCAMPUS(bindUserList, currentPage);
						List<Message> messages_RICHBOOK = getSendSmsInRICHBOOK(userList.get(0), currentPage);
						messages.addAll(messages_WISDOMCAMPUS);
						messages.addAll(messages_RICHBOOK);
					} else
					{
						// 亦信用户没有绑定智慧校园用户,如下执行过程↓
						List<Message> messages_RICHBOOK = getSendSmsInRICHBOOK(userList.get(0), currentPage);
						messages.addAll(messages_RICHBOOK);
					}
				} else
				{
					// 智慧校园用户 如下执行过程↓
					List<Message> messages_WISDOMCAMPUS = getSendSmsInWISDOMCAMPUS(userList, currentPage);
					List<Message> messages_RICHBOOK = getSendSmsInRICHBOOK(userList.get(0), currentPage);
					messages.addAll(messages_WISDOMCAMPUS);
					messages.addAll(messages_RICHBOOK);
				}

				// 按短信发送时间排序
				Collections.sort(messages, new Comparator<Message>()
				{
					@Override
					public int compare(Message o1, Message o2)
					{
						return o2.SendTime.compareTo(o1.SendTime);
					}

				});
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "获取成功";
				richHttpResponse.ResponseObject = messages;
			} else
			{
				richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			}
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** 获取智慧校园这一侧的发件箱 */
	@SuppressWarnings("unchecked")
	private List<Message> getSendSmsInWISDOMCAMPUS(List<User> userList, Integer currentPage)
	{
		List<Message> messages = new ArrayList<Message>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 智慧校园这一侧：获取自己的发送的短信
		for (User loginUser : userList)
		{
			if (loginUser.getSchoolId() != null)
			{
				// 这边是利用apikey 进行模拟登录
				loginUser = userService.setRoleAndPermissionToUser(loginUser);
				List<Role> roleList = loginUser.getRoleList();

				paramMap.put("roleCode", "other");
				for (Role role : roleList)
				{
					// 判断是否是管理员，因为管理可以看到所有人发的信息，其余只能看到自己发得短信
					String roleCode = role.getRoleCode();
					if (roleCode.equals("admin") || roleCode.equals("president") || roleCode.equals("chairman"))
					{
						paramMap.put("roleCode", roleCode);
						break;
					}
				}
				paramMap.put("userId", loginUser.getUserId());
				this.getPages().setPageSize(6);
				Pages<Sms> pages = smsService.loadSmsList(this.getPages().getPageSize(), currentPage, paramMap, loginUser);
				List<Sms> smsList = pages.getList(); // 获取后台查询到已发送短信列表

				// 重新包装返回给手机端的已发送短信列表
				for (Sms sms : smsList)
				{
					Message message = new Message();
					message.MessageId = String.valueOf(sms.getId());
					message.Content = sms.getContent();
					message.SendTime = sms.getSentTime();
					message.SenderName = sms.getSenderName();
					message.sendType = sms.getType();
					message.SchoolId = Long.valueOf(loginUser.getSchoolId());
					messages.add(message);
				}
			}
		}
		return messages;
	}

	/** 获取亦信这一侧的发件箱 */
	@SuppressWarnings("unchecked")
	private List<Message> getSendSmsInRICHBOOK(User user, Integer currentPage)
	{
		List<Message> messages = new ArrayList<Message>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", user.getUserId());
		this.getPages().setPageSize(4);
		Pages<Sms> pages = smsService.loadSmsListFromPlatform(this.getPages().getPageSize(), currentPage, paramMap);
		List<Sms> smsList = pages.getList(); // 获取后台查询到已发送短信列表
		for (Sms sms : smsList)
		{
			Message message = new Message();
			message.MessageId = String.valueOf(sms.getId());
			message.Content = sms.getContent();
			message.SendTime = sms.getSentTime();
			message.SenderName = sms.getSenderName();
			message.sendType = sms.getType();
			messages.add(message);
		}
		return messages;
	}

	/**
	 * 信息详情
	 */
	@RequestMapping(value = "/getSendSmsDetail.do")
	public String getSendSmsDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		RichHttpResponse<Message> richHttpResponse = new RichHttpResponse<Message>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		try
		{
			String apiKey = request.getParameter("apiKey");
			String schoolId = request.getParameter("schoolId");
			String sendType = request.getParameter("sendType");
			if (!StringUtil.isEmpty(sendType))
			{
				Integer messageId = Integer.parseInt(request.getParameter("messageId"));
				Message message = new Message();
				List<SmsDetail> smsDetailList = new ArrayList<SmsDetail>();
				// 好友短信短信类型，-1:好友短信和在线留言 -2:好友在线留言 -3:个人分发的短息
				int sendType_int = 0;
				try{
					sendType_int = Integer.valueOf(sendType).intValue();
				}catch(Exception e){
					User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
					log.error("亦信端查看发送详情出错，错误信息:"+e.getMessage());
					log.error("亦信端查看发送详情出错，查看人信息:"+schoolId+":"+loginUser.getUserId()+","+loginUser.getRealName());
					richHttpResponse.ResponseCode = 0;
					richHttpResponse.ResponseResult = "获取成功";
					richHttpResponse.ResponseObject = message;
					String json = GsonHelper.toJson(richHttpResponse);
					response.getWriter().write(json);
					response.getWriter().flush();
					return null;
				}
				
				if (sendType_int < 0)
				{// 亦信好友短信详情
					List<User> loginUserList = userService.getUserByApiKey(apiKey);
					if (loginUserList != null && loginUserList.size() > 0)
					{
						smsDetailList = smsService.loadSmsDetailFromPlatform(messageId);
					} else
					{
						richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
						richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;

						String json = GsonHelper.toJson(richHttpResponse);
						response.getWriter().write(json);
						response.getWriter().flush();
						return null;
					}
				} else
				{// 智慧校园短信详情
					// 这边是利用apikey 进行模拟登录
					User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
					if (loginUser != null)
					{
						smsDetailList = smsService.loadSmsDetail(messageId);
					} else
					{
						richHttpResponse.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
						richHttpResponse.ResponseResult = Cons.LOGIN_APIKEY_INVALID;

						String json = GsonHelper.toJson(richHttpResponse);
						response.getWriter().write(json);
						response.getWriter().flush();
						return null;
					}
				}
				if (smsDetailList != null && smsDetailList.size() > 0)
				{
					SmsDetail smsDetail_one = smsDetailList.get(0);
					// 发送0:成功 1：发送中 2:发送失败
					message.Status = 0;

					if ("1".equals(smsDetail_one.getIsRealTime()))
					{
						// 立即发送
						message.sendType = "实时发送";
					} else
					{
						// 定时发送
						message.sendType = "定时发送";
					}

					// 收信人列表
					StringBuffer sbBuffer = new StringBuffer();
					int count = 0;
					HashMap<String, Object> palmUserMap = new HashMap<String, Object>();
					for (SmsDetail smsDetail : smsDetailList)
					{
						if (!palmUserMap.containsKey(smsDetail.getReceiverId()))
						{
							sbBuffer.append(smsDetail.getReceiverName() + ",");
							palmUserMap.put(String.valueOf(smsDetail.getReceiverId()), "");
							count++;
						}
					}
					message.Receiver = sbBuffer.toString().substring(0, sbBuffer.toString().length() - 1);
					message.ReceiverCount = count;
				}
				richHttpResponse.ResponseCode = 0;
				richHttpResponse.ResponseResult = "获取成功";
				richHttpResponse.ResponseObject = message;
			}
			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/getSmsWordLimt.do")
	public String getSmsWordLimt(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		RichHttpResponse<Integer> richHttpResponse = new RichHttpResponse<Integer>();
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		try
		{
			Integer smsWordLimit = 190;
			String schoolId = request.getParameter("schoolId");
			Properties pro = PropertiesUtil.getInstance().read("wisdomcampus.properties");
			smsWordLimit = PropertiesUtil.getInstance().getSmsWordLimit(pro, schoolId);

			richHttpResponse.ResponseCode = 0;
			richHttpResponse.ResponseResult = "获取成功";
			richHttpResponse.ResponseObject = smsWordLimit;

			String json = GsonHelper.toJson(richHttpResponse);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	
}