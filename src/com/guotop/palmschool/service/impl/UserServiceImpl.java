package com.guotop.palmschool.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.constant.Cons;
import com.guotop.palmschool.entity.Clazz;
import com.guotop.palmschool.entity.Department;
import com.guotop.palmschool.entity.OrderMessage;
import com.guotop.palmschool.entity.OrderMessageClazz;
import com.guotop.palmschool.entity.OrderMessagePermission;
import com.guotop.palmschool.entity.OrderMessagePermissionDetail;
import com.guotop.palmschool.entity.OrderMessageUser;
import com.guotop.palmschool.entity.Permission;
import com.guotop.palmschool.entity.Role;
import com.guotop.palmschool.entity.RolePermission;
import com.guotop.palmschool.entity.School;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.entity.UserCard;
import com.guotop.palmschool.entity.UserPermission;
import com.guotop.palmschool.entity.UserRole;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.rest.entity.UserAuthorization;
import com.guotop.palmschool.rest.entity.UserInfoDetail;
import com.guotop.palmschool.service.BaseService;
import com.guotop.palmschool.service.ClazzService;
import com.guotop.palmschool.service.DepartmentService;
import com.guotop.palmschool.service.GradeService;
import com.guotop.palmschool.service.OrderMessagePermissionService;
import com.guotop.palmschool.service.OrderMessageService;
import com.guotop.palmschool.service.PermissionService;
import com.guotop.palmschool.service.RoleService;
import com.guotop.palmschool.service.SmsService;
import com.guotop.palmschool.service.StudentService;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.util.StringUtil;
import com.guotop.palmschool.util.TimeUtil;

/**
 * 用户操作类实现类
 * 
 * @author zhou
 *
 */
@Service("userService")
public class UserServiceImpl extends BaseService implements UserService
{
	@Resource
	private PermissionService permissionService;
	@Resource
	private RoleService roleService;
	@Resource
	private DepartmentService departmentService;
	@Resource
	private CommonService commonService;
	@Resource
	private GradeService gradeService;
	@Resource
	private ClazzService clazzService;
	@Resource
	private StudentService studentService;
	@Resource
	private OrderMessageService orderMessageService;
	@Resource
	private OrderMessagePermissionService orderMessagePermissionService;
	@Resource
	private SmsService smsService;

