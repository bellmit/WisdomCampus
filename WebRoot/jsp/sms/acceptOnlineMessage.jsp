<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]--><!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]--><!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<head>
    <meta charset="utf-8" />
    <title>智慧校园首页</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
    <!--公共css开始-->
	<%@ include file="/public_module/public_css_new.jsp"%>     
</head>
	<body class="page-header-fixed page-sidebar-closed-hide-logo page-content-white page-sidebar-fixed" id="body">
		<input class="themes" type="hidden" value="<%=ThemePath%>">
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
	            	<!-- 页面内容开始 -->
	            	<div class="col-md-12 col-sm-12 m_page_con">
	                <div class="page-bar m_margin_0_0_0_0">
	                    <ul class="page-breadcrumb">
	                        <li><a href="<%=basePath %>user/enterMain.do">首页</a><i class="fa fa-circle"></i></li>
	                        <li><span>在线留言</span></li>
	                    </ul>
	                </div>
	                <h3 class="page-title">在线留言</h3>
	                
	                <div class="row">
	                <div class="col-md-12 col-sm-12">
                    	<div class="m_margin_0_0_15_0">
                    		<!-- 角色切换开始 -->
                    		<ul class="nav nav-tabs"  id="userTab">
                                <c:forEach items="${sessionScope.user.roleList}" var="roleList">
									<li class="roleLi">
										<a attr1="${sessionScope.user.userId}" attr2="${roleList.roleCode}" attr3="${sessionScope.user.permissionList}" >${sessionScope.user.realName}(${roleList.roleName})
										</a>
									</li>
							</c:forEach>
                            </ul>
                    		<!-- 角色切换结束 -->
                        </div>
                        <!-- 添加功能菜单开始 -->
	                    <div class="portlet light form-fit m_margin_0_0_15_0">
	                    	<button class="btn btn-default" type="button" data-toggle="modal" href="#small">全部设为已读</button>
	                    </div>
	                    <!-- 添加功能菜单结束 -->
                       	<div class="portlet box green m_margin_15_auto_0">
                            <div class="portlet-title">
                                <div class="caption">
                                    <i class="fa fa-list"></i>在线留言</div>
                            </div>
                            <div class="portlet-body flip-scroll">
                                <table class="table table-bordered table-hover">
                                    <thead class="flip-content">
                                        <tr>
											<th class="m_width_10">发送人</th>
											<th class="m_width_15">发送时间</th>
											<th class="m_width_10">状态</th>
											<th class="m_width_65">内容</th>
                                        </tr>
                                    </thead>
                                    <tbody id="tbody">
                                    
                                    </tbody>
                                </table>
                            </div>
                        </div>
                       <!-- 列表展现开始 -->
                        <!-- 分页开始 -->
                        <%@include file="/public_module/public_page.jsp" %>
                        <!-- 分页结束 -->
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
		<script type="text/javascript">
		$(function(){
			//选取当前菜单位置
			setActive("index_","");  
			//页面初始化时候的数据加载
			App.init();
			//点击tab页加载
			$("#userTab li a").click(function(){
				$("#userTab li").removeAttr("class");
				$(this).parent().addClass("active");
				//用户ID
				var userId = $(this).attr("attr1");
				var roleCode = $(this).attr("attr2");
				loadOnlineMessageList(userId, roleCode,null,1);
			});
			$("#setAllRead").click(function(){
			var userId = $("#userTab li.active").find('a').attr("attr1");
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			location.href='<%=basePath%>online-message/toUpdateStatus.do?userId='+userId+'&roleCode='+roleCode;
		    });
			var userId = $("#userTab li.active").find('a').attr("attr1");
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			loadOnlineMessageList(userId, roleCode,null,1);
		});
		//初始化加载所有接受留言列表
		function loadOnlineMessageList(userId, roleCode,status,cPage)
		{
			var body = $("#body");
			App.blockUI(body);
			$.ajax({
				type: "post",
				url: "<%=basePath%>online-message/toAcceptAllOnlineMessage.do",
				data:{
						userId : userId,
						roleCode : roleCode,
						status : status,
						cPage : cPage
					},
				success: function(data){
					var page = eval("(" + data + ")");
					$("#currentPage").html(page.currentPage);
					$("#totalPage").html(page.totalPage);
					var appendHtml = '';
					$.each(page.list, function(n, value) {  
						//发送时间
           				var time = value.createTime;
						if (typeof(time)=="undefined"){
							time = "";
						}else{
							time = time.substr(0, time.length-2)
						}
						appendHtml += '<tr>';
           				appendHtml += '<td>' + value.senderName + '</td>';
           				appendHtml += '<td>' + time + '</td>';
           				if(value.status==1){
							appendHtml += '<td><a href="javascript:void(0)" class="mark name" onclick = "updateStatusById(' + value.id + '  )"> 标记为已读</a></td>';
						}else{
							appendHtml += '<td>已读</td>';	
						}
           				appendHtml += '<td>' + value.content + '</td>';
           				appendHtml += '</tr>';
          			}); 
          			$("#tbody").html(appendHtml);
          			//解锁UI
          			App.unblockUI(body);
          			if (null != cPage)
              		{
          				$("#currentPage").html(cPage);
              		}	
				},
			});
		}
		//用户更新留言状态
		function updateStatusById(id){
			var userId = $("#userTab li.active").find('a').attr("attr1");
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			location.href='<%=basePath%>online-message/toUpdateStatusById.do?id=' + id+'&userId='+userId+'&roleCode='+roleCode;
		}
	</script>
</html>