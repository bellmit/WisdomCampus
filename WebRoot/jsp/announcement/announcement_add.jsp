<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<head>
        <meta charset="utf-8" />
        <title>发布公告</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="width=device-width, initial-scale=1" name="viewport" />
         <!--公共css开始-->
		<%@ include file="/public_module/public_css_new.jsp"%>
	 	<!--公共css结束--> 
        </head>
    <body class="page-header-fixed page-sidebar-closed-hide-logo page-content-white page-sidebar-fixed">
    	<input class="themes" type="hidden" value="<%=basePath%>">
        <!-- 公共顶部开始-->
        <%@ include file="/public_module/public_header.jsp"%>    
        <!--公共顶部结束-->
        <div class="clearfix"> </div>
        <!-- 内容页开始 -->
        <div class="page-container">
            <!--主菜单开始-->
            <%@include file="/public_module/public_menu.jsp"%>
            <!--主菜单结束-->
            <div class="page-content-wrapper">
                <div class="page-content m_overflow_hidden m_page_content">
                	
                	<div class="col-md-12 col-sm-12 m_page_con">
                    <div class="page-bar m_margin_0_0_0_0">
                        <ul class="page-breadcrumb">
                            <li><a href="<%=basePath %>user/enterMain.do">首页</a><i class="fa fa-circle"></i></li>
                            <li><a href="<%=basePath %>announcement/toAnnouncementList.do">信息中心</a><i class="fa fa-circle"></i></li>
                            <li><span>发布公告</span></li>
                        </ul>
                    </div>
                    <div class="row">
                        <div class="col-md-12 col-sm-12">
                            <div class="portlet light portlet-fit portlet-form bordered m_margin_15_auto_0" >
                                <div class="portlet-title">
                                    <div class="caption">
                                        <i class=" icon-layers font-green"></i>
                                        <span class="caption-subject font-green sbold uppercase">发布公告</span>
                                    </div>
                                </div>
                                <div class="portlet-body">
                                    <form action="<%=basePath%>announcement/doAddAnnouncement.do" id="addAnnouncement" name="addAnnouncement" class="form-horizontal" method="post">
                                        <div class="form-body">
                                            <div class="form-group">
                                                <label class="control-label col-md-3">发布用户：</label>
                                                <div class="col-md-9 col-lg-4">
                                                    <input class="form-control" type="text" value="${user.realName}" disabled />
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-md-3">发布类型：</label>
                                                <div class="col-md-9 col-lg-4">
                                                    <select class="form-control" name="announcementSelect"  tabindex="1" id="announcementSelect">
														<c:forEach items="${permissionList }" var="permission">
															<c:if test="${(permission.permissionCode eq 'schoolAnnouncementAdd') || (permission.permissionCode eq 'admin') }">
																<option value="1">学校公告</option>
															</c:if>
															<c:if test="${(permission.permissionCode eq 'departmentAnnouncementAdd') || (permission.permissionCode eq 'admin')}">
																<option value="2">部门公告</option>
															</c:if>
															<c:if test="${(permission.permissionCode eq 'clazzAnnouncementAdd') || (permission.permissionCode eq 'admin')}">
																<option value="3">班级公告</option>
															</c:if>
															<c:if test="${(permission.permissionCode eq 'systemAnnouncementAdd') || (permission.permissionCode eq 'admin')}">
																<option value="4">系统公告</option>
															</c:if>	
														</c:forEach>
			                                        </select>
													<span class="help-inline" style="color: #f00; font-size: 12px;">此处为校园公告（非短信），只有登录用户才能看到</span>	
                                                </div>
                                            </div>
                                            <div class="form-group" id="opeObjectOption">
                                                <label class="control-label col-md-3">操作对象：</label>
                                                <div class="col-md-9 col-lg-4">
                                                    <select class="form-control" name="opeObject"  tabindex="1" id="opeObject">
	                                        		</select>	 
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-md-3">公告内容：</label>
                                                <div class="col-md-9 col-lg-7">
                                                    <textarea style="resize:none"  class="form-control" rows="10" name="content" id="content"></textarea>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-actions">
                                            <div class="row">
                                                <div class="col-md-offset-3 col-md-9">
                                                	<input type="hidden" value="${userId}" name="userId" id="userId"/>
													<input type="hidden" value="${roleCode}" name="roleCode" id="roleCode"/>
                                                    <button type="button" class="btn green" onclick="fabu();">发布</button>
                                                    <button type="button" class="btn" onclick="history.go(-1);">返回</button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                                </div>
                        </div>
                    </div>
                    </div>
                </div>
            </div>
            <!-- 亦信聊天开始 -->
        	<%@ include file="/public_module/public_QQ.jsp" %>
            <!-- 亦信聊天结束 -->
        </div>
        <!-- 内容页结束 -->
        <!-- 底部开始 -->
        <%@ include file="/public_module/public_footer.jsp" %>
        <!-- 底部结束 -->
    </body>
        <!-- 公共js开始 -->
        <%@ include file="/public_module/public_js.jsp" %>
        <!-- 公共js结束 -->
	<script>

		jQuery(document).ready(function(){
			loadSubMenu("informationPlatform"); 
			//选取当前菜单位置
			setActive("informationPlatform","announcementShow");  
			$("#announcementSelect").val(1);
			loadOpeObject();
			$("#opeObjectOption").hide();
			$("#announcementSelect").change(function(){
				var announcementSelect=$("#announcementSelect").val();
				if(announcementSelect==1 || announcementSelect==4){
					$("#opeObjectOption").hide();
				}else{
					$("#opeObjectOption").show();	
				}
				loadOpeObject();
			});
		});
		//发布
		function fabu(){
			var content=$.trim($("#content").val());
			var announcementSelect=$.trim($("#announcementSelect").val());
			var opeObject=$.trim($("#opeObject").val());
			var userId=$.trim($("#userId").val());
			if(content==""){
				layer.tips("请输入公告内容","#content");
				return false;
			}else if(content.length>10000){
				layer.tips("内容最多1000个字","#content");
				return false;
			}
			if(announcementSelect==2 || announcementSelect==3){
				if(typeof(opeObject)=="undefined" || opeObject==""){
					layer.tips("请选择操作对象","#opeObject");
					return false;
				}
			}
			var indexlayer = layer.msg('正在发布公告,请稍候。。。',{icon: 16,time:0,shade:0.3});
			$.ajax({
				type: "post",
				url: "<%=basePath%>announcement/doAddAnnouncement.do",
				data:{"content":content,"announcementSelect":announcementSelect,"opeObject":opeObject,"userId":userId},
				error:function(){},
				dataType:"json",
				success:function(data){
					if(data=="1"){
					  layer.msg("发布成功!",{icon:1,time:1000});
					  location.href="<%=basePath%>announcement/toAnnouncementList.do";
					}else{
					  layer.msg("发布失败!",{icon:2,time:1000});
					  layer.close(indexlayer);
					}
				}
			});
		}
		//建立一个可存取到该file的url
		function getObjectURL(file) {
			var url = null ; 
			if (window.createObjectURL!=undefined) { // basic
				url = window.createObjectURL(file) ;
			} else if (window.URL!=undefined) { // mozilla(firefox)
				url = window.URL.createObjectURL(file) ;
			} else if (window.webkitURL!=undefined) { // webkit or chrome
				url = window.webkitURL.createObjectURL(file) ;
			}
			return url ;
		}

		function loadOpeObject(){
			var selectVal = $("#announcementSelect").val();
			var roleCode = $("#roleCode").val();
			var userId = $("#userId").val();
			$.ajax({
				type: "post",
				url: "<%=basePath%>announcement/loadOpeObjectList.do",
				data:{
					selectVal : selectVal,
					roleCode : roleCode,
					userId : userId
					},
				success: function(data){
					if(selectVal == 1){
						$("#opeObjectDiv").css({"display":"none"});
					}else if(selectVal == 2){
						
						//部门公告
						$("#opeObjectDiv").css({"display":"block"});
						var list = eval("(" + data + ")");
						var appendHtml = '';
						$.each(list, function(n, value) { 
	           				appendHtml += '<option value="'+value.id+'">'+value.departmentName+'</option>';
	          			});
						$("#opeObject").html(appendHtml);
					}else if(selectVal == 3){
						//班级公告
						$("#opeObjectDiv").css({"display":"block"});
						var list = eval("(" + data + ")");
						var appendHtml = '';
						$.each(list, function(n, value) { 
							if(value.type == 0){
								appendHtml += '<option value="'+value.id+'">'+value.gradeName+value.clazzName+'</option>';
							}else if(value.type == 1){
								appendHtml += '<option value="'+value.id+'">'+value.gradeName+value.clazzName+'</option>';
							}else if(value.type == 2){
								appendHtml += '<option value="'+value.id+'">'+value.gradeName+value.clazzName+'</option>';
							}else if(value.type == 3){
								appendHtml += '<option value="'+value.id+'">高中'+value.gradeName+value.clazzName+'</option>';
							}
	           				
	          			});
						$("#opeObject").html(appendHtml);
					}else if(selectVal == 4){
						$("#opeObjectDiv").css({"display":"none"});
					}
				
				},
			});

		}
	</script>
</html>