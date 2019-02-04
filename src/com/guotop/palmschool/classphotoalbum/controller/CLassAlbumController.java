package com.guotop.palmschool.classphotoalbum.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guotop.palmschool.classphotoalbum.entity.ClassPhotoAlbum;
import com.guotop.palmschool.classphotoalbum.entity.Photo;
import com.guotop.palmschool.classphotoalbum.entity.RecordsComment;
import com.guotop.palmschool.classphotoalbum.entity.UploadRecords;
import com.guotop.palmschool.classphotoalbum.service.ClassAlbumService;
import com.guotop.palmschool.classphotoalbum.service.PhotoService;
import com.guotop.palmschool.classphotoalbum.service.RecordsCommentService;
import com.guotop.palmschool.classphotoalbum.service.UploadRecordsService;
import com.guotop.palmschool.common.service.CommonService;
import com.guotop.palmschool.constant.Cons;
import com.guotop.palmschool.controller.BaseController;
import com.guotop.palmschool.entity.Clazz;
import com.guotop.palmschool.entity.Grade;
import com.guotop.palmschool.entity.Role;
import com.guotop.palmschool.entity.User;
import com.guotop.palmschool.listener.DBContextHolder;
import com.guotop.palmschool.personAlbum.entity.PersonAlbum;
import com.guotop.palmschool.personAlbum.entity.PersonPhoto;
import com.guotop.palmschool.personAlbum.service.PersonAlbumService;
import com.guotop.palmschool.service.ClazzService;
import com.guotop.palmschool.service.GradeService;
import com.guotop.palmschool.service.UserService;
import com.guotop.palmschool.util.FileDownloadUtil;
import com.guotop.palmschool.util.FileUploadUtil;
import com.guotop.palmschool.util.StringUtil;
import com.guotop.palmschool.util.TimeUtil;
import com.richx.pojo.PalmUser;
import com.richx.pojo.QiniuPhoto;
import com.richx.pojo.RichHttpResponse;

import dev.gson.GsonHelper;

/**
 * 班级相册控制类
 *
 */
@RequestMapping("/clazzAlbum")
@Controller
public class CLassAlbumController extends BaseController
{
	private Logger log = LoggerFactory.getLogger(CLassAlbumController.class);
	@Resource
	private UserService userService;

	@Resource
	private ClassAlbumService classAlbumService;

	@Resource
	private PhotoService photoService;

	@Resource
	private UploadRecordsService uploadRecordsService;

	@Resource
	private RecordsCommentService recordsCommentService;

	@Resource
	private ClazzService clazzService;
	
	@Resource
	private GradeService gradeService;

	@Resource
	private PersonAlbumService personAlbumService;

	@Resource
	private CommonService commonService;

	/**
	 * 进入班级相册页面
	 */
	@RequestMapping(value = "/toClazzPhotoAlbumList.do")
	public String toClazzPhotoAlbumList()
	{
		
		return "classphotoalbum/album_list";
	}

	/**
	 * 进入班级相册页面
	 */
	@RequestMapping(value = "/toAlbnum.do")
	public String toAlbnum(HttpServletRequest request, HttpSession session, ModelMap modelMap)
	{
		Integer gradeId = StringUtil.toint(request.getParameter("gradeId"));
		Integer clazzId = StringUtil.toint(request.getParameter("clazzId"));
		String roleCode = request.getParameter("roleCode");
		modelMap.addAttribute("gradeId", gradeId);
		modelMap.addAttribute("clazzId", clazzId);
		modelMap.addAttribute("roleCode", roleCode);
		return "classphotoalbum/album_list";
	}

