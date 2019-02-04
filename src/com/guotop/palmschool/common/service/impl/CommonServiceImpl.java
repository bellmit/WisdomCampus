package com.guotop.palmschool.common.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

import com.baidu.yun.push.exception.PushClientException;
import com.baidu.yun.push.exception.PushServerException;
import com.guotop.palmschool.common.entity.Bill;
import com.guotop.palmschool.common.entity.ResultInfo;
import com.guotop.palmschool.common.entity.SmsMT;
import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.constant.Cons;
import com.guotop.palmschool.constant.Cons.PUSHTYPE;
import com.guotop.palmschool.entity.Card;
import com.guotop.palmschool.entity.Clazz;
import com.guotop.palmschool.entity.Department;
import com.guotop.palmschool.entity.Device;
import com.guotop.palmschool.entity.FriendSms;
import com.guotop.palmschool.entity.FriendSmsDetail;
import com.guotop.palmschool.entity.Grade;
import com.guotop.palmschool.entity.Inout;
import com.guotop.palmschool.entity.OrderMessage;
import com.guotop.palmschool.entity.OrderMessageUser;
import com.guotop.palmschool.entity.OrderUserMessageDetail;
import com.guotop.palmschool.entity.Permission;
import com.guotop.palmschool.entity.Pushmessage;
import com.guotop.palmschool.entity.Role;
import com.guotop.palmschool.entity.RolePermission;
import com.guotop.palmschool.entity.School;
import com.guotop.palmschool.entity.SchoolInfo;
import com.guotop.palmschool.entity.SchoolTrySms;
import com.guotop.palmschool.entity.Student;
import com.guotop.palmschool.entity.StudentClazz;
import com.guotop.palmschool.entity.StudentDetail;
import com.guotop.palmschool.entity.SysMessage;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.entity.UserAccount;
import com.guotop.palmschool.entity.UserAccountHistory;
import com.guotop.palmschool.entity.UserDepartment;
import com.guotop.palmschool.entity.UserForwardsmsSwitch;
import com.guotop.palmschool.entity.UserPermission;
import com.guotop.palmschool.entity.UserRole;
import com.guotop.palmschool.entity.Ykt;
import com.guotop.palmschool.entity.YktPlace;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.rest.entity.PushDataByJson;
import com.guotop.palmschool.rest.entity.PushItem;
import com.guotop.palmschool.service.BaseService;
import com.guotop.palmschool.service.CardService;
import com.guotop.palmschool.service.ClazzService;
import com.guotop.palmschool.service.OrderMessageService;
import com.guotop.palmschool.service.PushmessageService;
import com.guotop.palmschool.service.SchoolService;
import com.guotop.palmschool.service.SysMessageService;
import com.guotop.palmschool.service.UserAccountService;
import com.guotop.palmschool.service.UserForwardsmsSwitchService;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.shopping.service.OrdersService;
import com.guotop.palmschool.util.Pages;
import com.guotop.palmschool.util.PreciseCompute;
import com.guotop.palmschool.util.SDKSubAccount;
import com.guotop.palmschool.util.SmsUtil;
import com.guotop.palmschool.util.StringUtil;
import com.guotop.palmschool.util.TimeUtil;
import com.whty.aam.client.util.HttpUtil;
import com.whty.apigateway.security.EncryptionUtils;
import com.whty.apigateway.security.login.Constant;
import com.whty.apigateway.security.login.EncryptUtils;

import dev.gson.GsonHelper;

/**
 * 通用业务类实现类
 * 
 * @author zhou
 */