	/**
	 * 登陆获取用户,多用户对应单手机号
	 * 
	 * @param userName
	 *            用户名(即手机号)
	 * @param password
	 *            密码
	 * @return 登陆成功后的用户列表，可能对应单用户多权限
	 */
	@SuppressWarnings("unchecked")
	public List<User> doLogin(String userName, String password,String userId)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("userName", userName);
		paramMap.put("md5password", StringUtil.toMD5(password));
		paramMap.put("userId", userId);
        if(!StringUtil.isEmpty(userId)){
        	//E学习登陆资源云，只传userId登陆专用
        	return getBaseDao().selectListByObject("User.doLoginUserId", paramMap);
        }else{
        	return getBaseDao().selectListByObject("User.doLogin", paramMap);
        }
		
	}

	/**
	 * 添加userbase For 福建资源云
	 * @param user
	 * @return
	 */
	public Integer addUserBaseForFJ(User user){
		Integer userId = getBaseDao().addObject("User.addUserBaseForFJ", user);
		return userId;
	}
	/**
	 * 添加userDetail For 福建资源云
	 * @param user
	 * @return
	 */
	public void addUserDetailForFJ(User user){
		getBaseDao().addObject("User.addUserDetailForFJ", user);
	}
	
	
	/**
	 * 根据uuserId查询用户
	 * @param uuserId
	 * @return
	 */
	public User getUserByUUserId(String uuserId)
	{
		return (User) getBaseDao().selectObjectByObject("User.getUserByUUserId", uuserId);
	}
	
	/**
	 * 根据用户code获取用户card信息
	 * 
	 * @param code
	 * @return UserCard 用户卡信息
	 */
	public UserCard selectUserCardByHomeId(String code)
	{
		return (UserCard) getBaseDao().selectObjectByObject("User.selectUserCardByHomeId", code);
	}

	/**
	 * 检查密码是否存在
	 * 
	 * @param oldPassword
	 *            待查询密码
	 * @param userList
	 *            存在session中的用户集合
	 * @return true:存在 false:不存在
	 */
	public boolean checkPasswordExist(String oldPassword, Integer userId)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("password", StringUtil.toMD5(oldPassword));
		paramMap.put("userId", userId);

		Integer count = (Integer) getBaseDao().selectObjectByObject("User.checkPasswordExist", paramMap);
		if (Cons.RESULT_QUERYEXIST.equals(count))
		{
			return true;
		} else
		{
			return false;
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @param userList
	 *            return
	 */
	public Integer modifyPassword(String oldPassword, String newPassword, Integer userId)
	{
		Integer count = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("md5OldPassword", StringUtil.toMD5(oldPassword));
		paramMap.put("md5newPassword", StringUtil.toMD5(newPassword));
		paramMap.put("userId", userId);

		try
		{
			getBaseDao().updateObject("User.modifyPassword", paramMap);
			count = 1;
		} catch (Exception e)
		{
			count = -1;
		}
		return count;
	}
	/**
	 * 修改密码【福建专用】
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @param userList
	 *            return
	 */
	public Integer modifyPasswordFj(String oldPassword, String newPassword, Integer userId)
	{
		Integer count = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("md5OldPassword", StringUtil.toMD5(oldPassword));
		paramMap.put("md5newPassword", StringUtil.toMD5(newPassword));
		paramMap.put("userId", userId);
		
		try
		{
			getBaseDao().updateObject("User.modifyPasswordFj", paramMap);
			count = 1;
		} catch (Exception e)
		{
			count = -1;
		}
		return count;
	}

	/**
	 * @param userName
	 *            用户名
	 * @return List<User> 查找到的用户列表
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUserListByBindPhone(String bindPhone)
	{
		return getBaseDao().selectListByObject("User.getUserListByBindPhone", bindPhone);
	}

	/**
	 * 根据userId查找到电子卡Id
	 * 
	 * @param code
	 *            电子卡code
	 * @return
	 */
	public UserCard selectCardIdByUserId(Integer userid)
	{
		return (UserCard) getBaseDao().selectObjectByObject("User.selectCardIdByUserId", userid);
	}

	/**
	 * 获取所有互信ID为空的用户
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllUsersWithHxIdIsNull()
	{
		return getBaseDao().selectListBySql("User.selectUserWithHxIdIsNull");
	}

	/**
	 * 根据id更新对应互信ID
	 */
	public void modifyHxIdById(String huxinId, Integer id)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("huxinId", huxinId);
		paramMap.put("id", id);
		getBaseDao().addObject("User.modifyUserHxId", paramMap);
	}

	/**
	 * 根据id获取互信id
	 */
	public String getHxIdById(Integer userId)
	{
		return (String) getBaseDao().selectObject("User.selectHxIdById", userId);
	}

	/**
	 * 根据互信ID查找该互信ID能看到的所有教师(app接口)
	 * 
	 * @param huxinId
	 *            互信ID
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectTeacherListByRole(Map<String, Object> paramMap, User user)
	{
		List<User> list = new ArrayList<User>();

		/**
		 * 新增权限划分
		 */
		Integer roleId = 0;

		/**
		 * 学校超级管理员权限 和校长权限 看到内容是一样
		 */
		if (Cons.ROLE_ADMIN.equals(roleId) || Cons.ROLE_SCHOOLADMIN.equals(roleId) || Cons.ROLE_HEADMASTER.equals(roleId))
		{
			list = getBaseDao().selectListByObject("User.selectTeacherListAsSchool", paramMap);
		} else
		{
			paramMap.put("departmentId", user.getDepartmentId());
			paramMap.put("userId", user.getId());
			list = getBaseDao().selectListByObject("User.selectTeacherListAsDepartmentLeader", paramMap);
		}

		/**
		 * 部门主任
		 * 
		 */
		if (Cons.ROLE_GRADELEADER.equals(roleId))
		{
			paramMap.put("departmentId", user.getDepartmentId());
			paramMap.put("userId", user.getId());
			list = getBaseDao().selectListByObject("User.selectTeacherListAsDepartmentLeader", paramMap);

		}
		return list;
	}

	/**
	 * 生成学校管理员，同时写入学校关系表中
	 */
	public void addUser(User user)
	{
		Integer userId = addUserBase(user);
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		User tempUser = new User();
		tempUser.setUserId(userId);
		tempUser.setCreateTime(user.getCreateTime());
		tempUser.setRealName("学校管理员");
		tempUser.setBirthday(null);
		tempUser.setAge(0);
		tempUser.setCity(null);
		tempUser.setProvince(null);
		tempUser.setSex(null);
		tempUser.setQQ(null);
		tempUser.setCreateUserId(1);
		tempUser.setVoipAccount(user.getVoipAccount());
		tempUser.setCertificateId(null);
		addUserDetail(tempUser);// userdetail表
		paramMap.clear();
		paramMap.put("userId", userId);
		paramMap.put("schoolId", user.getSchoolId());
		paramMap.put("type", "0");
		getBaseDao().addObject("User.addUserSchool", paramMap);
	}

	/**
	 * 根据用户id获取班级ID jfy 2015-11-30
	 */
	public String getClazzIdByUserId(Integer userId)
	{
		return (String) getBaseDao().selectObject("User.getClazzIdByUserId", userId);
	}
	
	/**
	 * 根据学生ID获取年级ID
	 * @param userId
	 * @return
	 */
	public  Integer getGradeIdByStudentId(Integer userId)
	{
		return (Integer) getBaseDao().selectObject("User.getGradeIdByStudentId", userId);
	}

	/**
	 * 根据apiKey查找用户
	 * 
	 * @param apiKey
	 * @return User apiKey对于的用户 update syj 20151210
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUserByApiKey(String apiKey)
	{
		return getBaseDao().selectListByObject("User.selectUserByApiKey", apiKey);
	}

	public User getUserByApiKeyAndSchoolId(String apiKey, String schoolId)
	{
		// 这边是利用apikey 进行模拟登录
		User user = null;
		List<User> userList = getUserByApiKey1(apiKey);
		if (!CollectionUtils.isEmpty(userList))
		{
			for (User tmp : userList)
			{
				if (!StringUtil.isEmpty(schoolId) && schoolId.equals(tmp.getSchoolId()))
				{
					user = setRoleAndPermissionToUser(tmp);
					break;
				}
			}
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	private List<User> getUserByApiKey1(String apiKey)
	{
		List<User> userList = getBaseDao().selectListByObject("User.selectUserByApiKey", apiKey);
		if (!CollectionUtils.isEmpty(userList))
		{
			// 只有当集合长度为1，并且schoolId为空的时候，说明该用户没有绑定学校，需要找绑定关系
			if (userList.size() == 1 && StringUtil.isEmpty(userList.get(0).getSchoolId()))
			{
				Integer relate_userId = getPalmUserIdbyUserId(userList.get(0).getUserId());
				if(relate_userId != null){
					return getUserByUserId(relate_userId);
				}
				return null;
			}
			return userList;
		}
		return null;
	}
	
	public Integer getPalmUserIdbyUserId(Integer userId)
	{
		return (Integer) getBaseDao().selectObject("User.getPalmUserbyUserId", userId);
	}
	
	public void delBindPalmUserbyUserId(Integer userId)
	{
		getBaseDao().deleteObject("User.delBindPalmUserbyUserId", userId);
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getYIXINUserDetailListbyUserId(Integer userId)
	{
		return getBaseDao().selectListByObject("User.getYIXINUserDetailListbyUserId", userId);
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUserByUserId(Integer userId)
	{
		return getBaseDao().selectListByObject("User.selectUserByUserId", userId);
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUserByUserIdForFJ(Integer userId){
		return getBaseDao().selectListByObject("User.getUserByUserIdForFJ", userId);
	}
	/**
	 * 根据用户Id获得Userbase数据
	 * @author chenyong
	 * @Time 2017年4月19日 下午2:54:16
	 */
	@Override
	public User getUserbaseByUserId(Integer userId){
		return (User) getBaseDao().selectObject("User.getUserbaseByUserId", userId);
	}
	/**
	 * 将roleCode permissionCode 设置到user中
	 * 
	 * @param userList
	 */
	public User setRoleAndPermissionToUser(User user)
	{
		User tempUser = user;
		Integer userId = tempUser.getUserId();
		if ("sysadmin".equals(tempUser.getUsername()))// 超级管理员
		{
			List<Role> roleList = new ArrayList<Role>();
			List<Permission> permissionList = new ArrayList<Permission>();
			Role role = new Role();
			role.setRoleCode("admin");
			role.setRoleName("管理员");
			roleList.add(role);
			tempUser.setRoleList(roleList);

			Permission permission = new Permission();
			permission.setPermissionCode("admin");
			permission.setPermissionName("管理员");
			permissionList.add(permission);
			tempUser.setPermissionList(permissionList);

		} else
		{
			DBContextHolder.setDBType(tempUser.getSchoolId());
			List<Role> roleList = roleService.getRoleList(userId);
			boolean isStudent = false;
			boolean isParent = false;
			boolean isHasOtherRole = false;
			for (Role role : roleList)
			{
				if ("student".equals(role.getRoleCode()))
				{
					isStudent = true;
				} else if ("parent".equals(role.getRoleCode()))
				{
					isParent = true;
				}else{
					isHasOtherRole = true;
				}
			}

			if ("0".equals(tempUser.getSchoolType()))// 学校管理员
			{
				List<Permission> permissionList = permissionService.getPermissionList(userId);
				if (CollectionUtils.isEmpty(permissionList))
				{
					UserPermission up = new UserPermission();
					up.setUserId(userId);
					up.setPermissionCode("admin");
					HashMap<String, Object> parmMap = new HashMap<String, Object>();
					parmMap.put("userId", userId);
					parmMap.put("permissionCode", "admin");
					parmMap.put("roleCode", "admin");
					commonService.addUserPermission(parmMap);
					permissionList = permissionService.getPermissionList(userId);
				}
				if (CollectionUtils.isEmpty(roleList))
				{
					UserRole ur = new UserRole();
					ur.setUserId(userId);
					ur.setRoleCode("admin");
					roleService.addUserRole(ur);
					roleList = roleService.getRoleList(userId);
				}
				tempUser.setRoleList(roleList);
				tempUser.setPermissionList(permissionList);
			} else
			{
				List<Permission> permissionList = permissionService.getPermissionList(userId);
				String clazzId = getClazzIdByUserId(userId);
				List<Department> departList = departmentService.getDepartListByUserId(userId);
				tempUser.setRoleList(roleList);
				tempUser.setPermissionList(permissionList);
				tempUser.setDepartmentList(departList);
				if (!StringUtil.isEmpty(clazzId))
				{
					tempUser.setClazzId(Integer.valueOf(clazzId));
				}
			}
			
			if (isParent)
			{
				List<Clazz> clazzList = clazzService.getClazzListByParentId(userId);
				if (!CollectionUtils.isEmpty(clazzList))
				{
					tempUser.setClazzList(clazzList);
				}
				List<User> studentList = commonService.getAllChildrenByUserId(userId);
				tempUser.setStudentList(studentList);
			}
		}

		return tempUser;
	}

	/**
	 * 根据apiKey查找该apiKey能看到的所有学生(app接口)
	 * 
	 * update syj 20151211
	 */
	@SuppressWarnings("unchecked")
	public List<User> selectStudentListByRole(Map<String, Object> paramMap, User user)
	{
		List<User> list = new ArrayList<User>();
		String permissionCode = (String) paramMap.get("permissionCode");
		if (commonService.hasAdminPermission(user))
		{
			list = this.getBaseDao().selectListByObject("User.getStudentListInSms", paramMap);
		} else
		{
			List<Integer> clazzIds = commonService.getIdsByUserIdAndPermissionCode(user.getUserId(), permissionCode, 0);
			if(clazzIds !=null && clazzIds.size() > 0){
				paramMap.put("clazzIds", clazzIds);
				list = this.getBaseDao().selectListByObject("User.getStudentListByClazzIdsInSms", paramMap);		
			}
		}

		return list;
	}

	@Override
	public boolean isManagerBySchoolIdAndUserId(long schoolId, Integer userId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		boolean flag = false;
		map.put("userId", userId);
		map.put("schoolId", schoolId);
		String type = (String) getBaseDao().selectObjectByObject("User.isManagerBySchoolIdAndUserId", map);
		if ("0".equals(type))
		{
			flag = true;
		}
		return flag;
	}
	
	@Override
	public boolean isManagerBySchoolIdAndUserIdForFJ(long schoolId, Integer userId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		boolean flag = false;
		map.put("userId", userId);
		map.put("schoolId", schoolId);
		String type = (String) getBaseDao().selectObjectByObject("User.isManagerBySchoolIdAndUserIdForFJ", map);
		if ("0".equals(type))
		{
			flag = true;
		}
		return flag;
	}
	
	

	@Override
	public String getUserNameByUserId(Integer userId)
	{
		return (String) getBaseDao().selectObjectByObject("User.getUserNameByUserId", userId);
	}

	@SuppressWarnings("unchecked")
	public List<User> getUserIdListBySchoolId(Long schoolId)
	{
		return getBaseDao().selectListByObject("User.getUserIdBySchoolId", schoolId);
	}

	@Override
	public String getUuserIdByUserId(int userId)
	{
		return (String) getBaseDao().selectObjectByObject("User.getUuserIdByUserId", userId);
	}
	
	public User getClazzLeaderByClazzIdInRICHBOOK(Integer clazzId){
		return (User) getBaseDao().selectObject("User.getClazzLeaderByClazzIdInRICHBOOK",clazzId);
	}
	
	@Override
	public String getVoipAccountByUserId(Integer userId)
	{
		return (String) getBaseDao().selectObjectByObject("User.getVoipAccountByUserId", userId);
	}

	@Override
	public User getUserInfoByCardCode(String cardCode)
	{
		return (User)getBaseDao().selectObjectByObject("User.getUserInfoByCardCode", cardCode);
	}

	@Override
	public void addDownLoadNum()
	{
		getBaseDao().updateObject("User.addDownLoadNum", null);
		
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getPhonesByUserId(Integer userId)
	{
		return (List<String>)getBaseDao().selectListByObject("User.getPhonesByUserId", userId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUserNameAndCardCodeList()
	{
		return getBaseDao().selectList("User.getUserNameAndCardCode",null);
	}
	
	public Integer getIdByStudentIdAndParentId(Integer cardId,Integer loginId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("cardId", cardId);
		map.put("loginId", loginId);
		return (Integer)getBaseDao().selectObjectByObject("User.getIdByStudentIdAndParentId",map);
	}
	
	/**
	 * 获取学校所有家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllParentList()
	{
		return getBaseDao().selectListBySql("User.getAllParentList");
	}
	
	/**
	 * 获取学校所有教职工
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllTeacher()
	{
		return getBaseDao().selectListBySql("User.getAllTeacher");
	}
	
	/**
	 * 获取学校人员除了学生和家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllUserExceptStudentAndPArent()
	{
		return getBaseDao().selectListBySql("User.getAllUserExceptStudentAndPArent");
	}
	
	/**
	 * 根据班级Id获取对应的所有家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllParentListByClazz(Integer clazzId)
	{
		return getBaseDao().selectList("User.getAllParentListByClazz",clazzId);
	}
	/**
	 * 根据班级Id获取对应的所有家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllParentListByClazzs(String [] clazzIds)
	{
		return getBaseDao().selectListByObject("User.getAllParentListByClazzs",clazzIds);
	}

	/**
	 * 根据年级Id获取对应的所有家长
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllParentListByGradeId(Integer gradeId)
	{
		return getBaseDao().selectList("User.getAllParentListByGradeId",gradeId);
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getTypeByUserId(String userId) {
		return (List<User>)getBaseDao().selectListByObject("User.getTypeByUserId",userId);
	}
	
	public boolean isExistUserByPhoneAndUserId(HashMap<String,Object> param)
	{
		boolean flag = false;
		Integer count = (Integer)getBaseDao().selectObjectByObject("User.getCountByPhoneAndUserId", param);
		if (count > 0)
		{
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 查询系统管理员的信息
	 * @return
	 */
	public User getSysAdmin()
	{
		return (User) getBaseDao().selectObject("User.getSysAdmin", null);
	}
	
	/**
	 * 更新userbase 中的palmUserName为手机号 和source为1智慧校园用户
	 */
	public void updateUserBase(HashMap<String, Object> paramMap)
	{
		getBaseDao().updateObject("User.updateUserBase", paramMap);
	}
	
	public void updateUserDetail(HashMap<String, Object> paramMap)
	{
		getBaseDao().updateObject("User.updateUserDetail", paramMap);
	}
	
	/**
	 * 根据apikey查询亦信用户信息
	 * @param apiKey
	 * @return
	 */
	public User getHuxinUserByApiKey(String apiKey)
	{
		return (User)getBaseDao().selectObjectByObject("User.getHuxinUserByApiKey", apiKey);
	}
	
	/**
	 * 根据phone获取user
	 * @param phone
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getCASUserByPhone(String phone){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("phone", phone);
		return getBaseDao().selectListByObject("User.getCASUserByPhone", paramMap);
	}
	
	/**
	 * 根据name获取user
	 * @param phone
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getCASUserByName(String name){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("name", name);
		return getBaseDao().selectListByObject("User.getCASUserByName", paramMap);
	}
	
	
	/**
	 * 根据personid获取福建资源云用户信息
	 * @param phone
	 * @return
	 */
	public UserInfoDetail getFJUserDetailByPersonid(String personid){
		return (UserInfoDetail) getBaseDao().selectObjectByObject("User.getFJUserDetailByPersonid", personid);
	}
	
	public User getUserDetailByUserIdInPersonCenter(Integer userId){
		return (User) getBaseDao().selectObjectByObject("User.getUserDetailByUserIdInPersonCenter", userId);
	}
	
	@Override
	public UserAuthorization getUserAuthorizationByUserId(String userId)
	{
		UserAuthorization ua = (UserAuthorization) getBaseDao().selectObjectByObject("User.getUserAuthorizationByUserId", userId);
		return ua;
	}
	
	@Override
	public UserAuthorization getUserAuthorizationByPersonidForFJ(String personid)
	{
		UserAuthorization ua = (UserAuthorization) getBaseDao().selectObjectByObject("User.getUserAuthorizationByPersonidForFJ", personid);
		return ua;
	}
	
	@Override
	public void addUserAuthForFJ(UserAuthorization ua)
	{
		UserAuthorization  student_db = getUserAuthorizationByPersonidForFJ(ua.getPersonid());
		if(null == student_db){
			getBaseDao().addObject("User.addUserAuthorizationForFJ", ua);
		}
	}

	@Override
	public void updateUserAuthForFJ(UserAuthorization ua)
	{
		getBaseDao().updateObject("User.updateUserAuthorizationForFJ", ua);
	}
	
	/**
	 * 获取所有未购买服务费或卡押金没支付的学生 
	 * @param schoolId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getNotPurchaseStudentList(String schoolId)
	{
		Map<String,Object> parmMap = new HashMap<String, Object>();
		parmMap.put("schoolId", schoolId);
		List<User> not=new ArrayList<>();
		List<User> all=getBaseDao().selectListByObject("User.getPurchaseStudentList", parmMap);//所有学生
		List<User> service=getBaseDao().selectListByObject("User.getServiceAllStudent", parmMap);//已购买服务费
		List<User> card=getBaseDao().selectListByObject("User.getCardAllStudent", parmMap);//已购买卡押金
		List<OrderMessage> orderMessage=getBaseDao().selectListByObject("OrderMessage.getAllOrderMessageClazz",null);//所有班级的套餐
		Map<Integer,OrderMessage> orderMessageMap=new HashMap<>();
		for (OrderMessage m : orderMessage) {
			orderMessageMap.put(m.getClazzId(),m);
		}
		Map<Integer,Integer> serviceMap=new HashMap<>();
		for (User user : service) {
			serviceMap.put(user.getUserId(), user.getUserId());
		}
		Map<Integer,Integer> cardMap=new HashMap<>();
		for (User user : card) {
			cardMap.put(user.getUserId(), user.getUserId());
		}
		boolean flag=false;
		OrderMessage m=null;
		for (User user : all) {
			m=orderMessageMap.get(user.getClazzId());
			flag=false;
			if(m==null){
				continue;
			}
			user.setServicePrice(m.getServicePrice());
			user.setCardPrice(m.getCardPrice());
			user.setOrderMessageName(m.getName());
			user.setOrderMessage(m.getId());
			if(!serviceMap.containsKey(user.getUserId()) && m.getServicePrice()>0){//服务费未买
				flag=true;
				user.setType(0);
			}else{
				user.setType(1);	
			}
			if(!cardMap.containsKey(user.getUserId()) && m.getCardPrice()>0){//卡押金未支付
				 flag=true;	
				 user.setCardPay(0);
			}else{
				user.setCardPay(1);
			}
			if(flag){
				not.add(user);
			}
		}
		return not;
	}
	
	/**
	 * 根据部门IdList获取对应的教师
	 * @param departmentIdList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getTeacherListByDepartmentIdList(List<Integer> departmentIdList)
	{
		return (List<User>) getBaseDao().selectListByObject("User.getTeacherListByDepartmentIdList", departmentIdList);
	}
	
	
	//---------------------------------以下智慧校园3.0---------------------------------------------------
	
	@Override
	public boolean checkCodeIsExistInAddNewUser(String code, Long schoolId)
	{
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("code", code);
		paramMap.put("schoolId", schoolId);
		Integer result = (Integer) getBaseDao().selectObjectByObject("User.checkCodeIsExistInAddNewUser", paramMap);
		boolean flag = false;
		if (result > 0)
		{
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean checkCodeIsExistInModifyUser(String origCode, String code, Long schoolId)
	{
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("origCode", origCode);
		paramMap.put("code", code);
		paramMap.put("schoolId", schoolId);
		Integer result = (Integer) getBaseDao().selectObjectByObject("User.checkCodeIsExistInModifyUser", paramMap);
		boolean flag = false;
		if (result > 0)
		{
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean checkBindPhoneIsExist(String bindPhone)
	{
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("bindPhone", bindPhone);
		Integer result = (Integer) getBaseDao().selectObjectByObject("User.checkBindPhoneIsExist", paramMap);
		boolean flag = false;
		if (result > 0)
		{
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean checkBindPhoneIsExistInModifyUser(String origBindPhone, String bindPhone)
	{
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("origBindPhone", origBindPhone);
		paramMap.put("bindPhone", bindPhone);
		Integer result = (Integer) getBaseDao().selectObjectByObject("User.checkBindPhoneIsExistInModifyUser", paramMap);
		boolean flag = false;
		if (result > 0)
		{
			flag = true;
		}
		return flag;
	}
	
	@Override
	public Map<String, Object> getUserMapByBindPhone(String bindPhone,Long schoolId)
	{
		/*
		 * 处理逻辑如下
		 * 1.先根据bindPhone获取用户基本信息,判断是否是智慧校园用户
		 *   1.1  智慧校园用户
		 *   	1.1.1  根据userId获取用户的角色信息
		 *   	1.1.2 根据角色获取用户在学校的信息
		 *   1.2 亦信用户
		 *   	1.2.1 获取亦信的基本信息
		 */
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		User user = getUserByBindPhone(bindPhone);
		if(user == null){
			resultMap.put("flag", false);
			return resultMap;
		}
		resultMap.put("flag", true);
		resultMap.put("userId",user.getUserId());
		Integer userId = user.getUserId();
		String newSchoolId = "";
		List<User> schoolIdList = getUserSchoolByUserId(userId);
		if("1".equals(user.getSource()) && (!CollectionUtils.isEmpty(schoolIdList))){
			//智慧校园用户
			StringBuffer msgBuffer = new StringBuffer();
			String schoolName = "";
			msgBuffer.append(bindPhone+"已经被");
			//是否是本校
			boolean isLocalSchool = false;
			for(User newUser : schoolIdList)
			{
				newSchoolId = newUser.getSchoolId();
				schoolName = newUser.getSchoolName();
				if((schoolId+"").equals(newSchoolId))
				{
					isLocalSchool = true;
					break;
				}
			}
			if(!isLocalSchool){//非本校
				DBContextHolder.setDBType(newSchoolId);
				msgBuffer.append(schoolName+"的");
			}else{
				msgBuffer.append("本校的");	
			}
			resultMap.put("isLocalSchool",isLocalSchool);
			resultMap.put("isteacher",false);//是否是教职工
			resultMap.put("isYinUser",false);//是否是亦信
			resultMap.put("isDefalutUserName",user.getIsDefalutUserName());//是否是亦信
			List<UserRole> userRoleList = commonService.getUserRoleByUserId(userId);
			for(int j=0;j<userRoleList.size();j++)
			{
				UserRole ur = userRoleList.get(j);
				if("parent".equals(ur.getRoleCode()))
				{
					List<User> studentList=studentService.getStudentListByParentId(userId);//根据家长获得小孩
					if(!CollectionUtils.isEmpty(studentList))
					{
						for(int i=0;i<studentList.size();i++)
						{
							User student = studentList.get(i);
							if((i+1) == studentList.size()){
								msgBuffer.append(student.getClazzName()+":"+student.getRealName()+"的");
							}else {
								msgBuffer.append(student.getClazzName()+":"+student.getRealName()+",");
							}
						}
					}
					msgBuffer.append("'家长'");
				}else if("student".equals(ur.getRoleCode()))
				{
					msgBuffer.append("'学生'");
				}else
				{   
					//进入修改页面用到
					resultMap.put("isteacher",true);
					msgBuffer.append("'"+getRoleNameByRoleCode(ur.getRoleCode())+"',");
				}
			}
			msgBuffer.append("【"+user.getRealName()+"】使用");
			resultMap.put("msg", msgBuffer.toString());
		}else{
			//亦信用户
			resultMap.put("msg", "该号码已经被亦信用户【"+user.getNickName()+"】使用");
			resultMap.put("isYinUser",true);//是否是亦信
		}
		resultMap.put("user", user);
		//切换到原始的数据源
		DBContextHolder.setDBType(schoolId+"");
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getUserMapByUserId(Integer userId,Long schoolId)
	{
		/*
		 * 处理逻辑如下
		 * 1.先根据userId获取用户基本信息,判断是否是智慧校园用户
		 *   1.1  智慧校园用户
		 *   	1.1.1  根据userId获取用户的角色信息
		 *   	1.1.2 根据角色获取用户在学校的信息
		 *   1.2 亦信用户
		 *   	1.2.1 获取亦信的基本信息
		 */
		List<User> userList = getUserByUserId(userId);
		String newSchoolId = "";
		List<School> schoolList = new ArrayList<School>();
		User user = new User();
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("msg","");
		resultMap.put("msg","");
		resultMap.put("isLocalSchool",false);
		resultMap.put("isYinUser",false);
		try{
			for(User tmpUser : userList){
				if(!StringUtil.isEmpty(tmpUser.getSchoolId()))
				{
					School school = new School();
					school.setSchoolId(Long.valueOf(tmpUser.getSchoolId()));
					school.setSchoolName(tmpUser.getSchoolName());
					schoolList.add(school);
				}
				user = tmpUser;
			}
			if("1".equals(user.getSource()) && (!CollectionUtils.isEmpty(schoolList))){
				//智慧校园用户
				StringBuffer msgBuffer = new StringBuffer();
				String schoolName = "";
				//是否是本校
				boolean isLocalSchool = false;
				for(School school : schoolList)
				{
					newSchoolId = school.getSchoolId()+"";
					schoolName = school.getSchoolName();
					if((schoolId+"").equals(newSchoolId))
					{
						isLocalSchool = true;
						break;
					}
				}
				if(!isLocalSchool){//非本校
					DBContextHolder.setDBType(newSchoolId);
				}
				msgBuffer.append(schoolName+"的");	
				resultMap.put("isLocalSchool",isLocalSchool);
				resultMap.put("isteacher",false);
				List<UserRole> userRoleList = commonService.getUserRoleByUserId(userId);
				for(int j=0;j<userRoleList.size();j++)
				{
					UserRole ur = userRoleList.get(j);
					if("parent".equals(ur.getRoleCode()))
					{
						List<User> studentList=studentService.getStudentListByParentId(userId);//根据家长获得小孩
						if(!CollectionUtils.isEmpty(studentList))
						{
							for(int i=0;i<studentList.size();i++)
							{
								User student = studentList.get(i);
								if((i+1) == studentList.size()){
									msgBuffer.append(student.getClazzName()+":"+student.getRealName()+"的");
								}else {
									msgBuffer.append(student.getClazzName()+":"+student.getRealName()+",");
								}
							}
						}
						msgBuffer.append("'家长'");
					}else if("student".equals(ur.getRoleCode()))
					{
						msgBuffer.append("'学生'");
					}else
					{   
						//进入修改页面用到
						resultMap.put("isteacher",true);
						resultMap.put("userId",user.getUserId());
						msgBuffer.append("'"+getRoleNameByRoleCode(ur.getRoleCode())+"',");
						
					}
				}
				msgBuffer.append("【"+user.getRealName()+"】");
				resultMap.put("msg", msgBuffer.toString());
				resultMap.put("isYinUser",false);
			}else{
				//亦信用户
				resultMap.put("msg", "亦信用户【"+user.getNickName()+"】");
				resultMap.put("isYinUser",true);
			}
			resultMap.put("user", user);
		}catch(Exception e){
			
			
		}
		//切换到原始的数据源
		DBContextHolder.setDBType(schoolId+"");
		return resultMap;
	}
	
	@Override
	public User getUserByBindPhone(String bindPhone){
		return (User) this.getBaseDao().selectObjectByObject("User.getUserByBindPhone", bindPhone);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUserSchoolByUserId(Integer userId)
	{
		List<User> list = this.getBaseDao().selectListByObject("User.getUserSchoolByUserId", userId);
		return list;
	}
	
	/**
	 * 根据roleCode获取角色名称
	 * @param roleCode
	 * @return
	 */
	private String getRoleNameByRoleCode(String roleCode){
		String roleName ="";
		if("admin".equals(roleCode)){
			roleName = "学校管理员";
		}else if("chairman".equals(roleCode)){
			roleName = "董事长";
		}else if("classLeader".equals(roleCode)){
			roleName = "班主任";
		}else if("departManager".equals(roleCode)){
			roleName = "部门管理员";
		}else if("employee".equals(roleCode)){
			roleName = "职工";
		}else if("other".equals(roleCode)){
			roleName = "其它";
		}else if("parent".equals(roleCode)){
			roleName = "家长";
		}else if("president".equals(roleCode)){
			roleName = "校长";
		}else if("student".equals(roleCode)){
			roleName = "学生";
		}else if("teacher".equals(roleCode)){
			roleName = "教师";
		}
		return roleName;
	}
	
	@Override
	public Integer addUserBase(User userBase){
		Integer userId = getBaseDao().addObject("User.addUserBase", userBase);
		return userId;
	}
	
	@Override
	public void addUserDetail(User userDetail){
		getBaseDao().addObject("User.addUserDetail", userDetail);
	}
	
	@Override
	public void addUserAndSchoolLink(Integer userId, Long schoolId)
	{
		if(!isExistsInSchool(userId, schoolId)){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("schoolId", schoolId);
			map.put("type", 1);
			getBaseDao().addObject("User.addUserSchool", map);
		}
	}
	
	@Override
	public boolean isExistsInSchool(Integer userId, Long schoolId)
	{
		boolean flag = false;
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("schoolId", schoolId);
		Integer count = (Integer) getBaseDao().selectObjectByObject("User.getCountByUserIdAndSchoolId", map);
		if (count > 0)
		{
			flag = true;
		}
		return flag;

	}
	
	@Override
	public void deleteUserRole(Integer userId,String roleCode){
		// 删除role
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("roleCode", roleCode);
		this.getBaseDao().deleteObject("UserRole.deleteUserRoleByCondition", map);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deleteUserRolePermission(Integer userId,String roleCode)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		if(!StringUtil.isEmpty(roleCode)){
			// 删除role对应的权限
			map.put("roleCode", roleCode);
			List<RolePermission> list = getBaseDao().selectListByObject("RolePermission.getPermissionListByRoleCode", roleCode);
			for (RolePermission rp : list)
			{
				map.put("permissionCode", rp.getPermissionCode());
				this.getBaseDao().deleteObject("UserPermission.deleteUserPermissionByCondition", map);
			}
		}else{
			this.getBaseDao().deleteObject("UserPermission.deleteUserPermissionByCondition", map);
		}
	}
	
	@Override
	public void delUserAndSchoolLink(Integer userId,Long schoolId)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("schoolId", schoolId);
		getBaseDao().deleteObject("User.delUserAndSchoolLink", map);
	}
	/**
	 * 根据学生id，学校id 查询钱包余额大于传入钱数的家长
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUserAccountMoreThanPrice(HashMap<String, Object> param)
	{
		return (List<User>)getBaseDao().selectListByObject("User.getUserAccountMoreThanPrice", param);
	}
	
	@Override
	public void deleteUserCode(Integer userId)
	{
		getBaseDao().updateObject("User.deleteUserCode", userId);
	}
	
	/**
	 * 根据学校ID和家长ID获取小孩信息
	 */
	@SuppressWarnings("unchecked")
	public List<User> getStudentListByParentIdAndSchoolId(Map<String, Object>  map)
	{
		return (List<User>)getBaseDao().selectListByObject("User.getStudentListByParentIdAndSchoolId", map);
	}

	@Override
	public void delUser(Integer userId)
	{
		getBaseDao().deleteObject("User.delUser", userId);
	}

	@Override
	public void delUserFriend(Integer userId)
	{
		getBaseDao().deleteObject("User.delUserFriend", userId);
	}

	@Override
	public void delUserUserPhones(Integer userId)
	{
		getBaseDao().deleteObject("User.delUserUserPhones", userId);
	}
	
	@Override
	public void addUserPhones(Integer userId,String phone){
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("userId", userId);
		parmMap.put("phone", phone);
		parmMap.put("status", 1);
		getBaseDao().addObject("User.addUserPhones", parmMap);
	}
	
	/**
	 * 获得手机端技术支持的人员的手机号
	 * @author chenyong
	 * @Time 2016年12月27日 下午6:34:09
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getTechnicalSupportPhone(String bindPone) {
		return (List<User>)getBaseDao().selectListByObject("User.getTechnicalSupportPhone", bindPone);
	}
	
	/**
	 * 验证登录账号是否存在
	 * @param userName
	 * @return
	 */
	public boolean checkUserName(String userName)
	{
		boolean flag = false;
		Integer count = (Integer)getBaseDao().selectObjectByObject("User.checkUserName", userName);
		if(count>0)
		{
			flag = true;
		}
		else
		{
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 根据用户ID获取对应的部门领导人用户推送
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getLeaderByUserIdForPush(Integer userId)
	{
		return (List<User>)getBaseDao().selectList("User.getLeaderByUserIdForPush", userId);
	}
	
	/**
	 *  根据用户ID获取对应的部门领导人包含校长管理员
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getLeaderListByUserId(Integer userId)
	{
		return (List<User>)getBaseDao().selectList("User.getLeaderListByUserId", userId);
	}
	
	public User getUserByUserIdForPush(Integer userId){
		User receiver = (User) getBaseDao().selectObject("User.getUserByUserIdForPush", userId);
		return receiver;
	}
	
	/**
	 * 获取登录用户是否有权限使用该功能,购买了服务费能够查看，没有购买服务费的查看是否开启限制
	 * @param menuId 限制功能Id
	 * @param user
	 * @return
	 */
	public boolean getLoginUserHasPermission(String menuId,User user){
		
		boolean isTeacher = false;
		boolean isParent = false;
		boolean isStudent = false;
		boolean hasPermission = false;

		boolean schoolHasMessage = (boolean) smsService.getSmsServiceStatus().get("schoolHasMessage");// 学校是否有短信套餐
		//boolean isVirtualOpen = (boolean) smsService.getSmsServiceStatus().get("isVirtualOpen");// 学校虚拟套餐是否开启
		boolean isSchoolSingleMsg = (boolean) smsService.getSmsServiceStatus().get("isSchoolSingleMsg");// 是否有学校单条类型套餐

		List<Role> roleList = user.getRoleList();
		for (Role role : roleList)
		{
			String roleCode = role.getRoleCode();
			if ((!"parent".equals(roleCode)) && (!"student".equals(roleCode)))
			{
				isTeacher = true;
			}
			if ("parent".equals(roleCode))
			{
				isParent = true;
			}
			if ("student".equals(roleCode))
			{
				isStudent = true;
			}
		}
		//获取学生列表
		List<User> studentList = new ArrayList<User>();
		if (isStudent)
		{
			studentList.add(user);
		} else if (isParent)
		{
			List<User> studentList_tmp = commonService.getAllChildrenByUserId(user.getUserId());
			studentList.addAll(studentList_tmp);
		}
		
		Integer status = 0;
		OrderMessagePermission orderMessagePermission = orderMessagePermissionService.getMessagePermissionByMenuId(menuId);
		if (orderMessagePermission != null)
		{
			// 0不限制 1限制
			status = orderMessagePermission.getStatus();
			//如果是限制的话，判断登录用户在不在限制的年级中
			if(status == 1)
			{
				List<OrderMessagePermissionDetail> detailList = orderMessagePermission.getDetailList();
				if(!CollectionUtils.isEmpty(studentList)){
					for(User student : studentList)
					{
						Integer gradeId = getGradeIdByStudentId(student.getUserId());
						if(!CollectionUtils.isEmpty(detailList))
						{
							boolean isExist = false;
							for(OrderMessagePermissionDetail detail:detailList){
								if(detail.getGradeId().equals(gradeId)){
									isExist = true;
									break;
								}
							}
							if(!isExist){
								status = 0;
							}
						}
					}
				}
			}
		}
		
		/*
		 * 1.教师不受功能限制影响
		 * 2.试用短信套餐是否开启和功能限制没有关系
		 * 3.功能限制开启的情况下，学校没有套餐 或者有套餐的情况下家长已经购买 或者单条套餐还存在余额的情况下是能够查看
		 */
		if (isTeacher)
		{
			hasPermission = true;
		} else
		{
			//未缴纳服务费是否限制功能使用 0不限制 1限制
			if (status == 0)// 不限制
			{
				hasPermission = true;
			} else
			{
				if (schoolHasMessage)// 学校有短信套餐
				{
					if (isSchoolSingleMsg)// 是否有学校单条套餐 有判断条数
					{
						hasPermission = true;
					} else
					{								
						for (User student : studentList)
						{
							// 先判断学生有没有购买过
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("userId", student.getUserId());
							map.put("status", 0);
							
							OrderMessageUser messageUser = orderMessageService.getOrderMessageUserByUserId(map);
							
							if (messageUser != null)
							{
								if (messageUser.getType() == 0)
								{
									if (TimeUtil.nowDateIsBetween(messageUser.getStartTime(), messageUser.getEndTime()))
									{
										hasPermission = true;
										break;
									} else
									{  //存在学生服务费没有购买
										hasPermission = false;
										
									}

								} else
								{
									if (messageUser.getCount() > 0)
									{
										hasPermission = true;
										break;
									} else
									{
										hasPermission = false;
										
									}
								}
							}else{
								//可能服务费价格为0，没有购买记录
								Map<String, Object> studentMap = new HashMap<String, Object>();
								if(student.getClazzId()==null){
									User u=studentService.getStudentById(student.getUserId());
									student.setClazzId(u.getClazzId());
								}
								studentMap.put("clazzId", student.getClazzId());
								OrderMessageClazz orderMessageClazz = orderMessageService.getMessageClazzByClazzIdAndOrderMessageId(studentMap);
								if(orderMessageClazz!=null){
									int productId = orderMessageClazz.getOrderMessageId();	
									OrderMessage orderMessage = orderMessageService.getMessageById(productId);
									if(orderMessage!=null && orderMessage.getServicePrice()==0){//服务费不需要购买
										hasPermission = true;
										break;
									}else{
										hasPermission = false;
									}
								}
									
							}
						}
					}
				} else
				{
					hasPermission = true;
				}
			}
		}
		return hasPermission;
	}
	/**
	 * 判断学生的是否有家长是教师
	 * @author chenyong
	 * @Time 2017年2月23日 下午1:39:14
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getParentIsTeacher(List<Integer> list) {
		
		return getBaseDao().selectListByObject("User.getParentIsTeacher", list);
	}
	/**
	 * 根据班级，学生姓名获得学生
	 * @author chenyong
	 * @Time 2017年3月13日 下午2:02:01
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getSyidentByClazzIdAndName(Map<String, Object> map) {
		
		return getBaseDao().selectListByObject("User.getSyidentByClazzIdAndName", map);
	}
	/**
	 * 根据学生userId集合获得家长
	 * @author chenyong
	 * @Time 2017年3月13日 下午2:16:02
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getParentByStudent(List<Integer> listuserId) {
		
		return this.getBaseDao().selectListByObject("User.getParentByStudent", listuserId);
	}
}