	/**
	 * 加载年级列表 20151223
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loadGrade.do")
	public String loadGrade(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			Integer userId = StringUtil.toint(request.getParameter("userId"));
			String roleCode = request.getParameter("roleCode");

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", userId);
			paramMap.put("roleCode", roleCode);
			List<Grade> gradeList = gradeService.getGradeListByUserIdAndRoleCode(userId, roleCode);

			Gson gson = new Gson();
			String json = gson.toJson(gradeList);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 根据年级ID选择班级list
	 */
	@RequestMapping(value = "/loadClazzByGradeId.do")
	public String loadClazzByGradeId(HttpServletRequest request, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();

		Integer userId = StringUtil.toint(request.getParameter("userId"));
		Integer gradeId = StringUtil.toint(request.getParameter("gradeId"));
		String roleCode = request.getParameter("roleCode");
		try
		{
			List<Clazz> clazzList = clazzService.getClazzListByGradeIdAndUserIdAndRoleCode(StringUtil.toint(gradeId), roleCode, userId);
			String json = gson.toJson(clazzList);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 添加相册
	 */
	@RequestMapping(value = "/addAlbum.do")
	public String addAlbum(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相册名称
			String albumName = request.getParameter("albumName");
			// 相册描述
			String albumDesc = request.getParameter("albumDescription");
			// 创建日期
			String createTime = TimeUtil.date();
			// 班级Id
			Integer clazzId = StringUtil.toint(request.getParameter("clazzId"));
			// 班级名称
			String clazzName = request.getParameter("clazzName");

			User user = (User) session.getAttribute("user");

			ClassPhotoAlbum album = new ClassPhotoAlbum();
			album.setAlbumName(albumName);
			album.setAlbumDesc(albumDesc);
			album.setClazzId(clazzId);
			album.setClazzName(clazzName);
			album.setCreateUserId(user.getUserId());
			album.setCreateUserName(user.getRealName());
			album.setCreateTime(createTime);
			album.setUpdateTime(TimeUtil.getInstance().now());

			Integer albumId = classAlbumService.addAlbum(album);

			album.setAlbumId(albumId);
			Gson gson = new Gson();
			String json = gson.toJson(album);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据clazzId加载相册
	 */
	@RequestMapping(value = "/loadAllAlbumList.do")
	public String loadAllAlbumList(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 班级Id
			Integer clazzId = StringUtil.toint(request.getParameter("clazzId"));

			List<ClassPhotoAlbum> albumList = classAlbumService.getAlbumListByClazzId(clazzId);
			for (ClassPhotoAlbum album : albumList)
			{
				if (StringUtil.isEmpty(album.getAlbumPath()))
				{
					Photo photo = classAlbumService.getPhotoByAlbumId(album.getAlbumId());
					if (photo != null)
					{
						album.setAlbumPath(photo.getPhotoPath());
					}
				}
			}
			Gson gson = new Gson();
			String json = gson.toJson(albumList);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据albumId修改相册
	 */
	@RequestMapping(value = "/modifyAlbumByAlbumId.do")
	public String modifyAlbumByAlbumId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相册Id
			Integer albumId = StringUtil.toint(request.getParameter("albumId"));
			// 相册名称
			String albumName = request.getParameter("albumName");
			// 相册描述
			String albumDesc = request.getParameter("albumDescription");

			ClassPhotoAlbum album = new ClassPhotoAlbum();
			album.setAlbumId(albumId);
			album.setAlbumName(albumName);
			album.setAlbumDesc(albumDesc);
			album.setUpdateTime(TimeUtil.getInstance().now());

			classAlbumService.modifyAlbumByAlbumId(album);
			Gson gson = new Gson();
			String json = gson.toJson(album);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 根据albumId删除相册和下面的所有图片 上传记录 评论等
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/deleteAlbumByAlbumId.do")
	public String deleteAlbumByAlbumId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response) throws Exception
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相册Id
			Integer albumId = StringUtil.toint(request.getParameter("albumId"));

			// 根据相册Id 查询对应的相片 用于七牛上删除对应的相片
			List<Photo> photoList = photoService.getPhotoDetailByAlbumId(albumId);

			classAlbumService.deleteAlbumByAlbumId(albumId);

			if (photoList.size() > 0)
			{
				FileUploadUtil fuu = new FileUploadUtil();
				for (Photo photo : photoList)
				{
					String photoPath = photo.getPhotoPath();
					fuu.simpleDelete(Cons.QINIU_BUCKETNAME_CLASSALBUM, photoPath.substring(Cons.QINIU_URL_CLASSALBUM.length()));
				}
			}
			Gson gson = new Gson();
			String json = gson.toJson(true);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 设置相册封面
	 */
	@RequestMapping(value = "/setAlbumCover.do")
	public String setAlbumCover(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相册Id
			Integer albumId = StringUtil.toint(request.getParameter("albumId"));
			// 相册封面路劲
			String albumPath = request.getParameter("albumPath");

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("albumId", albumId);
			paramMap.put("albumPath", albumPath);
			classAlbumService.setAlbumCover(paramMap);

			Gson gson = new Gson();
			String json = gson.toJson(true);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据albumId加载相片
	 */
	@RequestMapping(value = "/loadPhotoDetailByAlbumId.do")
	public String loadPhotoDetailByAlbumId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相册Id
			Integer albumId = StringUtil.toint(request.getParameter("albumId"));

			ClassPhotoAlbum album = classAlbumService.getAlbumByAlbumId(albumId);
			List<Photo> photoList = photoService.getPhotoDetailByAlbumId(albumId);
			if (album != null && StringUtil.isEmpty(album.getAlbumPath()) && (photoList.size() > 0))
			{
				album.setAlbumPath(photoList.get(0).getPhotoPath());
			}

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("album", album);
			map.put("photoList", photoList);
			Gson gson = new Gson();
			String json = gson.toJson(map);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据albumId加载所有的照片和 其对应的评论 点击照片时用
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/getPhotoDetailByAlbumId.do")
	public String getPhotoDetailByAlbumId(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap)
	{
		response.setCharacterEncoding("UTF-8");
		Integer albumId = StringUtil.toint(request.getParameter("albumId"));

		ClassPhotoAlbum album = classAlbumService.getAlbumByAlbumId(albumId);
		List<Photo> photoList = photoService.getPhotoListAndCommentListByAlbumId(albumId);
		List<Photo> result_photoList = new ArrayList<Photo>();
		Map<Integer, Photo> photoMap = new HashMap<Integer, Photo>();
		if (photoList != null && photoList.size() > 0)
		{
			for (Photo photo : photoList)
			{
				Integer photoId = photo.getPhotoId();
				Integer commentId = photo.getCommentId();
				if (commentId != null)
				{
					Integer parentId = photo.getParentId();
					Integer commentType = photo.getCommentType();
					RecordsComment rc = new RecordsComment();
					rc.setCommentId(commentId);
					rc.setParentId(parentId);
					rc.setCommentType(commentType);
					rc.setContent(photo.getContent());
					rc.setCommentUserId(photo.getCommentUserId());
					rc.setCommentUserName(photo.getCommentUserName());
					rc.setCreateTime(photo.getCreateTime());
					rc.setCommentReplyId(photo.getCommentReplyId());
					rc.setCommentReplyUserId(photo.getCommentReplyUserId());
					rc.setCommentReplyUserName(photo.getCommentReplyUserName());
					rc.setHeadImg(photo.getHeadImg());
					if (commentType == 0)
					{
						// 点赞
						photo.getPraiseList().add(rc);
					} else
					{
						photo.getCommentMap().put(Integer.valueOf(commentId), rc);

						if (parentId == 0 || StringUtil.isEmpty(parentId + ""))
						{
							// 父级评论
							photo.getCommentList().add(rc);
						} else
						{
							Map<Integer, List<RecordsComment>> commentSubMap = photo.getCommentSubMap();
							if (!commentSubMap.containsKey(parentId))
							{
								List<RecordsComment> subRecordsCommentsList = new ArrayList<RecordsComment>();
								subRecordsCommentsList.add(rc);
								commentSubMap.put(parentId, subRecordsCommentsList);
							} else
							{
								commentSubMap.get(parentId).add(rc);
							}
						}
					}
				}
				if (!photoMap.containsKey(photoId))
				{
					photoMap.put(photoId, photo);
				} else
				{
					Photo photo_tmp = photoMap.get(photoId);
					if (commentId != null)
					{
						Integer parentId = photo.getParentId();
						Integer commentType = photo.getCommentType();
						if (commentType == 0)
						{
							photo_tmp.getPraiseList().addAll(photo.getPraiseList());
						} else
						{
							photo_tmp.getCommentMap().putAll(photo.getCommentMap());
							if (parentId == 0 || StringUtil.isEmpty(parentId + ""))
							{
								photo_tmp.getCommentList().addAll(photo.getCommentList());
							} else
							{
								Map<Integer, List<RecordsComment>> commentSubMap = photo_tmp.getCommentSubMap();
								if (!commentSubMap.containsKey(parentId))
								{
									List<RecordsComment> subRecordsCommentsList = new ArrayList<RecordsComment>();
									subRecordsCommentsList.add(photo.getCommentMap().get(commentId));
									commentSubMap.put(parentId, subRecordsCommentsList);
								} else
								{
									commentSubMap.get(parentId).addAll(photo.getCommentSubMap().get(parentId));
								}
							}
						}
					}
				}
			}
			result_photoList = new ArrayList<Photo>(photoMap.values());
			Collections.sort(result_photoList, new Comparator<Photo>()
			{
				public int compare(Photo dish1, Photo dish2)
				{
					return dish1.getPhotoId().compareTo(dish2.getPhotoId());
				}
			});

		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("album", album);
		map.put("photoList", result_photoList);
		/*
		 * flush到页面
		 */
		Gson gson = new Gson();
		String json = gson.toJson(map);
		try
		{
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据photoId修改照片
	 */
	@RequestMapping(value = "/modifyPhotoByPhotoId.do")
	public String modifyPhotoByPhotoId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 照片Id
			Integer photoId = StringUtil.toint(request.getParameter("photoId"));
			// 照片名称
			String photoName = request.getParameter("photoName");
			// 照片描述
			String photoDesc = request.getParameter("photoDesc");

			Photo photo = new Photo();
			photo.setPhotoId(photoId);
			photo.setPhotoName(photoName);
			photo.setPhotoDesc(photoDesc);

			photoService.modifyPhotoByPhotoId(photo);

			Gson gson = new Gson();
			String json = gson.toJson(photo);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据photoId删除相片和下面的所有图片
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/deletePhotoByPhotoId.do")
	public String deletePhotoByPhotoId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response) throws Exception
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 相片Id
			Integer photoId = StringUtil.toint(request.getParameter("photoId"));

			// 根据相片id查询对应记录Id下面的所有照片 如果只有一张则对应的记录也删除 否则只删除照片
			List<Photo> photoList = photoService.getPhotoList(photoId);
			if (photoList.size() == 1)
			{
				Integer recordsId = photoList.get(0).getRecordsId();
				// 根据recordsId 删除对应的上传记录及对应的评论
				uploadRecordsService.deleteRecordsByRecordsId(recordsId);
			}

			// 根据photoId查询对应的相片 用于七牛上删除对应的相片
			Photo photo = photoService.getPhotoByPhotoId(photoId);

			// 删除照片
			photoService.deletePhotoByPhotoId(photoId);

			if (photo != null)
			{
				FileUploadUtil fuu = new FileUploadUtil();
				String photoPath = photo.getPhotoPath();
				fuu.simpleDelete(Cons.QINIU_BUCKETNAME_CLASSALBUM, photoPath.substring(Cons.QINIU_URL_CLASSALBUM.length()));
			}

			Gson gson = new Gson();
			String json = gson.toJson(true);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	public String getTempRoot()
	{
		return "D:/Guolang/WisdomCampus/";
	}

	public String getEndWith(String filename)
	{
		int i = filename.lastIndexOf(".");
		if (i >= 0)
		{
			return filename.substring(i + 1, filename.length());
		}
		return "";
	}

	/**
	 * 上传图片
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/uploadPhoto.do")
	public String uploadPhoto(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			User loginUser = (User) session.getAttribute("user");

			Integer albumId = StringUtil.toint(request.getParameter("albumId"));

			String uploadPhoto = request.getParameter("photos");

			String roleCode = request.getParameter("roleCode");
			
			// 分享的学生
			String student = request.getParameter("student");
			
			// 上传照片的数量
			int num = 0;
			
			if(!StringUtil.isEmpty(uploadPhoto)){
				String[] photos = uploadPhoto.split(",");
				if (!StringUtil.isEmpty(uploadPhoto))
				{
					// 上传相册的Id
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					// 上传记录的时间
					String createTime = formatter.format(new Date());
					UploadRecords uploadRecords = new UploadRecords();
					uploadRecords.setAlbumId(albumId);
					uploadRecords.setUploadUserId(loginUser.getUserId());
					uploadRecords.setUploadUserName(loginUser.getRealName());
					uploadRecords.setCreateTime(createTime);
					uploadRecords.setRecordsType(1);

					// 添加上传记录 并返回上传Id
					Integer recordsId = uploadRecordsService.addRecords(uploadRecords);

					for (int i = 0; i < photos.length; i++)
					{
						String photoName = photos[i].substring(photos[i].lastIndexOf("/") + 5, photos[i].lastIndexOf("."));

						Photo photo = new Photo();
						photo.setAlbumId(albumId);
						photo.setRecordsId(recordsId);
						photo.setPhotoName(photoName);
						photo.setPhotoPath(Cons.QINIU_URL_CLASSALBUM + photos[i]);
						photo.setCreateUserId(loginUser.getUserId());
						photo.setCreateUserName(loginUser.getRealName());
						photo.setMediaType(1);//默认图文1
						photoService.addPhoto(photo);
						num++;
					}

					modelMap.addAttribute("recordsId", recordsId);

					if (!StringUtil.isEmpty(student))
					{
						// 上传照片是选择分享分
						shareWhenUpload(student, photos);
					}
				}
			}
			modelMap.addAttribute("uploadCom", "uploadCom");
			modelMap.addAttribute("num", num);
			modelMap.addAttribute("roleCode", roleCode);
			modelMap.addAttribute("albumId", albumId);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return "classphotoalbum/album_list";

	}

	public void shareWhenUpload(String student, String[] photos)
	{

		String[] students = student.split(",");

		List<PersonPhoto> photoList = new ArrayList<PersonPhoto>();

		for (int i = 0; i < students.length; i++)
		{
			Integer userId = StringUtil.toint(students[i].split(":")[0]);
			String userName = students[i].split(":")[1];
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", userId);
			paramMap.put("albumName", "班级相册分享");
			PersonAlbum album = personAlbumService.getPersonAlbumByUserIdAndAlbumName(paramMap);
			if (album == null)
			{
				album = new PersonAlbum();
				album.setAlbumName("班级相册分享");
				album.setUserId(userId);
				album.setUserName(userName);
				album.setCreateTime(TimeUtil.date());
				album.setUpdateTime(TimeUtil.getInstance().now());
				Integer albumId = personAlbumService.addPersonAlbum(album);
				album.setAlbumId(albumId);
			}

			for (int j = 0; j < photos.length; j++)
			{
				PersonPhoto photo = new PersonPhoto();
				photo.setAlbumId(album.getAlbumId());
				photo.setUserId(userId);
				photo.setUserName(userName);
				photo.setPhotoPath(Cons.QINIU_URL_CLASSALBUM + photos[j]);
				photo.setPhotoName(photos[j].substring(photos[j].lastIndexOf("/") + 5, photos[j].lastIndexOf(".")));
				photo.setMediaType(1);//默认图文
				photoList.add(photo);
			}
		}

		if (photoList.size() > 0)
		{
			personAlbumService.sharePhoto(photoList);
		}
	}

	/**
	 * 根据recordsId批量修改照片
	 */
	@RequestMapping(value = "/savePhotoByRecordsId.do")
	public String savePhotoByRecordsId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		try
		{
			// 上传记录Id
			Integer recordsId = StringUtil.toint(request.getParameter("recordsId"));
			// 照片名称
			String photoUpName = request.getParameter("photoUpName");
			// 照片描述
			String photoUpDesc = request.getParameter("photoUpDesc");

			Photo photo = new Photo();
			photo.setRecordsId(recordsId);
			photo.setPhotoName(photoUpName);
			photo.setPhotoDesc(photoUpDesc);

			if (!photoUpName.equals("") || !photoUpDesc.equals(""))
			{
				photoService.savePhotoByRecordsId(photo);
			}

			Gson gson = new Gson();
			String json = gson.toJson(photo);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据相册的Id查出所有的照片并导出zip
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/exportPhotoByAlbumId.do")
	public String exportPhotoByAlbumId(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response) throws IOException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		String basePath = request.getSession().getServletContext().getRealPath("");

		Integer albumId = StringUtil.toint(request.getParameter("albumId"));
		String albumName = request.getParameter("albumName");
		List<Photo> photoList = photoService.getPhotoDetailByAlbumId(albumId);
		// 临时文件路径
		String path_tmp = "D:/clazzPhoto/" + albumName + "_" + albumId + "/";
		List<File> files = new ArrayList<File>();
		HashMap<String, Object> photoNameMap = new HashMap<String, Object>();
		int count = 0;
		for (Photo photo : photoList)
		{
			count++;
			if (photoNameMap.containsKey(photo.getPhotoName()))
			{
				photoNameMap.put(photo.getPhotoName() + count, photo);
				photo.setPhotoName(photo.getPhotoName() + count);
			} else
			{
				photoNameMap.put(photo.getPhotoName(), photo);
			}
			boolean flag = FileDownloadUtil.getRemoteFile(photo.getPhotoPath(), path_tmp, photo.getPhotoName() + ".jpg");
			if (flag)
			{
				File file = new File(path_tmp + photo.getPhotoName() + ".jpg");
				files.add(file);
			}
		}

		String fileName = albumName + ".zip";
		// 在服务器端创建打包下载的临时文件
		String outFilePath = basePath + "\\" + fileName;
		File file = new File(outFilePath);
		FileOutputStream outStream = new FileOutputStream(file);
		ZipOutputStream toClient = new ZipOutputStream(outStream);
		try
		{
			FileDownloadUtil.zipFile(files, toClient);
			toClient.close();
			outStream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ServletException e)
		{
			e.printStackTrace();
		}
		FileDownloadUtil.downloadZip(file, response);
		try
		{
			Gson gson = new Gson();
			String json = gson.toJson(fileName);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	@RequestMapping(value = "/deleteZipFile.do")
	public void deleteZipFile(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response) throws UnsupportedEncodingException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		String basePath = request.getSession().getServletContext().getRealPath("");

		String fileName = request.getParameter("fileName");
		String outFilePath = basePath + "\\" + fileName;
		File file = new File(outFilePath);
		if (file.length() > 0)
		{
			file.delete();
		}
	}

	/**
	 * 分享照片到个人相册
	 * 
	 * @param request
	 * @param session
	 * @param modelMap
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/sharePhoto.do")
	public String sharePhoto(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response) throws UnsupportedEncodingException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		try
		{
			// 照片名称
			String photoPath = request.getParameter("photoPath");
			// 照片描述
			String student = request.getParameter("student");

			String[] photoPaths = photoPath.split(",");

			String[] students = student.split(",");

			List<PersonPhoto> photoList = new ArrayList<PersonPhoto>();

			for (int i = 0; i < students.length; i++)
			{
				Integer userId = StringUtil.toint(students[i].split(":")[0]);
				String userName = students[i].split(":")[1];
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("userId", userId);
				paramMap.put("albumName", "班级相册分享");
				PersonAlbum album = personAlbumService.getPersonAlbumByUserIdAndAlbumName(paramMap);
				if (album == null)
				{
					album = new PersonAlbum();
					album.setAlbumName("班级相册分享");
					album.setUserId(userId);
					album.setUserName(userName);
					album.setCreateTime(TimeUtil.date());
					album.setUpdateTime(TimeUtil.getInstance().now());
					Integer albumId = personAlbumService.addPersonAlbum(album);
					album.setAlbumId(albumId);
				}

				for (int j = 0; j < photoPaths.length; j++)
				{
					PersonPhoto photo = new PersonPhoto();
					photo.setAlbumId(album.getAlbumId());
					photo.setUserId(userId);
					photo.setUserName(userName);
					photo.setPhotoPath(photoPaths[j]);
					photo.setPhotoName(photoPaths[j].substring(photoPaths[j].lastIndexOf("/") + 5, photoPaths[j].lastIndexOf(".")));
					photo.setMediaType(1);//默认图文
					photoList.add(photo);
				}
			}

			if (photoList.size() > 0)
			{
				personAlbumService.sharePhoto(photoList);
			}

			Gson gson = new Gson();
			String json = gson.toJson(true);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 获取班级相册列表 用于成长档案
	 * 
	 * @param request
	 * @param session
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getAlbum4Growth.do")
	public String getAlbum4Growth(HttpServletRequest request, HttpSession session, HttpServletResponse response)
	{
		response.setCharacterEncoding("utf-8");
		String studentId = request.getParameter("studentId");
		try
		{
			List<Clazz> clazzList = clazzService.getClazzListByStudentId(Integer.valueOf(studentId));
			if (clazzList != null && clazzList.size() > 0)
			{
				List<ClassPhotoAlbum> albumList = classAlbumService.getAlbumListByClazzId(clazzList.get(0).getId());
				response.getWriter().write(GsonHelper.toJson(albumList));
				response.getWriter().flush();
			}

		} catch (Exception e)
		{
			log.error(e.getMessage());
		}
		return null;
	}

	/**
	 * ---------------------------下面是手机端班级相册接口----------------------------------
	 * ------------
	 */

	/**
	 * 根据clazzId加载相册
	 */
	@RequestMapping(value = "/loadAllAlbumListByApiKey.do")
	public String loadAllAlbumListByApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<List<ClassPhotoAlbum>> rhr = new RichHttpResponse<List<ClassPhotoAlbum>>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		int clazzId = StringUtil.toint(request.getParameter("deClazzId"));
		String json;
		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		if (loginUser != null)
		{
			session.setAttribute("user", loginUser);
			DBContextHolder.setDBType(loginUser.getSchoolId());

			List<ClassPhotoAlbum> albumList = classAlbumService.getAlbumListByClazzId(clazzId);
			for (ClassPhotoAlbum album : albumList)
			{
				if (StringUtil.isEmpty(album.getAlbumPath()))
				{
					Photo photo = classAlbumService.getPhotoByAlbumId(album.getAlbumId());
					if (photo != null)
					{
						album.setAlbumPath(photo.getPhotoPath());
					}
				}
			}
			rhr.ResponseCode = 0;
			rhr.ResponseResult = "获取列表成功";
			rhr.ResponseObject = albumList;
			json = GsonHelper.toJson(rhr);
		} else
		{
			rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
			rhr.ResponseObject = null;
			rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
			json = GsonHelper.toJson(rhr);
		}
		try
		{
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		}
		return null;

	}

	/**
	 * 【手机端】创建相册
	 */
	@RequestMapping(value = "/addAlbumByApiKey.do")
	public String addAlbumByApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		String albumName = request.getParameter("albumName");
		String albumDesc = request.getParameter("albumDescription");
		Integer clazzId = StringUtil.toint(request.getParameter("deClazzId"));
		String clazzName = request.getParameter("clazzName");
		String json;
		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		try
		{
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());
				// 创建日期
				String createTime = TimeUtil.date();

				ClassPhotoAlbum album = new ClassPhotoAlbum();
				album.setAlbumName(albumName);
				album.setAlbumDesc(albumDesc);
				album.setClazzId(clazzId);
				album.setClazzName(clazzName);
				album.setCreateUserId(loginUser.getUserId());
				album.setCreateUserName(loginUser.getRealName());
				album.setCreateTime(createTime);
				album.setUpdateTime(TimeUtil.getInstance().now());
				Integer albumId = classAlbumService.addAlbum(album);
				album.setAlbumId(albumId);

				rhr.ResponseCode = 0;
				rhr.ResponseResult = "创建相册成功";
				rhr.ResponseObject = album;
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}

			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			rhr.ResponseCode = -2;
			rhr.ResponseResult = "创建相册失败";
			json = GsonHelper.toJson(rhr);

			log.error("创建相册失败：" + e.getMessage());
		}
		return null;

	}

	/**
	 * 【手机端】上传图片
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/uploadPhotoByApiKey.do")
	public String uploadPhotoByApiKey(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		Integer albumId = StringUtil.toint(request.getParameter("albumId"));
		String photoDesc = request.getParameter("photoDesc");
		String qiniuPhotoUrlJson = request.getParameter("qiniuPhotoUrlJson");
		String studentListJson = request.getParameter("studentList");
		String recordsType = request.getParameter("recordsType");
		String json;

		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		try
		{
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				// 解析七牛图片地址list
				List<PalmUser> studentList = GsonHelper.fromJson(studentListJson, new TypeToken<ArrayList<PalmUser>>()
				{
				}.getType());

				// 解析七牛图片地址list
				List<QiniuPhoto> qiniuPhotoList = GsonHelper.fromJson(qiniuPhotoUrlJson, new TypeToken<ArrayList<QiniuPhoto>>()
				{
				}.getType());
				if (!CollectionUtils.isEmpty(qiniuPhotoList))
				{
					UploadRecords uploadRecords = new UploadRecords();
					uploadRecords.setRecordsDesc(photoDesc);
					uploadRecords.setAlbumId(albumId);
					uploadRecords.setUploadUserId(loginUser.getUserId());
					uploadRecords.setUploadUserName(loginUser.getRealName());
					uploadRecords.setCreateTime(TimeUtil.getInstance().nowFormat());
					uploadRecords.setRecordsType(StringUtil.toint(recordsType));
					// 添加上传记录 并返回上传Id
					Integer recordsId = uploadRecordsService.addRecords(uploadRecords);

					Integer num = 0;
					List<Photo> photoList = new ArrayList<Photo>();
					for (QiniuPhoto qiniuPhoto : qiniuPhotoList)
					{
						Photo photo = new Photo();
						photo.setAlbumId(albumId);
						photo.setRecordsId(recordsId);
						photo.setPhotoName(qiniuPhoto.photoName);
						photo.setPhotoPath(qiniuPhoto.photoUrl);
						photo.setCreateUserId(loginUser.getUserId());
						photo.setCreateUserName(loginUser.getRealName());
						photo.setPhotoDesc(photoDesc);
						if(null == qiniuPhoto.mediaType){
							photo.setMediaType(1);
						}else{
							photo.setMediaType(qiniuPhoto.mediaType);
						}
						
						photo.setMediaSecordUrl(qiniuPhoto.mediaSecordUrl);
						photoList.add(photo);
						
						num++;
						if (studentList != null && studentList.size() > 0)
						{
							List<PersonPhoto> personPhotoList = new ArrayList<PersonPhoto>();
							for (PalmUser student : studentList)
							{
								String userId = student.UserId;
								String userName = student.UserName;
								Map<String, Object> paramMap = new HashMap<String, Object>();
								paramMap.put("userId", userId);
								paramMap.put("albumName", "班级相册分享");
								PersonAlbum album = personAlbumService.getPersonAlbumByUserIdAndAlbumName(paramMap);
								if (album == null)
								{
									album = new PersonAlbum();
									album.setAlbumName("班级相册分享");
									album.setUserId(StringUtil.toint(userId));
									album.setUserName(userName);
									album.setCreateTime(TimeUtil.date());
									album.setUpdateTime(TimeUtil.getInstance().now());
									Integer aId = personAlbumService.addPersonAlbum(album);
									album.setAlbumId(aId);
								}
								PersonPhoto personPhoto = new PersonPhoto();
								personPhoto.setAlbumId(album.getAlbumId());
								personPhoto.setUserId(StringUtil.toint(userId));
								personPhoto.setUserName(userName);
								personPhoto.setPhotoPath(qiniuPhoto.photoUrl);
								personPhoto.setPhotoName(qiniuPhoto.photoName);
								personPhoto.setPhotoDesc(photoDesc);
								if(null == qiniuPhoto.mediaType){
									personPhoto.setMediaType(1);
								}else{
									personPhoto.setMediaType(qiniuPhoto.mediaType);
								}
								personPhoto.setMediaSecordUrl(qiniuPhoto.mediaSecordUrl);
								personPhotoList.add(personPhoto);
								
							}
							personAlbumService.addPersonPhotoBATCH(personPhotoList);
						}
					}
					photoService.addPhotoBATCH(photoList);
				}
				
				rhr.ResponseCode = 0;
				rhr.ResponseResult = "上传图片成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("手机端班级相册上传图片失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【手机端】根据albumId加载相片
	 */
	@RequestMapping(value = "/loadPhotoDetailByAlbumIdAndApiKey.do")
	public String loadPhotoDetailByAlbumIdAndApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<List<Photo>> rhr = new RichHttpResponse<List<Photo>>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		Integer albumId = StringUtil.toint(request.getParameter("albumId"));
		String json;
		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		try
		{
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				List<Photo> photoList = photoService.getPhotoDetailByAlbumId(albumId);

				rhr.ResponseCode = 0;
				rhr.ResponseResult = "上传图片成功";
				rhr.ResponseObject = photoList;
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}

			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("手机端班级相册加载图片时json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("手机端班级相册加载图片失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【手机端】根据albumId删除相册和下面的所有图片 上传记录 评论等
	 */
	@RequestMapping(value = "/deleteAlbumByAlbumIdAndApiKey.do")
	public String deleteAlbumByAlbumIdAndApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		Integer albumId = StringUtil.toint(request.getParameter("albumId"));
		try
		{
			String json;
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);

			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				// 根据相册Id 查询对应的相片 用于七牛上删除对应的相片
				List<Photo> photoList = photoService.getPhotoDetailByAlbumId(albumId);

				classAlbumService.deleteAlbumByAlbumId(albumId);

				if (photoList.size() > 0)
				{
					FileUploadUtil fuu = new FileUploadUtil();
					for (Photo photo : photoList)
					{
						String photoPath = photo.getPhotoPath();
						fuu.simpleDelete(Cons.QINIU_BUCKETNAME_CLASSALBUM, photoPath.substring(Cons.QINIU_URL_CLASSALBUM.length()));
					}
				}

				rhr.ResponseCode = 0;
				rhr.ResponseResult = "删除相册成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("删除相册失败：" + e.getMessage());
		}
		return null;

	}

	/**
	 * 【手机端】根据photoId删除相片和下面的所有图片
	 */
	@RequestMapping(value = "/deletePhotoByPhotoIdAndApiKey.do")
	public String deletePhotoByPhotoIdAndApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		Integer photoId = StringUtil.toint(request.getParameter("photoId"));
		try
		{
			String json;
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);

			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				// 根据相片id查询对应记录Id下面的所有照片 如果只有一张则对应的记录也删除
				List<Photo> photoList = photoService.getPhotoList(photoId);
				if (photoList.size() == 1)
				{
					Integer recordsId = photoList.get(0).getRecordsId();
					// 根据recordsId 删除对应的上传记录及对应的评论
					uploadRecordsService.deleteRecordsByRecordsId(recordsId);
				}

				// 根据photoId查询对应的相片 用于七牛上删除对应的相片
				Photo photo = photoService.getPhotoByPhotoId(photoId);

				// 删除照片
				photoService.deletePhotoByPhotoId(photoId);

				if (photo != null)
				{
					FileUploadUtil fuu = new FileUploadUtil();
					String photoPath = photo.getPhotoPath();
					fuu.simpleDelete(Cons.QINIU_BUCKETNAME_CLASSALBUM, photoPath.substring(Cons.QINIU_URL_CLASSALBUM.length()));
				}

				rhr.ResponseCode = 0;
				rhr.ResponseResult = "删除照片成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("删除照片失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【手机端】获取所有班级
	 * 根据apiKey和schoolId
	 */
	@RequestMapping(value = "/getClazzListByApiKey.do")
	public String getClazzListByApiKey(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<List<Clazz>> rhr = new RichHttpResponse<List<Clazz>>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		try
		{
			String json;
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				List<Clazz> clazzList = new ArrayList<Clazz>();
				List<Role> roleList = loginUser.getRoleList();
				for (Role role : roleList)
				{
					List<Grade> gradeList = gradeService.getGradeListByUserIdAndRoleCode(loginUser.getUserId(), role.getRoleCode());
					for (Grade grade : gradeList)
					{
						List<Clazz> sub_clazzList = clazzService.getClazzListByGradeIdAndUserIdAndRoleCode(grade.getId(), role.getRoleCode(), loginUser.getUserId());
						clazzList.addAll(sub_clazzList);
					}
				}
				// 去除重复clazz
				List<Clazz> clazzList_norepeat = new ArrayList<Clazz>();
				Map<Integer, Clazz> clazzMap = new HashMap<Integer, Clazz>();
				/*
				 * 玛利娅·蒙特梭利国际幼儿园 schoolId:3201150082 该学校获取班级相册的时候，只要班级名称，不要包含年级
				 */
				for (Clazz clazz : clazzList)
				{
					clazz.setClazzId(clazz.getId());
					if ("3201150082".equals(schoolId))
					{
						clazz.setFullClazzName(clazz.getClazzName());
					}
					if (!clazzMap.containsKey(clazz.getId()))
					{
						clazzMap.put(clazz.getId(), clazz);
						clazzList_norepeat.add(clazz);
					}
				}

				rhr.ResponseCode = 0;
				rhr.ResponseObject = clazzList_norepeat;
				rhr.ResponseResult = "获取成功";
				json = GsonHelper.toJsonWithAnnotation(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【手机端】获取上传token
	 */
	@RequestMapping(value = "/getUpToken.do")
	public String getUpToken(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<String> rhr = new RichHttpResponse<String>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		try
		{
			String json;
			// 这边是利用apikey 进行模拟登录
			User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				FileUploadUtil fileUploadUtil = new FileUploadUtil();
				String token = fileUploadUtil.getSimpleUpToken(Cons.QINIU_BUCKETNAME_CLASSALBUM);
				rhr.ResponseCode = 0;
				rhr.ResponseObject = token;
				rhr.ResponseResult = "获取成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = -1;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("获取七牛token失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【JS端】获取上传token
	 */
	@RequestMapping(value = "/getUpTokenInJS.do")
	public String getUpTokenInJS(HttpServletRequest request, HttpSession session, ModelMap modelMap, HttpServletResponse response)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		try
		{
			FileUploadUtil fileUploadUtil = new FileUploadUtil();
			String token = fileUploadUtil.getSimpleUpToken(Cons.QINIU_BUCKETNAME_CLASSALBUM);
			String json = GsonHelper.toJson(token);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("获取七牛token失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 【手机端】收藏照片到个人相册（家长自己小孩、其他自己的个人相册）
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/collectPhotoByApiKey.do")
	public String collectPhotoByApiKey(HttpServletRequest request, HttpServletResponse response, HttpSession session)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		String photoDesc = request.getParameter("photoDesc");
		String qiniuPhotoUrlJson = request.getParameter("qiniuPhotoUrlJson");
		String json;

		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		try
		{
			if (loginUser != null)
			{
				session.setAttribute("user", loginUser);
				DBContextHolder.setDBType(loginUser.getSchoolId());

				List<Role> roleList = loginUser.getRoleList();
				String roleCode = "";
				for (Role role : roleList)
				{
					roleCode = role.getRoleCode();
					if (roleCode.equals("parent"))
					{
						break;
					}
				}

				List<User> userList = new ArrayList<User>();
				// 如果是家长则存入其对应的小孩相册里 不是的话就存入自己的个人相册
				if (roleCode.equals("parent"))
				{
					userList = commonService.getAllChildrenByUserId(loginUser.getUserId());
				} else
				{
					userList.add(loginUser);
				}

				// 如果是家长 但是系统里没有其对应的小孩怎存入自己的个人相册
				if (CollectionUtils.isEmpty(userList))
				{
					userList.add(loginUser);
				}

				// 解析七牛图片地址list
				List<QiniuPhoto> qiniuPhotoList = GsonHelper.fromJson(qiniuPhotoUrlJson, new TypeToken<ArrayList<QiniuPhoto>>()
				{
				}.getType());
				if (qiniuPhotoList != null && qiniuPhotoList.size() > 0)
				{
					for (QiniuPhoto qiniuPhoto : qiniuPhotoList)
					{
						for (User user : userList)
						{
							Integer userId = user.getUserId();
							String userName = user.getRealName();
							Map<String, Object> paramMap = new HashMap<String, Object>();
							paramMap.put("userId", userId);
							paramMap.put("albumName", "宝宝相册");
							PersonAlbum album = personAlbumService.getPersonAlbumByUserIdAndAlbumName(paramMap);
							if (album == null)
							{
								album = new PersonAlbum();
								album.setAlbumName("宝宝相册");
								album.setUserId(StringUtil.toint(userId));
								album.setUserName(userName);
								album.setCreateTime(TimeUtil.getInstance().date());
								album.setUpdateTime(TimeUtil.getInstance().now());
								Integer aId = personAlbumService.addPersonAlbum(album);
								album.setAlbumId(aId);
							}
							PersonPhoto personPhoto = new PersonPhoto();
							personPhoto.setAlbumId(album.getAlbumId());
							personPhoto.setUserId(StringUtil.toint(userId));
							personPhoto.setUserName(userName);
							personPhoto.setPhotoPath(qiniuPhoto.photoUrl);
							personPhoto.setPhotoName(qiniuPhoto.photoName);
							personPhoto.setPhotoDesc(photoDesc);
							if(null == qiniuPhoto.mediaType){
								personPhoto.setMediaType(1);
							}else{
								personPhoto.setMediaType(qiniuPhoto.mediaType);
							}
							personPhoto.setMediaSecordUrl(qiniuPhoto.mediaSecordUrl);
							personAlbumService.addPersonPhoto(personPhoto);
						}
					}
				}
				rhr.ResponseCode = 0;
				rhr.ResponseResult = "收藏图片成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}

			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("手机端班级相册收藏图片失败：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 手机端删除上传记录
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/deleteUploadRecordsByApiKey.do")
	public String deleteUploadRecords(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap)
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		RichHttpResponse<ClassPhotoAlbum> rhr = new RichHttpResponse<ClassPhotoAlbum>();
		String apiKey = request.getParameter("apiKey");
		String schoolId = request.getParameter("schoolId");
		String recordsId = request.getParameter("recordsId");
		String json;

		// 这边是利用apikey 进行模拟登录
		User loginUser = userService.getUserByApiKeyAndSchoolId(apiKey, schoolId);
		try
		{
			if (loginUser != null)
			{
				DBContextHolder.setDBType(loginUser.getSchoolId());
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("recordsId", recordsId);
				//根据recordsId查询对应的照片   用于删除七牛上对应的照片
				List<Photo> photoList = photoService.getPhotoListByRecordsId(paramMap);
				
				uploadRecordsService.deleteRecordsByRecordsId(StringUtil.toint(recordsId));
				photoService.deletePhotoByRecordsId(StringUtil.toint(recordsId));
				if(!CollectionUtils.isEmpty(photoList))
				{
					FileUploadUtil fuu = new FileUploadUtil();
					for(Photo photo : photoList)
					{
						String photoPath = photo.getPhotoPath();
						fuu.simpleDelete(Cons.QINIU_BUCKETNAME_CLASSALBUM, photoPath.substring(Cons.QINIU_URL_CLASSALBUM.length()));
					}
				}
				
				rhr.ResponseCode = 0;
				rhr.ResponseResult = "删除上传记录成功";
				json = GsonHelper.toJson(rhr);
			} else
			{
				rhr.ResponseCode = Cons.LOGIN_APIKEY_ERROR_CODE;
				rhr.ResponseObject = null;
				rhr.ResponseResult = Cons.LOGIN_APIKEY_INVALID;
				json = GsonHelper.toJson(rhr);
			}

			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e)
		{
			log.error("deleteUploadRecords，json转换失败：" + e.getMessage());
		} catch (Exception e)
		{
			log.error("deleteUploadRecords，手机端删除上传记录失败：" + e.getMessage());
		}
		return null;
	}
	
}