@Service("commonService")
public class CommonServiceImpl extends BaseService implements CommonService
{
	private Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);
	@Resource
	private SchoolService schoolService;
	@Resource
	private SysMessageService sysMessageService;
	@Resource
	private UserService userService;
	@Resource
	private UserAccountService userAccountService;
	@Resource
	private UserForwardsmsSwitchService userForwardsmsSwitchService;
	@Resource
	private CommonService commonService;
	@Resource
	private OrderMessageService orderMessageService;
	@Resource
	private CardService cardService;
	@Resource
	private OrdersService ordersService;
	@Resource
	private ClazzService clazzService;
	@Resource
	private PushmessageService pushmessageService;
	
	@Autowired
	private ThreadPoolTaskExecutor poolTaskExecutor;
	
	
	/**
	 * 根据用户Id查找用户详细信息 20151127
	 * 
	 * @param userId
	 *            用户Id
	 * @return 查出用户的权限 部门信息 年级信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectUserInfoByUserId(Integer userId)
	{
		return (List<User>) getBaseDao().selectList("User.selectUserInfoByUserId", userId);
	}

	/**
	 * 查询部门信息
	 * 
	 * @return 该学校下所有的部门信息
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentList()
	{
		return getBaseDao().selectListBySql("Depart.getDepartList");
	}

	/**
	 * 根据学校ID 和 roleID查找属于哪个年级
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param roleId
	 *            角色ID
	 * @return 该角色能看到的所有年级
	 */
	@SuppressWarnings("unchecked")
	public List<Grade> selectGradeListBySchoolIdAndRoleId(String schoolId, String roleId)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("schoolId", schoolId);
		paramMap.put("roleId", roleId);
		return getBaseDao().selectListByObject("Grade.selectGradeListByRoleId", paramMap);
	}

	/**
	 * 根据年级ID查找该年级下所有教师的信息
	 * 
	 * @param gradeId
	 *            年级ID
	 * @return 年级ID对应年级下所有的教师信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectTeacherListByGradeId(Integer gradeId)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("gradeId", gradeId.toString());
		return getBaseDao().selectListByObject("User.selectTeacherListByGradeId", paramMap);
	}

	/**
	 * 根据部门ID查找该部门下所有教师的信息 20151127 tao
	 * 
	 * @param departmentId
	 *            部门ID
	 * @return 部门ID对应部门下所有的教师信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherListByDepartmentId(Integer departmentId)
	{

		return getBaseDao().selectList("User.getTeacherDetailByDepartmentId", departmentId);
	}

	/**
	 * 根据年级id查找班级列表
	 * 
	 * @param gradeId
	 *            年级id
	 * @return 班级列表
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> selectClazzListByGradeId(Integer gradeId)
	{
		return getBaseDao().selectListByObject("Clazz.selectClazzListByGradeId", gradeId);
	}

	/**
	 * 根据班级Id查找该班级所有学生集合
	 * 
	 * @param clazzId
	 *            班级Id
	 * @return 改班级下所有学生集合
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectStudentListByClazzId(Integer clazzId)
	{
		return getBaseDao().selectListByObject("Clazz.selectStudentListByClazzId", clazzId);
	}

	@SuppressWarnings("unchecked")
	public List<User> selectStudentListByClazzId1(Integer clazzId)
	{
		return getBaseDao().selectListByObject("Clazz.selectStudentListByClazzId1", clazzId);
	}

	/**
	 * 根据学生Id查找该学生的所有信息
	 * 
	 * @Id 学生Id
	 * @return 该学生的详细信息
	 */
	public StudentDetail selectStudentDetailByStudentId(Integer id)
	{
		return (StudentDetail) getBaseDao().selectObjectByObject("Student.selectStudentDetailByStudentId", id);
	}

	/**
	 * 根据homeId查找除了自己的所有家庭成员
	 * 
	 * @param paramMap
	 *            参数列表
	 * @return 所有的家庭成员
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectUserListByHomeId(Map<String, Object> paramMap)
	{
		return getBaseDao().selectListByObject("User.selectUserListByHomeId", paramMap);
	}

	/**
	 * 根据用户ID从用户List中选到指定的用户
	 * 
	 * @param userList
	 *            源userList
	 * @param userId
	 *            用户ID
	 * @param roleId
	 *            用户权限ID
	 * @return User 指定用户
	 */
	public User selectUserFromSessionByUserId(List<User> userList, Integer userId, String roleCode)
	{
		if (null != userList && userList.size() == 1)
		{
			return userList.get(0);
		}

		for (User user : userList)
		{
			if (user.getUserId().equals(userId) && "0".equals(roleCode))
			{
				return user;
			}
		}
		return new User();
	}

	/**
	 * 查询所有学校信息
	 * 
	 * @return 所有的学校信息列表
	 */
	@SuppressWarnings("unchecked")
	public List<School> selectAllSchoolList()
	{
		return getBaseDao().selectListBySql("School.selectSchoolList");
	}

	/**
	 * 查询所有班级信息
	 * 
	 * @return 所有的班级信息列表
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> selectAllClazzList()
	{
		return getBaseDao().selectListBySql("Clazz.selectAllClazzList");
	}

	/**
	 * 根据roleId查找年级组长
	 * 
	 * @return 所有的年级组长信息列表
	 */
	@SuppressWarnings("unchecked")
	public List<UserRole> selectGradeLeaderListByRoleId()
	{
		return getBaseDao().selectListBySql("UserRole.selectGradeLeaderListByRoleId");
	}

	/**
	 * 根据roleId查找班主任
	 * 
	 * @return 所有的班主任信息列表
	 */
	@SuppressWarnings("unchecked")
	public List<UserRole> selectClazzLeaderListByRoleId()
	{
		return getBaseDao().selectListBySql("UserRole.selectClazzLeaderListByRoleId");
	}

	/**
	 * 根据roleId查找角色
	 * 
	 * @return 角色信息列表
	 */
	@SuppressWarnings("unchecked")
	public List<Role> getRoleList()
	{
		return getBaseDao().selectListBySql("User.getRoleList");
	}

	/**
	 * 修改权限
	 **/
	public void modifyRoleById(Map<String, Object> paramMap)
	{
		getBaseDao().updateObject("UserRole.modifyRoleById", paramMap);
	}

	/**
	 * 添加卡号（添加用户时没有添加卡， 修改用户时添加卡号）
	 * 
	 * @param
	 */
	public void addCardCode(Map<String, Object> paramMap, Card card)
	{
		String code = card.getCardCode();

		if (!"".equals(code))
		{
			Integer cardId = getBaseDao().addObject("Card.addCardTs", card);
			paramMap.put("cardid", cardId);
			getBaseDao().addObject("User.addUserCard", paramMap);
		}
	}

	/**
	 * 根据userId 和roleId 获取 gradeId 和classId
	 * 
	 * @param userId
	 *            用户id
	 * @param roleId
	 *            权限id
	 */
	public UserRole selectUserRoleByUserIdAndroleId(Map<String, Object> paramMap)
	{

		return (UserRole) getBaseDao().selectObjectByObject("UserRole.selectUserRoleByUserIdAndRoleId", paramMap);
	}

	/**
	 * 根据clazzId 查询所有老师
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectTeacherListByClazzId(Integer clazzId)
	{
		return getBaseDao().selectListByObject("Clazz.selectTeachertListByClazzId", clazzId);
	}

	/**
	 * 根据id查出持有者
	 * 
	 * @param id
	 * 
	 * @return 持有者
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectTeacherInfoForTeacherDetail(Integer id)
	{
		return getBaseDao().selectListByObject("User.selectTeacherInfoForTeacherDetail", id);
	}

	/**
	 * 根据老师的id查询老师的职务信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> loadTeacherOfJobDetailInfo(Integer id)
	{
		return getBaseDao().selectList("User.loadTeacherOfJobDetailInfo", id);
	}

	/**
	 * 查询学校学生总数
	 * 
	 * @return 学校学生总数
	 */
	public Integer selectStudentCountBySchoolScale()
	{
		return (Integer) getBaseDao().selectObject("User.selectStudentCountBySchoolScale", null);
	}

	/**
	 * 查询学校教师总数
	 * 
	 * @return 学校教师总数
	 */
	public Integer getTeacherCount()
	{
		return (Integer) getBaseDao().selectObject("User.getTeacherCount", null);
	}

	/**
	 * 根据学校的id查询部门 分页显示
	 * 
	 * id 学校id
	 */
	@SuppressWarnings("unchecked")
	public Pages loadDepartList(int pageSize, int page, Map<String, Object> paramMap)
	{

		int allRow = 0;
		int currentPage = 0;
		int totalPage = 0;
		List<Department> list = new ArrayList<Department>();

		allRow = this.getBaseDao().getAllRowCountByCondition("Depart.loadDepartListByQueryContent", paramMap);
		totalPage = Pages.countTotalPage(pageSize, allRow);

		final int offset = Pages.countOffset(pageSize, page);
		final int length = pageSize;
		currentPage = Pages.countCurrentPage(page);
		list = this.getBaseDao().queryForPageByCondition("Depart.loadDepartListByQueryContent", paramMap, offset, length);

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
	 * 查询部门
	 * 
	 * 20151126 tao
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartList()
	{
		return getBaseDao().selectListBySql("Depart.getDepartList");
	}

	/**
	 * 查询部门 paramMap 需要包含 User userId permissionCode
	 * 
	 * 20151204 shengyinjiang
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartList(Map<String, Object> paramMap)
	{
		User user = (User) paramMap.get("user");

		List<Department> departmentList = new ArrayList<Department>();
		/*
		 * 学校超级管理员权限 董事长 和校长权限 看到内容是所有的部门
		 */
		if (hasAdminPermission(user))
		{
			departmentList = getBaseDao().selectListBySql("Depart.getDepartList");
		} else
		{
			departmentList = getBaseDao().selectListByObject("UserPermission.getDepartmentListByUserIdAndPermissionCode", paramMap);
		}

		return departmentList;
	}

	/**
	 * 查询部门 paramMap 需要包含 User userId permissionCode【福建专用】
	 * 
	 * 20151204 shengyinjiang
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartListFj(Map<String, Object> paramMap)
	{
		User user = (User) paramMap.get("user");

		List<Department> departmentList = new ArrayList<Department>();
		/*
		 * 学校超级管理员权限 董事长 和校长权限 看到内容是所有的部门
		 */
		if (hasAdminPermissionFj(user))
		{
			departmentList = getBaseDao().selectListBySql("Depart.getDepartListFj");
		} else
		{
			departmentList = getBaseDao().selectListByObject("UserPermission.getDepartmentListByUserIdAndPermissionCodeFj", paramMap);
		}

		return departmentList;
	}

	/**
	 * 根据部门的id查询对应的教师 20151127 tao
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherDetailByDepartmentId(Integer departmentId)
	{
		return getBaseDao().selectListByObject("User.getTeacherDetailByDepartmentId", departmentId);
	}

	/**
	 * 根据部门的id查询对应的教师 20151127 tao
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherDetailByDepartmentIdFj(Integer departmentId)
	{
		return getBaseDao().selectListByObject("User.getTeacherDetailByDepartmentIdFj", departmentId);
	}

	/**
	 * 根据部门的id查询对应的教师(资源云接口)
	 */
	@SuppressWarnings("unchecked")
	public List<User> getCloudTeacherByDepartmentId(Map<String, Object> map)
	{
		return getBaseDao().selectListByObject("User.getCloudTeacherByDepartmentId", map);
	}

	/**
	 * 根据部门的id查询对应的教师 20151208 syj
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherDetailByDepartmentIdInSms(Integer departmentId)
	{
		return getBaseDao().selectListByObject("User.getTeacherDetailByDepartmentIdInSms", departmentId);
	}

	/**
	 * 根据部门的id查询对应的教师【福建专用】
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherDetailByDepartmentIdInSmsFj(Integer departmentId)
	{
		return getBaseDao().selectListByObject("User.getTeacherDetailByDepartmentIdInSmsFj", departmentId);
	}

	@SuppressWarnings("unchecked")
	public List<User> getTeacherListByDepartmentIdInRICHBOOK(Integer departmentId)
	{
		return getBaseDao().selectListByObject("User.getTeacherListByDepartmentIdInRICHBOOK", departmentId);
	}

	/**
	 * 根据用户名查询用户 name 用户名
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectUserByName(ModelMap modelMap)
	{
		return (List<User>) getBaseDao().selectListByObject("User.selectUserByName", modelMap);
	}

	/**
	 * 加载所有的教师信息, 用于教师考勤
	 * 
	 * @return 所有教师信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAllTeacherListForCheck()
	{
		return getBaseDao().selectListBySql("User.selectAllTeacherListForCheck");
	}

	/**
	 * 上午---begin
	 */
	/*
	 * 上午旷工
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmTeacherKG()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherKG", paramMap);

		return teacherList;
	}

	/*
	 * 上午迟到
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmTeacherLate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 8); // 时
		cal.set(Calendar.MINUTE, 1); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 11); // 时
		cal.set(Calendar.MINUTE, 20); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLate", paramMap);

		return teacherList;
	}

	/*
	 * 上午早退
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmTeacherLeave()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 8); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 11); // 时
		cal.set(Calendar.MINUTE, 20); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLeave", paramMap);

		return teacherList;
	}

	/*
	 * 上午正常下班
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmTeacherLeaveNormal()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 11); // 时
		cal.set(Calendar.MINUTE, 20); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLeaveNormal", paramMap);

		return teacherList;
	}

	/*
	 * 上午正常上班
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmTeacherNormal()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 8); // 时
		cal.set(Calendar.MINUTE, 5); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherNormal", paramMap);

		return teacherList;
	}

	/*
	 * 上午非正常进校
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmAbnormalIn()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectAbnormalIn", paramMap);

		return teacherList;
	}

	/*
	 * 上午非正常出校
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectAmAbnormalOut()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectAbnormalOut", paramMap);

		return teacherList;
	}

	/**
	 * 上午---end
	 */

	/**
	 * 下午---begin
	 */
	/*
	 * 下午旷工
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmTeacherKG()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 22); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherKG", paramMap);

		return teacherList;
	}

	/*
	 * 下午迟到
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmTeacherLate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 14); // 时
		cal.set(Calendar.MINUTE, 1); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 15); // 时
		cal.set(Calendar.MINUTE, 40); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLate", paramMap);

		return teacherList;
	}

	/*
	 * 下午早退
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmTeacherLeave()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 14); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 15); // 时
		cal.set(Calendar.MINUTE, 40); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLeave", paramMap);

		return teacherList;
	}

	/*
	 * 下午正常下班
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmTeacherLeaveNormal()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 15); // 时
		cal.set(Calendar.MINUTE, 40); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 23); // 时
		cal.set(Calendar.MINUTE, 59); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherLeaveNormal", paramMap);

		return teacherList;
	}

	/*
	 * 下午正常上班
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmTeacherNormal()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 14); // 时
		cal.set(Calendar.MINUTE, 5); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectTeacherNormal", paramMap);

		return teacherList;
	}

	/*
	 * 下午非正常进校
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmAbnormalIn()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 23); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectAbnormalIn", paramMap);

		return teacherList;
	}

	/*
	 * 下午非正常出校
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectPmAbnormalOut()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		/**
		 * 时间区间:
		 */

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 13); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String beginTime = formatter.format(cal.getTime());

		cal.set(Calendar.HOUR_OF_DAY, 23); // 时
		cal.set(Calendar.MINUTE, 0); // 分
		cal.set(Calendar.SECOND, 0); // 秒
		cal.set(Calendar.MILLISECOND, 0); // 毫秒
		String endTime = formatter.format(cal.getTime());

		paramMap.put("beginTime", beginTime);
		paramMap.put("endTime", endTime);

		List<User> teacherList = getBaseDao().selectListByObject("User.selectAbnormalOut", paramMap);

		return teacherList;
	}

	/**
	 * 上午---end
	 */

	/**
	 * 通过姓名及学校编码获取学生信息顺带出家长的信息
	 * 
	 * @param realName
	 *            真实姓名
	 * @return 返回值 add by shengyinjiang 20160902
	 */
	@SuppressWarnings("unchecked")
	public List<User> getStudentByRealName(String realName, long schoolId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("realName", realName);
		map.put("schoolId", schoolId);
		List<User> userList = getBaseDao().selectListByObject("User.getStudentByRealName", map);
		return userList;
	}

	@SuppressWarnings("unchecked")
	public List<User> getUserByRealName(String realName, long schoolId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("realName", realName);
		map.put("schoolId", schoolId);
		List<User> userList = getBaseDao().selectListByObject("User.getUserByRealName", map);
		return userList;
	}

	/**
	 * 通过 code name 来查找该职工
	 */
	public User selectTeacherByCodeAndName(ModelMap modelMap)
	{
		return (User) getBaseDao().selectObjectByObject("User.selectUserByCodeAndName", modelMap);
	}

	/**
	 * 加载所有的教师信息, 用于选择年级主任和班主任
	 * 
	 * @return 所有教师信息 update by shengyinjiang 20151126
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllTeacherList()
	{
		return getBaseDao().selectListBySql("User.getAllTeacherList");
	}

	/**
	 * 添加用户角色表，并且添加用户权限表 modelMap 中需要包含 userId和roleCode update by shengyinjiang
	 * 20151129
	 */
	public void addUserRole(HashMap<String, Object> modelMap)
	{
		// 获取该角色下所有的权限
		String roleCode = (String) modelMap.get("roleCode");
		String optClazzId = String.valueOf(modelMap.get("optClazzId"));
		Integer userId = StringUtil.toint(modelMap.get("userId"));
		if ("null".equals(optClazzId))
		{
			optClazzId = null;
		}
		String optDepartId = String.valueOf(modelMap.get("optDepartId"));
		if ("null".equals(optDepartId))
		{
			optDepartId = null;
		}
		// 判断是否已经有该角色
		List<UserRole> userRoleList = getUserRoleByUserIdAndRoleCode(modelMap);
		if (CollectionUtils.isEmpty(userRoleList))
		{
			// 保存userRole关系
			getBaseDao().addObject("UserRole.addUserRole", modelMap);

			List<RolePermission> rolePermissionList = getPermissionByRoleCode(roleCode);
			List<UserPermission> userPermissionList = new ArrayList<UserPermission>();
			if (!CollectionUtils.isEmpty(rolePermissionList))
			{
				for (RolePermission rolePermission : rolePermissionList)
				{
					modelMap.put("permissionCode", rolePermission.getPermissionCode());
					/*
					 * 添加user_permission
					 */
					UserPermission up = new UserPermission();
					up.setUserId(userId);
					up.setPermissionCode(rolePermission.getPermissionCode());
					if (!StringUtil.isEmpty(optClazzId))
					{
						up.setOptClazzId(String.valueOf(optClazzId));
					}
					if (!StringUtil.isEmpty(optDepartId))
					{
						up.setOptDepartId(String.valueOf(optDepartId));
					}
					up.setRoleCode(roleCode);
					userPermissionList.add(up);
				}
			}
			if (!CollectionUtils.isEmpty(userPermissionList))
			{
				addUserPermission(userPermissionList);
			}
		} else
		{
			List<UserPermission> userPerList = getUserPermissionGroupByOptClazzId(userId, roleCode);
			if (userPerList.size() == 1 && StringUtil.isEmpty(userPerList.get(0).getOptClazzId()))
			{
				// 如果只有一条记录 且 optClazzId为空 则直接update 因为修改班主任的之前，该人员必须现有班主任权限
				updateUserPermissionByUserId(modelMap);
			} else
			{
				List<RolePermission> rolePermissionList = getPermissionByRoleCode(roleCode);
				List<UserPermission> userPermissionList = new ArrayList<UserPermission>();
				if (!CollectionUtils.isEmpty(rolePermissionList))
				{
					for (RolePermission role : rolePermissionList)
					{
						modelMap.put("permissionCode", role.getPermissionCode());
						UserPermission up = new UserPermission();
						up.setUserId(userId);
						up.setPermissionCode(role.getPermissionCode());
						if (!StringUtil.isEmpty(optClazzId))
						{
							up.setOptClazzId(String.valueOf(optClazzId));
						}
						if (!StringUtil.isEmpty(optDepartId))
						{
							up.setOptDepartId(String.valueOf(optDepartId));
						}
						up.setRoleCode(roleCode);
						userPermissionList.add(up);
					}
					if (!CollectionUtils.isEmpty(userPermissionList))
					{
						addUserPermission(userPermissionList);
					}
				}
			}
		}
	}

	/**
	 * 获取用户角色,通过userId 和roleCode 主要作用查看该用户是否拥有该角色
	 * 
	 * update by shengyinjiang 20151129
	 */
	@SuppressWarnings("unchecked")
	public List<UserRole> getUserRoleByUserIdAndRoleCode(HashMap<String, Object> modelMap)
	{
		List<UserRole> userRoleList = (List<UserRole>) getBaseDao().selectListByObject("UserRole.getUserRoleByUserIdAndRoleCode", modelMap);
		return userRoleList;
	}

	/**
	 * 通过角色获取该角色对于的所有权限
	 * 
	 * update by shengyinjiang 20151129
	 */
	@SuppressWarnings("unchecked")
	public List<RolePermission> getPermissionByRoleCode(String roleCode)
	{
		List<RolePermission> rolePermissionList = getBaseDao().selectListByObject("RolePermission.getPermissionListByRoleCode", roleCode);
		return rolePermissionList;
	}

	/**
	 * 获取用户权限,通过userId 和 PermissionCode 主要作用查看该用户是否拥有该权限
	 * 
	 * update by shengyinjiang 20151129
	 */
	@SuppressWarnings("unchecked")
	public List<UserPermission> getUserPermissionByUserIdAndPermissionCode(HashMap<String, Object> modelMap)
	{
		List<UserPermission> userPermissionList = (List<UserPermission>) getBaseDao()
				.selectListByObject("UserPermission.getUserPermissionByUserIdAndPermissionCode", modelMap);
		return userPermissionList;
	}

	/**
	 * 获取用户权限,通过userId 和 PermissionCode 以及roleCode主要作用查看该用户是否拥有该权限
	 * 
	 * update by shengyinjiang 20160928
	 */
	@SuppressWarnings("unchecked")
	public List<UserPermission> getUserPermissionByUserIdAndPermissionCodeAndRoleCode(HashMap<String, Object> modelMap)
	{
		List<UserPermission> userPermissionList = (List<UserPermission>) getBaseDao()
				.selectListByObject("UserPermission.getUserPermissionByUserIdAndPermissionCodeAndRoleCode", modelMap);
		return userPermissionList;
	}

	/**
	 * 先通过userId roleCode以及permission判断有没有已经存在， 1.不存在的情况下 直接添加 2.存在的情况下
	 * 需要判断clazzId 和 departId是否存在如果已经存在，如果都不存在就更新
	 * 
	 * modelMap 中需要包含
	 * userId,optClazzId(非必需),optDepartId(非必需),permissionCode以及roleCode update
	 * by shengyinjiang 20160928
	 */
	public void addUserPermission(HashMap<String, Object> modelMap)
	{
		// 用来判断是更新还是添加 true:更新 false:添加
		boolean flag = false;
		List<UserPermission> userPermissionList = getUserPermissionByUserIdAndPermissionCodeAndRoleCode(modelMap);
		HashSet<String> optClassIdSet = new HashSet<String>();
		HashSet<String> optDepartIdSet = new HashSet<String>();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		if (CollectionUtils.isEmpty(userPermissionList))
		{
			addUserPermisson(modelMap);
		} else
		{
			for (UserPermission userPermission : userPermissionList)
			{
				if (StringUtil.isEmpty(userPermission.getOptClazzId()) && StringUtil.isEmpty(userPermission.getOptDepartId()))
				{
					flag = true;
					map.put("NULL", userPermission.getId());
				}
				if (!StringUtil.isEmpty(userPermission.getOptClazzId()))
				{
					optClassIdSet.add(userPermission.getOptClazzId() + "");
				}
				if (!StringUtil.isEmpty(userPermission.getOptDepartId()))
				{
					optDepartIdSet.add(userPermission.getOptDepartId() + "");
				}
			}
			String optClazzId = String.valueOf(modelMap.get("optClazzId"));
			if ("null".equals(optClazzId))
			{
				optClazzId = null;
			}
			String optDepartId = String.valueOf(modelMap.get("optDepartId"));
			if ("null".equals(optDepartId))
			{
				optDepartId = null;
			}

			if (StringUtil.isEmpty(optClazzId) && StringUtil.isEmpty(optDepartId))
			{
				// 传入的参数两者都为空的时候不添加，不做任何操作
			} else
			{
				if (flag)
				{
					// 更新
					modelMap.put("id", map.get("NULL"));
					updateUserPermissionById(modelMap);
				} else
				{
					// 添加
					if ((!optClassIdSet.contains(optClazzId)) && (!StringUtil.isEmpty(optClazzId)))
					{
						addUserPermisson(modelMap);
					}
					if ((!optDepartIdSet.contains(optDepartId)) && (!StringUtil.isEmpty(optDepartId)))
					{
						addUserPermisson(modelMap);
					}
				}
			}
		}
	}

	/**
	 * 批量添加
	 * 
	 * @param list
	 */
	public void addUserPermission(List<UserPermission> userPermissionList)
	{

		getBaseDao().addObject("UserPermission.addUserPermissionBATCH", userPermissionList);
	}

	private void addUserPermisson(HashMap<String, Object> modelMap)
	{
		getBaseDao().addObject("UserPermission.addUserPermission", modelMap);
	}

	/**
	 * 根据班主任id查找班级列表
	 * 
	 * @param leaderid
	 *            班主任id
	 * @return 班级列表
	 * 
	 *         update by jfy 20151127
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> getClazzListByLeaderId(Integer leaderId)
	{
		return getBaseDao().selectListByObject("Clazz.getClazzListByLeaderId", leaderId);
	}

	/**
	 * 根据用户Id 获取对应的孩子信息 20151130
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getAllChildrenByUserId(Integer userId)
	{
		return (List<User>) getBaseDao().selectList("User.getAllChildrenByUserId", userId);
	}

	/**
	 * 根据用户Id 获取对应的孩子信息 【福建专用】
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getAllChildrenByUserIdFj(Integer userId)
	{
		return (List<User>) getBaseDao().selectList("User.getAllChildrenByUserIdFj", userId);
	}

	/**
	 * 根据用户Id查找用户对应孩子信息 亦信端
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Student> getAllChildrenByUserIdYixin(Integer userId)
	{
		return (List<Student>) getBaseDao().selectList("User.getAllChildrenByUserIdYixin", userId);
	}

	/**
	 * 根据用户Id查找用户 亦信端
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Student> getUserByUserIdYixin(Integer userId)
	{
		return (List<Student>) getBaseDao().selectList("User.getUserByUserIdYixin", userId);
	}

	/**
	 * 根据用户Id 获取用户信息 20151130
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getUserByUserId(Integer userId)
	{
		return (List<User>) getBaseDao().selectList("User.getUserByUserId", userId);
	}

	/**
	 * 根据班主任id查找班级列表
	 * 
	 * @param leaderid
	 *            班主任id
	 * @return 班级列表
	 * 
	 *         update by jfy 20151201
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> loadClazzListByLeaderId(Integer leaderId)
	{
		return getBaseDao().selectListByObject("Clazz.loadClazzListByLeaderId", leaderId);
	}

	/**
	 * 根据用户手机号及真实姓名查找该用户是否存在
	 *
	 */
	@SuppressWarnings("unchecked")
	public ResultInfo checkUserExistsByPhoneAndName(String phone, String realName)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("phone", phone);
		map.put("realName", realName);
		List<Integer> result = this.getBaseDao().selectListByObject("User.checkUserExistsByPhoneAndName", map);

		ResultInfo resultInfo = new ResultInfo();
		// if (ResultInfo.RESULT_EXISTS.equals(result))
		if (result.size() > 0)
		{
			resultInfo.setResultCode(ResultInfo.RESULT_SUCCESS);
		} else
		{
			resultInfo.setResultCode(ResultInfo.RESULT_ERROR);
		}
		return resultInfo;
	}

	/**
	 * 发送短信 smsId 客户写入的短信ID标识(不能为空 ) sendContent 发送内容 (不能为空) presendTime为空时是实时发送
	 * presendTime 为String的时间时 是定时发送 格式必须为yyyy-MM-dd HH:mm:ss 
	 * type 用于判断发短信时是否推送
	 * type=3时推送 其他的只发短信不推送 update by syj 2015-12-03
	 */
	public boolean sendSmsByDB(String schoolId, PUSHTYPE pushTypeEnum, String phone, Integer userId, String sendContent, String presendTime,
			Integer type)
	{
		
		boolean flag = false;
		try
		{
			User receiver = (User) getBaseDao().selectObject("User.getUserByUserIdForPush", userId);
			commonService.getUserByUserIdForPush(userId);
			if (type == 3)
			{
				try
				{
					// 推送手机端消息
					List<PushItem> list = new ArrayList<PushItem>();
					PushItem pi = new PushItem();
					pi.title = pushTypeEnum.getName();
					pi.PushContent = sendContent;
					pi.PushType = pushTypeEnum.getType();
					pi.PushContentType = pushTypeEnum.getContentType();
					String channelId = receiver.getBaiduChannelId();
					Integer deviceType = receiver.getDeviceType();
					if (!StringUtil.isEmpty(channelId) && deviceType != null && deviceType != 0)
					{
						pi.channels = channelId;
						pi.deviceType = String.valueOf(deviceType);
					}
					pi.receiverId = userId;
					pi.schoolId = schoolId;
					list.add(pi);

					pushMsg(list, false);
				} catch (Exception e)
				{
					logger.error("推送出错:" + e.getMessage());
				}

			}

			// 对sms_mt进行插入操作
			SmsMT smsMt = new SmsMT();
//			smsMt.setChannel("gw_yd,gw_lt,gw_dx");
			smsMt.setSmsId(pushTypeEnum.getName());
			smsMt.setDestAddr(phone);
			smsMt.setMessage(sendContent);
			smsMt.setOrgId(10001);
			smsMt.setSendUserId(0);

			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			smsMt.setCreateTime(timestamp);

			int isRealTime;// 0：定时发送 1: 实时发送
			if (presendTime == null)
			{
				smsMt.setPresendTime(timestamp);
				isRealTime = 1;
			} else
			{
				smsMt.setPresendTime(Timestamp.valueOf(presendTime));
				isRealTime = 0;
			}
			try
			{

				// 用于保存 能够接受短信的人的消息的userId
				Integer newUserId = null;
				if((!StringUtil.isEmpty(phone)) && (!StringUtil.isEmpty(sendContent))){
					addSmsMt(smsMt, schoolId, sendContent, phone, pushTypeEnum, userId);
				}
				newUserId = userId;
				flag = true;
				
				// 短信分发，此时原先的接收者变成了发送者
				if (newUserId != null)
				{
					UserForwardsmsSwitch userForwardsmsSwitch = userForwardsmsSwitchService.findUserForwardsmsSwitch(newUserId);
					if (userForwardsmsSwitch != null)
					{
						// 短信转发状态:0停用，1启用
						if (userForwardsmsSwitch.getForwardSmsStatus() == 1)
						{
							forward(smsMt, receiver, isRealTime, true);
						}
					}

				}

			} catch (Exception e)
			{
				logger.error("发送短信出错:" + e.getMessage());
				throw e;
			}

		} catch (Exception ex)
		{
			logger.error("发送短信出错:" + ex.getMessage());
			throw ex;
		}
		
		return flag;
	}

	/**
	 * 【仅用于校车】推送 发送短信 smsId 客户写入的短信ID标识(不能为空 ) sendContent 发送内容 (不能为空)
	 * presendTime 为空时是实时发送 presendTime 为String的时间时 是定时发送 格式必须为yyyy-MM-dd
	 * HH:mm:ss type 用于判断发短信时是否推送 type=3时推送 其他的只发短信不推送 update by syj 2015-12-03
	 */
	public void sendSmsByDB(String schoolId, PUSHTYPE pushTypeEnum, String phone, Integer userId, String sendContent, String presendTime, Integer type,
			PushDataByJson p)
	{

		try
		{
			User receiver = commonService.getUserByUserIdForPush(userId);
			if (receiver != null)
			{
				if (type == 3)
				{
					try
					{
						// 推送手机端消息
						List<PushItem> list = new ArrayList<PushItem>();
						PushItem pi = new PushItem();
						pi.title = pushTypeEnum.getName();
						pi.PushContent = sendContent;
						pi.PushType = pushTypeEnum.getType();
						pi.PushContentType = pushTypeEnum.getContentType();
						pi.PushData = GsonHelper.toJson(p);
						String channelId = receiver.getBaiduChannelId();
						Integer deviceType = receiver.getDeviceType();
						if (!StringUtil.isEmpty(channelId) && deviceType != null && deviceType != 0)
						{
							pi.channels = channelId;
							pi.deviceType = String.valueOf(deviceType);
						}
						pi.receiverId = userId;
						pi.schoolId = schoolId;
						list.add(pi);

						pushMsg(list, false);
					} catch (Exception e)
					{
						logger.error("schoolBus推送出错:" + e.getMessage());
					}

				}

				// 对sms_mt进行插入操作
				SmsMT smsMt = new SmsMT();
				smsMt.setSmsId(pushTypeEnum.getName());
				smsMt.setDestAddr(phone);
//				smsMt.setChannel("gw_yd,gw_lt,gw_dx");
				smsMt.setMessage(sendContent);
				smsMt.setOrgId(10001);
				smsMt.setSendUserId(0);

				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				smsMt.setCreateTime(timestamp);

				int isRealTime;// 0：定时发送 1: 实时发送
				if (presendTime == null)
				{
					smsMt.setPresendTime(timestamp);
					isRealTime = 1;
				} else
				{
					smsMt.setPresendTime(Timestamp.valueOf(presendTime));
					isRealTime = 0;
				}
				try
				{
					/*
					 * 1.先判断是否学校有购买过学校条数套餐 1.1 已购买并且剩余条数>0 1.2 已购买但是剩余条数=0
					 * 1.3没有购买
					 */
					// 判断学校是否购买的学校条数套餐剩余条数
					boolean isSchoolBuy = false;
					Integer remainCount = orderMessageService.getRemainCountBySchoolIdForSchoolRange(schoolId);
					if (null == remainCount)
					{
						// 未购买
						isSchoolBuy = false;
					} else
					{
						if (remainCount > 0)
						{
							isSchoolBuy = true;
						} else
						{
							isSchoolBuy = false;
						}
					}

					// 用于保存 能够接受短信的人的消息的userId
					Integer newUserId = null;
					if (isSchoolBuy)
					{
						// 更新学校palm_order_message_school中的reminCount
						orderMessageService.updateOrderMessageSchoolById(schoolId);
						addSmsMt(smsMt, schoolId, sendContent, phone, pushTypeEnum, userId);
						newUserId = userId;
					} else
					{
						// 0:可用 1：失效
						String status = orderMessageService.getVirtualMessageStatus();
						List<OrderMessage> messageList = new ArrayList<OrderMessage>();
						if (!"0".equals(status))
						{

							messageList = orderMessageService.getProduct();
						}
						// 用于保存 能够接受短信的人的消息的userId
						// 说明学校没有短信套餐，直接发送信息，这种属于线下收费
						if (CollectionUtils.isEmpty(messageList))
						{
							addSmsMt(smsMt, schoolId, sendContent, phone, pushTypeEnum, userId);
							newUserId = userId;
						} else
						{
							// 是否收费
							boolean isCharge = true;
							List<UserRole> userRoleList = getUserRoleByUserId(userId);
							if (!CollectionUtils.isEmpty(userRoleList))
							{
								// 学生家长收费
								for (UserRole ur : userRoleList)
								{
									if (!ur.getRoleCode().equals("parent"))
									{
										isCharge = false;
										break;
									}
								}
							}
							if (isCharge)
							{
								Map<String, Object> paramMap = new HashMap<String, Object>();
								paramMap.put("userId", userId);
								paramMap.put("status", 0);
								OrderMessageUser mess = (OrderMessageUser) getBaseDao().selectObjectByObject("OrderMessage.getOrderMessageUserByUserId",
										paramMap);
								if (mess != null)
								{
									addSmsMt(smsMt, schoolId, sendContent, phone, pushTypeEnum, userId);
									// TODO
									int count = ((int) sendContent.length() / 65) + 1;
									int messcount = 0;
									if (mess.getType() == 1)
									{
										// 如果短信剩余条数小于本次短信条说 剩余条数为0 如果大于等于本次短信条说
										// 则本次剩余条数为原剩余条数减本次短信条数

										if (mess.getCount() < count)
										{
											messcount = 0;
										} else
										{
											messcount = mess.getCount() - count;
										}
										paramMap.put("count", messcount);
										paramMap.put("id", mess.getId());
										getBaseDao().updateObject("OrderMessage.updateOrederMessageUserCount", paramMap);

									}

									OrderUserMessageDetail messDetail = new OrderUserMessageDetail();
									messDetail.setCount(count);
									messDetail.setMessageId(mess.getMessageId());
									messDetail.setMessUserId(mess.getId());
									messDetail.setNowCount(messcount);
									messDetail.setOrgCount(mess.getCount());
									messDetail.setType(mess.getType());
									messDetail.setUserId(userId);
									getBaseDao().addObject("OrderMessage.addOrderUserMessageDetail", messDetail);

									newUserId = userId;
								}
							} else
							{
								addSmsMt(smsMt, schoolId, sendContent, phone, pushTypeEnum, userId);
								newUserId = userId;
							}
						}
					}
					// 短信分发，此时原先的接收者变成了发送者
					if (newUserId != null)
					{
						UserForwardsmsSwitch userForwardsmsSwitch = userForwardsmsSwitchService.findUserForwardsmsSwitch(newUserId);
						// 短信转发状态:0停用，1启用
						if (userForwardsmsSwitch.getForwardSmsStatus() == 1)
						{
							forward(smsMt, receiver, isRealTime, true);
						}
					}

				} catch (Exception e)
				{
					throw e;
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public void addSmsMt(SmsMT smsMt, String schoolId, String sendContent, String phone, PUSHTYPE pushTypeEnum, Integer userId)
	{
		// 更新 school_countinfo表中的短信总数
		// 通过schoolId来查询 schoolInfo
		SchoolInfo schoolInfo = schoolService.getSchoolCountInfoById(Long.valueOf(schoolId));
		Integer smsCount = schoolInfo.getSmsCount();
		// int count = (int)Math.ceil(sendContent.length()/65);
		int count = ((int) sendContent.length() / 65) + 1;
		smsCount += count;// 总数加1

		Integer unicomSmsCount = schoolInfo.getUnicomSmsCount();
		Integer mobileSmsCount = schoolInfo.getMobileSmsCount();
		Integer telecomSmsCount = schoolInfo.getTelecomSmsCount();

		List<String> chinaUnicom1 = Arrays.asList("130", "131", "132", "145", "154", "155", "156", "176", "185", "186");
		List<String> chinaUnicom2 = Arrays.asList("1707", "1708", "1709");
		List<String> chinaMobile1 = Arrays.asList("134", "135", "136", "137", "138", "139", "144", "147", "150", "151", "152", "157", "158", "159", "178",
				"182", "183", "184", "187", "188");
		List<String> chinaMobile2 = Arrays.asList("1705");
		List<String> chinaTelecom1 = Arrays.asList("133", "153", "173", "177", "180", "181", "189");
		List<String> chinaTelecom2 = Arrays.asList("1700", "1701", "1702");

		// 运营商类型0:移动 1:电信联通
		String type = "0";
		boolean bolChinaUnicom1 = (chinaUnicom1.contains(phone.substring(0, 3)));
		boolean bolChinaUnicom2 = (chinaUnicom2.contains(phone.substring(0, 4)));
		boolean bolChinaMobile1 = (chinaMobile1.contains(phone.substring(0, 3)));
		boolean bolChinaMobile2 = (chinaMobile2.contains(phone.substring(0, 4)));
		boolean bolChinaTelecom1 = (chinaTelecom1.contains(phone.substring(0, 3)));
		boolean bolChinaTelecom2 = (chinaTelecom2.contains(phone.substring(0, 4)));
		if (bolChinaUnicom1 || bolChinaUnicom2)
		{
			// 联通
			unicomSmsCount += count;
			type = "1";
		} else if (bolChinaMobile1 || bolChinaMobile2)
		{
			// 移动
			mobileSmsCount += count;
			type = "0";
		} else if (bolChinaTelecom1 || bolChinaTelecom2)
		{
			// 电信
			telecomSmsCount += count;
			type = "1";
		}

		// 玛利亚学校和Richx学校改成【深圳壹通道】
		// if("3201150082".equals(schoolId) || "3201140009".equals(schoolId)){
		if ("3201150082".equals(schoolId))
		{
			Map<String, String> resultMap = SmsUtil.getInstance().sendSmsFromYTD(phone, null, sendContent, type);
			String message = resultMap.get("message");// OK 是成功
			String remainpoint = resultMap.get("remainpoint");// 剩余条数
			if (!"ok".equalsIgnoreCase(message))
			{
				// 当【深圳壹通道】返回状态不为ok的时候尝试使用【33得9】
				// 添加短信
				getBaseDao().addObject("SmsMT.addSmsMt", smsMt);
			}
			if ("0".equals(type))
			{
				System.out.println("【深圳壹通道】移动剩余条数：" + remainpoint);
			} else
			{
				System.out.println("【深圳壹通道】联通电信剩余条数：" + remainpoint);
			}
		} else
		{
			// 添加短信
			getBaseDao().addObject("SmsMT.addSmsMt", smsMt);
		}

		// 更新统计条数操作
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("smsCount", smsCount);
		paramMap.put("unicomSmsCount", unicomSmsCount);
		paramMap.put("mobileSmsCount", mobileSmsCount);
		paramMap.put("telecomSmsCount", telecomSmsCount);
		paramMap.put("schoolId", Long.valueOf(schoolId));
		schoolService.updateSchoolCountInfo(paramMap);

		// 保存系统消息
		SysMessage sysMessage = new SysMessage();
		sysMessage.setContent(sendContent);
		sysMessage.setMessageType(pushTypeEnum.getType());
		sysMessage.setType(1);
		sysMessage.setUserId(userId);
		sysMessage.setCreateTime(TimeUtil.getInstance().now());
		sysMessageService.addSysMessage(sysMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getIdsByUserIdAndPermissionCode(Integer userId, String permissionCode, int type)
	{
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		List<Integer> list = null;
		paramMap.put("userId", userId);
		paramMap.put("permissionCode", permissionCode);
		if (type == 0)
		{
			list = getBaseDao().selectListByObject("UserPermission.getOptClazzsByUserIdAndPermissionCode", paramMap);
		} else
		{
			list = getBaseDao().selectListByObject("UserPermission.getOptDepartsByUserIdAndPermissionCode", paramMap);
		}
		return list;
	}

	/**
	 * 通过用户的不同权限获得班级【福建专用】
	 * 
	 * @author chenyong
	 * @date 2016年11月5日 下午1:18:22
	 * @param userId
	 * @param permissionCode
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getIdsByUserIdAndPermissionCodeFj(Integer userId, String permissionCode, int type, String fjSchoolId)
	{
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		List<Integer> list = null;
		paramMap.put("userId", userId);
		paramMap.put("fjSchoolId", fjSchoolId);
		paramMap.put("permissionCode", permissionCode);
		if (type == 0)
		{
			list = getBaseDao().selectListByObject("UserPermission.getOptClazzsByUserIdAndPermissionCodeFj", paramMap);
		} else
		{
			list = getBaseDao().selectListByObject("UserPermission.getOptDepartsByUserIdAndPermissionCodeFj", paramMap);
		}
		return list;
	}

	@Override
	public boolean hasAdminPermission(User sessionUser)
	{
		List<Permission> permissionList = sessionUser.getPermissionList();
		boolean flag = false;
		if (permissionList != null)
		{
			for (Permission p : permissionList)
			{
				if ("admin".equals(p.getPermissionCode()))
				{
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 是否是管理员【福建专用】
	 * 
	 * @author chenyong
	 * @date 2016年11月5日 下午1:15:56
	 * @param sessionUser
	 * @return
	 */
	@Override
	public boolean hasAdminPermissionFj(User sessionUser)
	{
		List<Permission> permissionList = sessionUser.getPermissionList();
		boolean flag = false;
		if (permissionList != null)
		{
			for (Permission p : permissionList)
			{
				if ("admin".equals(p.getPermissionCode()))
				{
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 根据用户Id查找用户详细信息
	 * 
	 * @param userId
	 *            用户Id
	 * @return 用户详细信息
	 * 
	 *         update by syj 20151206
	 */
	public User getUserDetailByUserId(Integer userId)
	{
		return (User) getBaseDao().selectObjectByObject("User.getUserDetailByUserId", userId);
	}

	/**
	 * 根据用户Id查找用户详细信息【福建专用】
	 * 
	 * @param userId
	 *            用户Id
	 * @return 用户详细信息
	 * 
	 *         update by syj 20151206
	 */
	public User getUserDetailByUserIdFj(Integer userId)
	{
		return (User) getBaseDao().selectObjectByObject("User.getUserDetailByUserIdFj", userId);
	}

	/**
	 * 所有班级集合 不同的权限看到不同的班级
	 * 
	 * @param schoolId
	 *            学校Id
	 * @return 该校所有班级集合
	 * 
	 *         20151204 shengyinjiang
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> getClazzList(Map<String, Object> paramMap)
	{
		User user = (User) paramMap.get("user");
		List<Clazz> clazzList = new ArrayList<Clazz>();
		/*
		 * 学校超级管理员权限 董事长 和校长权限 看到内容是所有的班级
		 */
		if (hasAdminPermission(user))
		{
			clazzList = getBaseDao().selectListBySql("Clazz.getAllClazzList");
		} else
		{
			clazzList = getBaseDao().selectListByObject("Clazz.getClazzListByUserIdAndPermissionCode", paramMap);
		}

		return clazzList;
	}

	/**
	 * 所有班级集合 不同的权限看到不同的班级
	 * 
	 * @param schoolId
	 *            学校Id
	 * @return 该校所有班级集合
	 * 
	 *         20160317 shengyinjiang
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> getClazzListInEvalution(Map<String, Object> paramMap)
	{
		List<Clazz> clazzList = getBaseDao().selectListByObject("Clazz.getClazzListByUserIdAndPermissionCode", paramMap);
		return clazzList;
	}

	/**
	 * admin权限获取所有班级 20160409
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> getClazzListInEvalution()
	{
		List<Clazz> clazzList = getBaseDao().selectListBySql("Clazz.getClazzListInEvalution");
		return clazzList;
	}

	/**
	 * 表单校验相关---begin
	 */

	/**
	 * 根据用户Id及学校id 判断用户与该学校是否有关
	 * 
	 * @param userId
	 * @param schoolId
	 * @return
	 * 
	 * 		add by syj 20151216
	 */
	public Integer getUserByUserIdAndSchoolId(Integer userId, Long schoolId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("schoolId", schoolId);

		return (Integer) this.getBaseDao().selectObjectByObject("User.getUserByUserIdAndSchoolId", map);
	}

	/**
	 * 修改用户对应的UserPermission 中的班级Or 部门Id 20151217
	 */
	@SuppressWarnings("unchecked")
	public void updateUserRole(HashMap<String, Object> modelMap)
	{

		String roleCode = (String) modelMap.get("roleCode");

		List<String> rolePermissionList = getBaseDao().selectListByObject("RolePermission.getPermissionCodeListByRoleCode", roleCode);

		modelMap.put("rolePermissionList", rolePermissionList);

		getBaseDao().updateObject("UserPermission.updateOptClazzIdByPermissionAndUserId", modelMap);

	}

	// 添加卡
	public Integer addCard(String cardCode)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Card card = new Card();
		card.setCardCode(cardCode);
		card.setCreatetime(formatter.format(new Date()));
		card.setUpdatetime(formatter.format(new Date()));
		card.setStatus("1");
		return getBaseDao().addObject("Card.addCard", card);
	}

	public void addUserAndCardLink(String cardCode, Integer userId)
	{
		// 校验卡号是否存在，不存在则添加卡,并且添加user_card信息
		Card card = cardService.getCardByCardCode(cardCode);
		if (null == card)//
		{
			Integer cardId = addCard(cardCode);
			card = new Card();
			card.setId(cardId);
			Map<String, Object> paramMap_card = new HashMap<String, Object>();
			paramMap_card.put("userId", userId);
			paramMap_card.put("cardId", cardId);
			getBaseDao().addObject("Card.addUserCard", paramMap_card);
		} else
		{
			// 判断此卡号是否有持有者
			Integer result = (Integer) getBaseDao().selectObjectByObject("Card.checkCardCodeServer", cardCode);
			// 没有持有者
			if (!ResultInfo.RESULT_EXISTS.equals(result))
			{
				Map<String, Object> paramMap_card = new HashMap<String, Object>();
				paramMap_card.put("userId", userId);
				paramMap_card.put("cardId", card.getId());
				getBaseDao().addObject("Card.addUserCard", paramMap_card);
			}
		}
	}

	/**
	 * 添加卡以及用户卡关系（换班导入学生时） 1、若卡不存在正常添加人与卡关系 2、卡有人使用，添加该卡与当前用户的关系
	 * 
	 * @param parentCardcode
	 * @param userId
	 * 
	 * 
	 */
	public void addUserAndCardLinkWhenNewImport(String cardCode, Integer userId)
	{
		// 校验卡号是否存在，不存在则添加卡,并且添加user_card信息
		Card card = cardService.getCardByCardCode(cardCode);
		if (null == card)//
		{
			Integer cardId = addCard(cardCode);
			card = new Card();
			card.setId(cardId);
			Map<String, Object> paramMap_card = new HashMap<String, Object>();
			paramMap_card.put("userId", userId);
			paramMap_card.put("cardId", cardId);
			getBaseDao().addObject("Card.addUserCard", paramMap_card);
		} else
		{
			// 判断此卡号是否有持有者
			Integer result = (Integer) getBaseDao().selectObjectByObject("Card.checkCardCodeServer", cardCode);
			// 没有持有者
			if (!ResultInfo.RESULT_EXISTS.equals(result))
			{
				Map<String, Object> paramMap_card = new HashMap<String, Object>();
				paramMap_card.put("userId", userId);
				paramMap_card.put("cardId", card.getId());
				getBaseDao().addObject("Card.addUserCard", paramMap_card);
			} else
			{
				// 卡有人使用，修改该卡与该学生的关系
				Map<String, Object> paramMap_card = new HashMap<String, Object>();
				paramMap_card.put("userId", userId);
				paramMap_card.put("cardId", card.getId());
				getBaseDao().addObject("Card.updateCardUserId", paramMap_card);
			}
		}
	}

	/**
	 * 手机推送 flag 是否转发短信(该flag是为了解决即发短信又发推送，导致分发的短信重复)
	 */
	public void pushMsg(List<PushItem> piList, boolean flag)
	{
		final String schoolId = DBContextHolder.getDBType();
		//发送推送采用线程发送
		final List<PushItem> piList_final = piList;
		final boolean flag_final = flag;
		poolTaskExecutor.execute(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				DBContextHolder.setDBType(schoolId);
				List<PushItem> piListNew = new ArrayList<PushItem>();
				piListNew.addAll(piList_final);
				if (!CollectionUtils.isEmpty(piList_final))
				{
					Date date = new Date();
					Timestamp timestamp = new Timestamp(date.getTime());
					// 保存系统消息
					SysMessage sysMessage = new SysMessage();
					for (PushItem pi : piList_final)
					{
						Integer receiverId = pi.receiverId;
						try
						{
							if (receiverId != null)
							{
								sysMessage.setContent(pi.PushContent);
								sysMessage.setMessageType(pi.PushType);
								sysMessage.setType(0);
								sysMessage.setUserId(pi.receiverId);
								sysMessage.setCreateTime(TimeUtil.getInstance().now());
								sysMessageService.addSysMessage(sysMessage);

								// 找到智慧校园对应绑定的亦信用户，也推送相应的推送信息
								List<User> yixinUserDetailList = userService.getYIXINUserDetailListbyUserId(receiverId);
								if (yixinUserDetailList != null && yixinUserDetailList.size() > 0)
								{
									for (User yixinUserDetail : yixinUserDetailList)
									{
										try{
											PushItem pi_yixin = pi.clone();
											pi_yixin.receiverId = yixinUserDetail.getUserId();
											pi_yixin.channels = yixinUserDetail.getBaiduChannelId();
											pi_yixin.deviceType = String.valueOf(yixinUserDetail.getDeviceType());
											piListNew.add(pi_yixin);
											/* 调用推送接口 */
											sendPost(pi_yixin);
										}catch(Exception e){
											logger.error("调用推送方法出错,错误信息如下: "+e.getMessage());
											continue;
										}
										
									}
								}

								// 推送转为分发短信
								// 分发之前先判断推送分发开关是否开启，
								UserForwardsmsSwitch userForwardsmsSwitch = userForwardsmsSwitchService.findUserForwardsmsSwitch(receiverId);
								// 推送转发短信状态:0停用，1启用
								if (userForwardsmsSwitch.getForwardPushStatus() == 1)
								{
									User receiver = commonService.getUserByUserIdForPush(receiverId);
									// 对sms_mt进行插入操作
									SmsMT smsMt = new SmsMT();
									smsMt.setSmsId(pi.title);
									smsMt.setDestAddr(receiver.getPhone());
									smsMt.setMessage(pi.PushContent);
									smsMt.setOrgId(10001);
									smsMt.setSendUserId(0);
									smsMt.setCreateTime(timestamp);
									smsMt.setPresendTime(timestamp);
									// isRealTime 0：定时发送 1: 实时发送
									forward(smsMt, receiver, 1, flag_final);
								}
								/* 调用推送接口 */
								sendPost(pi);
							}
						} catch (Exception e)
						{
							logger.error("接收人:" + receiverId + ",pushMsg:" + e);
							continue;
						}
					}
				}
			}
		}));

	}

	/**
	 * 调用推送接口
	 * 
	 * @param piList
	 * @throws PushServerException 
	 * @throws PushClientException 
	 */
	private void sendPost(PushItem pi) throws PushClientException, PushServerException
	{
		//调用推送接口，并保存到pushMessage中
		try{
			String deviceType = pi.deviceType;
			String baiduChannelId = pi.channels;
			if ((!StringUtil.isEmpty(deviceType)) && (!StringUtil.isEmpty(baiduChannelId)))
			{
				// 先保存推送信息，后推送
				if (StringUtil.isEmpty(pi.PushContent))
				{
					pi.PushContent = "智慧校园向您发送了一条新消息，请注意查收";
				}
				Pushmessage pushmessage = new Pushmessage(pi);
				pushmessage.setStatus(0);
				Integer pushId = pushmessageService.addPushmesaage(pushmessage);
				pi.pushId = pushId;
				
				pi.CreateTime=pushmessage.getCreateTime();
				if (deviceType.equals("3"))// android
				{
					pushmessageService.androidPushMsgToSingle(pi);
				} else if (deviceType.equals("4"))// ios
				{
					int badge = pushmessageService.findLatestMessageCountList(pi.receiverId);
					pushmessageService.iosPushNotificationToSingleDevice(pi,badge);
				}
			} else
			{
				// 保存推送信息
				Pushmessage pushmessage = new Pushmessage(pi);
				pushmessage.setStatus(1);
				pushmessageService.addPushmesaage(pushmessage);
			}
		}catch(PushClientException | PushServerException e){
			logger.error(pi.receiverId + ",推送出错："+e.getMessage());
		}
	}

	@Override
	public String registerOpenFire(String uuserid)
	{

		// 注册到容联云，创建容联云账号
		String voipAccount = SDKSubAccount.createSubAccount(uuserid);
		return voipAccount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getSchoolAdminBySchoolId(Long schoolId)
	{
		return (List<User>) getBaseDao().selectListByObject("User.getSchoolAdminBySchoolId", schoolId);
	}

	/**
	 * 添加进出记录（同步数据） 20160105
	 */
	public void addInout(Inout inout)
	{
		getBaseDao().addObject("Inout.addInout", inout);
	}

	/**
	 * 添加进出记录错误记录 没有通过卡号找到对应的用户（同步数据） 20160105
	 */
	public void addInoutError(Inout inout)
	{
		getBaseDao().addObject("Inout.addInoutError", inout);
	}

	/**
	 * 根据学生id 获取对应的家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getParentByStudentId(Integer studentId)
	{
		List<User> list = new ArrayList<User>();
		list = (List<User>)getBaseDao().selectList("User.getParentByStudentId", studentId);
		return list;
	}

	// 根据渠道编码获取用户编码
	@SuppressWarnings("unused")
	private Integer getUserIdByChannelId(String channelId)
	{
		return (Integer) getBaseDao().selectObjectByObject("User.getUserIdByChannel", channelId);
	}

	/**
	 * 所有班级集合 不同的权限看到不同的班级
	 * 
	 * @param schoolId
	 *            学校Id
	 * 
	 * @return 该校所有班级集合
	 * 
	 *         20160108 tao
	 */
	@SuppressWarnings("unchecked")
	public List<Clazz> getClazzListByRoleCode(Map<String, Object> paramMap)
	{
		User user = (User) paramMap.get("user");
		String roleCode = (String) paramMap.get("roleCode");
		if(StringUtil.isEmpty(roleCode) || "null".equals(roleCode)){
			List<Role> roleList = user.getRoleList();
			roleCode = "parent";
			if(!CollectionUtils.isEmpty(roleList))
			{
				if(roleList.size() == 1){
					roleCode = roleList.get(0).getRoleCode();
				}else{
					for(Role role : roleList){
						if((!"parent".equals(role.getRoleCode())) && (!"student".equals(role.getRoleCode()))){
							roleCode = role.getRoleCode();
							break;
						}
					}
				}
			}
		}
		
		List<Clazz> clazzList = new ArrayList<Clazz>();
		/*
		 * 学校超级管理员权限 董事长 和校长权限 看到内容是所有的班级
		 */
		if (hasAdminPermission(user))
		{
			clazzList = getBaseDao().selectListBySql("Clazz.getAllClazzList");
		} else if ("parent".equals(roleCode))
		{
			clazzList = clazzService.getClazzListByParentId(user.getUserId());
		} else if ("student".equals(roleCode)){
			clazzList = clazzService.getClazzListByStudentId(user.getUserId());
		}else
		{
			clazzList = getBaseDao().selectListByObject("Clazz.getClazzListByUserIdAndPermissionCode", paramMap);
		}

		return clazzList;
	}

	/**
	 * 根据学生id获取对应的班级
	 */
	@SuppressWarnings("unchecked")
	public List<StudentClazz> getUserClazzId(Integer userId)
	{
		return (List<StudentClazz>) getBaseDao().selectList("Clazz.getStudentClazzId", userId);
	}

	/**
	 * 根据教师id获取对应的部门 201601011
	 */
	@SuppressWarnings("unchecked")
	public List<UserDepartment> getUserDeparmentId(Integer userId)
	{
		return (List<UserDepartment>) getBaseDao().selectList("UserDepartment.getUserDeparmentId", userId);
	}

	/**
	 * 添加一卡通记录（同步数据） 20160111
	 */
	public void addYkt(Ykt ykt)
	{
		getBaseDao().addObject("Ykt.addYkt", ykt);
	}

	/**
	 * 添加一卡通错误记录 没有通过卡号找到对应的用户（同步数据） 20160117
	 */
	public void addYktError(Ykt ykt)
	{
		getBaseDao().addObject("Ykt.addYktError", ykt);
	}

	/**
	 * 根据卡号查询UserId（同步数据一卡通数据） 20160111
	 * 
	 * @param cardCode
	 */
	public User getUserByCardCode(String cardCode)
	{
		return (User) getBaseDao().selectObjectByObject("User.getUserByCardCode", cardCode);
	}

	/**
	 * 查询一卡通消费地点（同步数据） 20160112
	 */
	@SuppressWarnings("unchecked")
	public List<YktPlace> getYktPlace(String consPlace)
	{
		return (List<YktPlace>) getBaseDao().selectListByObject("Ykt.getYktPlace", consPlace);
	}

	/**
	 * 添加一卡通消费地点（同步数据） 20160112
	 */
	public void addYktPlace(YktPlace yktPlace)
	{
		getBaseDao().addObject("Ykt.addYktPlace", yktPlace);
	}

	/**
	 * 通过IP获得对应的设备Id（同步数据） 20160112
	 */
	public Device getDeviceByIP(String position)
	{
		return (Device) getBaseDao().selectObjectByObject("Device.getDeviceByIP", position);
	}

	/**
	 * 根据用户iD获取推送所需内容
	 */
	public User getUserByUserIdForPush(Integer userId)
	{
		return (User) getBaseDao().selectObject("User.getUserByUserIdForPush", userId);
	}
	/**
	 * 根据用户iD集合获取推送所需内容
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUserByUserIdsForPush(List<Integer> list){
		
		return getBaseDao().selectListByObject("User.getUserByUserIdsForPush", list);	
	}
	/**
	 * 转发短信通用方法
	 * 
	 * @param smsMt
	 * @param receiver
	 * @param isRealTime
	 *            是否实时发送 // 0：定时发送 1: 实时发送
	 * @param flag
	 *            是否转发短信(该flag是为了解决即发短信又发推送，导致分发的短信重复)
	 */
	private void forward(SmsMT smsMt, User receiver, int isRealTime, boolean flag)
	{
		if (flag)
		{
			// 这里还需要判断个人账户余额是否充足 ,并且需要扣费
			UserAccount userAccount = userAccountService.findUserAccountByUserId(receiver.getUserId());
			if (userAccount != null)
			{
				double balance = Double.valueOf(userAccount.getBalance());
				if (balance > 0)
				{
					List<String> phones = userService.getPhonesByUserId(receiver.getUserId());
					if (!CollectionUtils.isEmpty(phones))
					{
						int count = ((int) smsMt.getMessage().length() / 65) + 1;
						// 计算当条短信的价格(65个字作为一条短信，假如短信130个字，就是两条短信的钱)
						double singleSMSFee = PreciseCompute.mul((double) count, Cons.THE_AVERAGE_COST_OF_EACH_SMS);
						double totalFee = PreciseCompute.mul(singleSMSFee, (double) phones.size());
						if (balance >= totalFee)
						{
							// 扣费在转发中进行
							forwardAddSmsMt(smsMt, receiver, phones, isRealTime, singleSMSFee);
						}
					}
				}
			}
		}
	}

	/**
	 * 发送用户设置的转发短信,保存到friend_sms 和 friend_sms_detail中
	 * 
	 * @param smsMt
	 * @param sendContent
	 *            发送短信内容
	 * @param sender
	 *            发送者
	 * @param phones
	 *            手机号
	 * @param isRealTime
	 *            是否实时发送
	 * @param singleSMSFee
	 *            当前短信的价格
	 */
	private synchronized void forwardAddSmsMt(SmsMT smsMt, User sender, List<String> phones, int isRealTime, double singleSMSFee)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sendContent = smsMt.getMessage();
		Integer senderId = sender.getUserId();
		int count = ((int) sendContent.length() / 65) + 1;
		if (!CollectionUtils.isEmpty(phones))
		{
			// 获取当前数据库最后一条数据
			FriendSms lastFriendSms = (FriendSms) getBaseDao().selectObjectByObject("FriendSms.getLastOneFromFriendSms", null);
			// 保存到friend_sms中
			FriendSms sms = new FriendSms();
			sms.setContent(sendContent);
			sms.setCreateTime(formatter.format(smsMt.getCreateTime()));
			sms.setIsRealTime(String.valueOf(isRealTime));
			sms.setPhoneCount(phones.size());
			sms.setReportAmount(0);
			sms.setResult(0);
			sms.setSenderId(senderId);
			sms.setSenderName(sender.getRealName());
			sms.setSentTime(formatter.format(smsMt.getPresendTime()));
			sms.setSmsLength(sendContent.length());
			sms.setSuccAmount(0);
			sms.setType("-3");// -1:好友短信和在线留言 -2:好友在线留言 -3:个人分发的短息

			int smsSum = 0; // 短信发送总数
			int errorSmsSum = 0;
			if (lastFriendSms != null)
			{
				smsSum = lastFriendSms.getSmsSum();
				errorSmsSum = lastFriendSms.getErrorSmsSum();
			}
			int orig_smsSum = smsSum;
			int orig_errorSmsSum = errorSmsSum;
			sms.setSmsSum(smsSum);
			sms.setErrorSmsSum(errorSmsSum);
			int friendSmsId = getBaseDao().addObject("FriendSms.addFriendSms", sms);

			//定义变量，用于上是否添加到涨到中
			boolean isAddBill = false;
			int billCounts = 0; 
			StringBuffer phoneBuffer = new StringBuffer();
			for (String phone : phones)
			{
				// 如果user_phones中的phone等于自己，就不发送继续发送
				if (!phone.equals(smsMt.getDestAddr()) && !StringUtil.isEmpty(phone))
				{
					// 保存到friend_sms_detail中
					FriendSmsDetail smsDetail = new FriendSmsDetail();
					smsDetail.setContent(sendContent);
					smsDetail.setSenderId(senderId);
					smsDetail.setSenderName(sender.getRealName());
					smsDetail.setReceiverId(0);// 短信转发用户，没有userId
					smsDetail.setPhone(phone);
					smsDetail.setReceiverName("短信转发用户" + phone);
					smsDetail.setSeq("");
					smsDetail.setReport("");
					smsDetail.setSmsId(friendSmsId);
					smsDetail.setReportTime(formatter.format(smsMt.getPresendTime()));
					smsDetail.setCreateTime(formatter.format(smsMt.getCreateTime()));
					smsDetail.setSentTime(formatter.format(smsMt.getPresendTime()));
					smsDetail.setType("-3");// 发送短信类型，-1:好友短信和在线留言 -2:好友在线留言
											// -3:个人分发的短息
					// 发送短信
					try
					{
						smsMt.setDestAddr(phone);
						getBaseDao().addObject("SmsMT.addSmsMt", smsMt);

						smsDetail.setStatus(0);// 发送成功
						smsSum += count;

						// 扣费在成功之后进行，更新数据库中platform.user_account和user_account_history表
						UserAccount userAccount = userAccountService.findUserAccountByUserId(senderId);
						String oldBalance = userAccount.getBalance();
						double newBalance = PreciseCompute.sub(Double.valueOf(oldBalance), singleSMSFee);
						userAccount.setBalance(newBalance + "");
						userAccount.setUpdateTime(TimeUtil.getInstance().now());
						userAccountService.updateUserAccount(userAccount);

						String desc = "亦信钱包扣费:" + singleSMSFee + "元,用于短信转发给：" + phone;
						UserAccountHistory userAccountHistory = new UserAccountHistory();
						userAccountHistory.setUserId(senderId);
						userAccountHistory.setNewBalance(newBalance + "");
						userAccountHistory.setOldBalance(oldBalance);
						userAccountHistory.setDescription(desc);
						userAccountHistory.setCreateTime(TimeUtil.getInstance().now());
						userAccountHistory.setType(2);
						userAccountService.addUserAccountHistory(userAccountHistory);
						
						isAddBill = true;
						billCounts++;
						phoneBuffer.append(phone+",");
					} catch (Exception e)
					{
						smsDetail.setStatus(1);// 发送失败
						errorSmsSum += count;
					}
					getBaseDao().addObject("FriendSmsDetail.addFriendSmsDetail", smsDetail);
				}
			}
			
			if(isAddBill){
				
				double totalFee = PreciseCompute.mul(billCounts, singleSMSFee);
				String billDesc = "亦信钱包扣费:" + totalFee + "元,用于短信分发给：" + phoneBuffer;
				Bill myBill = new Bill();
				myBill.setTypeId(Cons.BILLTYPE_OUT_SMS);
				myBill.setTradeNo("");
				myBill.setChannel(3);
				myBill.setUserId(senderId);
				myBill.setCounts(billCounts);
				myBill.setAmount(totalFee);
				myBill.setPlatformCode(0);
				myBill.setOrderId("");
				myBill.setGoodsId(0);
				myBill.setOtherUserId(0);
				myBill.setGoodsName("短信分发");
				myBill.setCaption(billDesc);
				ordersService.addBill(myBill);
			}
			
			if (orig_smsSum != smsSum || orig_errorSmsSum != errorSmsSum)
			{
				HashMap<String, Object> parMap = new HashMap<String, Object>();
				parMap.put("smsSum", smsSum);
				parMap.put("errorSmsSum", errorSmsSum);
				parMap.put("id", friendSmsId);
				getBaseDao().updateObject("FriendSms.updateFriendSms", parMap);
			}
		}
	}

	/**
	 * 根据用户apikey 获取对应的孩子信息 20151130
	 */
	@Override
	@SuppressWarnings("unchecked")
	public HashMap<String, List<String>> getAllChildrenByApiKey(String apiKey)
	{
		List<User> parents = getBaseDao().selectListByObject("User.selectUserByApiKey", apiKey);
		HashMap<String, List<String>> map = null;
		if (parents != null)
		{
			map = new HashMap<String, List<String>>();
			List<User> childrens = getAllChildrenByUserId(parents.get(0).getUserId());
			List<String> cardCodeList = null;
			for (User children : childrens)
			{
				List<Card> cards = getBaseDao().selectListByObject("Card.getCardListByUserId", children.getUserId());
				if (cards != null)
				{
					cardCodeList = new ArrayList<String>();
					for (Card card : cards)
					{
						cardCodeList.add(card.getCardCode());
					}
					map.put(children.getRealName(), cardCodeList);
				}
			}
		}

		return map;
	}

	/**
	 * 根据clazzId加载班级学生信息 如果是家长这查找本班级下自己的小孩 其他则查看本班级的所有学生
	 */
	@SuppressWarnings("unchecked")
	public List<User> loadStudentByClazzId(String roleCode, Map<String, Object> paramMap, User user)
	{
		if (roleCode.equals("parent"))
		{
			return (List<User>) getBaseDao().selectListByObject("User.loadStudentByClazzIdForParent", paramMap);
		} else
		{
			paramMap.put("userId", user.getUserId());
			return (List<User>) getBaseDao().selectListByObject("User.loadStudentByClazzIdForTeacher", paramMap);
		}
	}

	/**
	 * 查询学校是否有试用短信 且是否超过试用条数
	 * 
	 * @param schoolId
	 * @return
	 */
	public SchoolTrySms getSchoolTrySms(String schoolId)
	{
		return (SchoolTrySms) getBaseDao().selectObjectByObject("Sms.getSchoolTrySms", schoolId);
	}

	/**
	 * 根据userId查询班主任对应的班级权限 按照optClazzId 分组
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserPermission> getUserPermissionGroupByOptClazzId(Integer userId, String roleCode)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", userId);
		paramMap.put("roleCode", roleCode);
		return (List<UserPermission>) getBaseDao().selectListByObject("UserPermission.getUserPermissionGroupByOptClazzId", paramMap);
	}

	/**
	 * 根据userId 和 optClazzId 删除对应的user_permission
	 * 
	 * @param paramMap
	 */
	public void deleteUserRolePermissionByUserIdAndClazzId(Map<String, Object> paramMap)
	{
		getBaseDao().deleteObject("UserPermission.deleteUserRolePermissionByUserIdAndClazzId", paramMap);
	}

	/**
	 * 根据Id
	 * 
	 * @param paramMap
	 */
	public void updateUserPermissionById(Map<String, Object> paramMap)
	{
		getBaseDao().updateObject("UserPermission.updateUserPermissionById", paramMap);
	}

	/**
	 * 根据userId 和 optClazzId 更新对应的user_permission
	 */
	public void updateUserPermissionByUserId(Map<String, Object> paramMap)
	{
		getBaseDao().updateObject("UserPermission.updateUserPermissionByUserId", paramMap);
	}

	/**
	 * 福建云平台请求获取访问TOKEN 根据appid和appkey获取token
	 * 
	 * @param appid
	 * @param appkey
	 * @return
	 */
	public String getAccesstoken(String appid, String appkey)
	{
		String tokenstr = null;
		JSONObject dataJson = null;
		try
		{
			JSONObject reqObj = new JSONObject();
			long timestamp = System.currentTimeMillis();
			reqObj.put("appid", appid);
			reqObj.put("timestamp", timestamp);

			String paramValues = appid + appkey + timestamp;
			byte[] hmacSHA = EncryptionUtils.getHmacSHA1(paramValues, appkey);
			String digest = EncryptionUtils.bytesToHexString(hmacSHA);
			digest = digest.toUpperCase();

			reqObj.put("keyinfo", digest);

			// System.out.println("查询参数1为[" + reqObj.toString() + "]");
			String resp = HttpUtil.doPost("http://www.fjedu.cn:20001/apigateway/getaccesstoken", reqObj.toString());
			// System.out.println("获取token返回结果为[" + resp + "]");
			if (resp != null && !"".equals(resp))
			{
				JSONObject obj = (JSONObject) JSONObject.fromObject(resp);
				String resultCode = (String) obj.get("result");
				// System.out.println("查询token响应码为[" + resultCode + "]");
				if ("000000".equalsIgnoreCase(resultCode))
				{
					// logger.info("查询token响应码为["+resultCode+",成功]");
					dataJson = (JSONObject) obj.get("tokenInfo");
					String token = (String) dataJson.get("token");
					String validtime = (String) dataJson.get("validtime");

					tokenstr = token + "__" + validtime;

				}
			}
		} catch (Exception e)
		{
			logger.error("福建资源云平台,查询token失败:" + e);
		}

		String token = null;
		@SuppressWarnings("unused")
		Long validtime = null;
		if (tokenstr != null && !"".equals(tokenstr))
		{ // resp为 token 加 __
			// 加validtime
			String[] res = tokenstr.split("__");
			token = res[0];
			validtime = Long.parseLong(res[1]);
		}
		return token;
	}

	/**
	 * 福建资源云平台登录
	 * 
	 * @param token
	 * @param account
	 *            账号
	 * @param password
	 *            密码是MD5加密之后
	 * @param portaltype
	 *            登陆来源
	 * @return
	 */
	public String doFJlogin(String token, String account, String password, String portaltype)
	{
		try
		{
			EncryptUtils eu = EncryptUtils.getInstance();
			// 福建资源云平台的密码需要双重加密
			String encodepassword1 = eu.encode(password.toUpperCase(), Constant.USER_SERCRETKEY);

			JSONObject reqObj = new JSONObject();
			reqObj.put("account", account);
			reqObj.put("password", encodepassword1);
			reqObj.put("portaltype", portaltype);

			String resp = HttpUtil.doPost("http://www.fjedu.cn:20001/aam/rest/account/login?token=" + token, reqObj.toString());
			if (resp != null && !"".equals(resp))
			{
				JSONObject obj = (JSONObject) JSONObject.fromObject(resp);
				String resultCode = (String) obj.get("result");
				if ("000000".equalsIgnoreCase(resultCode))
				{
					return resp;
				}
			}
		} catch (Exception e)
		{
			logger.error("福建资源云平台,调用登录接口失败:" + e);
		}
		return "";
	}

	/**
	 * 根据登陆家长的userI的查找对应的小孩的所有家长
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllParentByParentId(Integer userId, Long schoolId)
	{
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("userId", userId);
		parmMap.put("schoolId", schoolId);
		return (List<User>) getBaseDao().selectListByObject("User.getAllParentByParentId", parmMap);
	}

	/**
	 * 根据用户Id 判断用户是否已经拥有多少角色
	 * 
	 * @param userId
	 * @param roleCode
	 * @return
	 * 
	 * 		add by syj 20151216
	 */
	@SuppressWarnings("unchecked")
	public List<UserRole> getUserRoleByUserId(Integer userId)
	{
		return (List<UserRole>) this.getBaseDao().selectListByObject("UserRole.getUserRoleByUserId", userId);

	}
	/**
	 * 根据班级获得学生和家长信息
	 * @author chenyong
	 * @Time 2017年3月14日 下午3:56:18
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getStudentAndParentByClazzId(Map<String,Object>map) {
		
		return (List<User>) getBaseDao().selectListByObject("User.getStudentAndParentByClazzId", map);
	}



}