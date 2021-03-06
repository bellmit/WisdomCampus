<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]--><!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]--><!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<head>
    <meta charset="utf-8" />
    <title>视频管理</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
    <!--公共css开始-->
	<%@ include file="/public_module/public_css_new.jsp"%>
 	<!--公共css结束-->        
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
	                        <li><a href="">视频直播</a><i class="fa fa-circle"></i></li>
	                        <li><span>视频管理</span></li>
	                    </ul>
	                </div>
	                <h3 class="page-title">视频管理</h3>
	                <div class="row">
	                    <div class="col-md-12 col-sm-12">
	                    	<div class="m_margin_0_0_15_0">
	                    		<!-- 角色切换开始 -->
	                    		<ul class="nav nav-tabs"  id="userTab">
	                                <c:forEach items="${sessionScope.user.roleList}" var="roleList">
										<li class="roleLi" id="${roleList.roleCode}">
											<a attr1="${sessionScope.user.userId}" attr2="${roleList.roleCode}" attr3="${sessionScope.user.permissionList}">${sessionScope.user.realName}(${roleList.roleName})</a>
										</li>
									</c:forEach>
	                            </ul>
	                    		<!-- 角色切换结束 -->
	                        </div>
	                         <!-- 添加功能菜单开始 -->
	                        <div class="portlet light form-fit m_margin_0_0_15_0">
	                        	<r:right rightCode="cameraManagerEditor">
									<a href="#" class="btn btn-default" id="addCamera">添加设备</a>
									<a href="<%=basePath%>camera/toClazzCameraConfig.do" class="btn btn-default" id="addCamera">班级摄像头</a>
									<a href="<%=basePath%>camera/toUserCameraConfig.do" class="btn btn-default" id="addCamera">人员摄像头</a>
								</r:right>
	                        </div>
	                        <!-- 添加功能菜单结束 -->
	                        <!-- 页面搜索开始 -->
	                        <div class="portlet light form-fit bordered search_box">
	                       	<form action="#" class="form-horizontal">
	                         <div class="row m_margin_10_auto">
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		<span class="m_span">搜索内容：</span>
	                         		<input type="text"  class="m_input form-control" id="queryContent" placeholder="设备编码,地点名称..." onkeyup="getCameraData();"/>
	                         	</div>
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		<button type="button" class="btn green"   id="search">搜索</button>
	                         	</div>
	                         </div>
	                         </form>
	                        </div>
	                       	<div class="portlet box green m_margin_15_auto_0">
	                            <div class="portlet-title">
	                                <div class="caption">
	                                    <i class="fa fa-list"></i>视频管理</div>
	                            </div>
	                            <div class="portlet-body flip-scroll">
	                                <table class="table table-bordered table-hover">
	                                    <thead class="flip-content">
	                                        <tr>
	                                            <th class="m_width_10">地点名称</th>
												<th class="m_width_10 hidden-sm hidden-md">摄像头编码</th>
												<th class="m_width_10">摄像头名称</th>
												<th class="m_width_5">类型</th>
												<th class="m_width_10">开始日期</th>
												<th class="m_width_10">结束日期</th>
												<th class="m_width_10  hidden-sm hidden-md">使用时间段</th>
												<th class="m_width_20 hidden-sm hidden-md hidden-lg">创建时间</th>
												<th class="m_width_15">操作</th>
	                                        </tr>
	                                    </thead>
	                                    <tbody id="tbody">
	                                      
	                                    </tbody>
	                                </table>
	                                <input type="hidden" name="placeId" id="placeId" value="${placeId}">
	                            </div>
	                        </div>
	                       <!-- 列表展现开始 -->
	                        <!-- 分页开始 -->
	                        <%@include file="/public_module/public_page.jsp" %>
	                        <!-- 分页结束 -->
	                    </div>
	                </div>
	                </div>
	                <!--页面内容结束 -->
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
	<script src="<%=basePath%>assets/global/plugins/layer/layer.js" type="text/javascript"></script>
	<script>

		jQuery(document).ready(function() {    
			loadSubMenu("liveCameraManager"); 
			//选取当前菜单位置
			setActive("liveCameraManager","cameraManager");  
			//页面初始化时候的数据加载
			var userId = $("#userTab li.active").find('a').attr("attr1");
			var roleCode = $("#userTab li.active").find('a').attr("attr2");

			//点击tab页加载
			$("#userTab li a").click(function(){
				$("#userTab li").removeAttr("class");
				$(this).parent().addClass("active");
				//用户ID
				var userId = $(this).attr("attr1");
				var roleCode = $(this).attr("attr2");
				loadCameraList(null,roleCode,null);
			});
			
			//点击增加设备页面
			$("#addCamera").click(function(){
				window.location.href='<%=basePath%>camera/toAddCamera.do';
			});

			loadCameraList(null,roleCode,null);
			
			//条件查询
			$("#search").click(function(){
				//查询内容
				var queryContent = $("#queryContent").val();
				var roleCode = $("#userTab li.active").find('a').attr("attr2");
				loadCameraList(queryContent,roleCode,null);
			});

			$("#queryContent").keypress(function(event){
				if(event.which == "13")    
            	{
					$("#search").click();
					return false;
            	}
			});	

		});

		function getCameraData(){
			var queryContent = $("#queryContent").val();
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			loadCameraList(queryContent,roleCode,null);
		}
		//加载摄像头页面
		function loadCameraList(queryContent,roleCode,cPage)
		{
			var body = $("#body");
			var placeId = $("#placeId").val();
			App.blockUI(body);
			$.ajax({
				type: "post",
				url: "<%=basePath%>camera/getCameraList.do",
				data:{
						queryContent : queryContent,
						placeId : placeId,
						roleCode : roleCode,
						cPage : cPage
				},
				success: function(data){
					var page = eval("(" + data + ")");
					$("#currentPage").html(page.currentPage);
					$("#totalPage").html(page.totalPage);
					var appendHtml = '';
					$.each(page.list, function(n, value) {  
						
						appendHtml += '<tr>';
					
            			appendHtml += '<td>' + value.placeName + '</td>';
            			
            			appendHtml += '<td class=" hidden-sm hidden-md">' + value.cameraCode + '</td>';

            			if(typeof(value.cameraName) == "undefined"){
            				appendHtml += '<td></td>';
                		}else{
                			appendHtml += '<td>' + value.cameraName + '</td>';
                    	}
                    	
            			if(value.type == 0){
            				appendHtml += '<td>公用</td>';
                		}else if(value.type == 1){
                			appendHtml += '<td>私用</td>';
                    	}else if(value.type == 2)
                        {
                    		appendHtml += '<td>全校</td>';
                        }else
                        {
                    		appendHtml += '<td>全校</td>';
                        }

            			var startDay = value.startDay;
            			var endDay = value.endDay;
                    	
                    	if(startDay == undefined)
                       	{
                    		startDay = "";
                       	}
                    	if(endDay == undefined)
                       	{
                    		endDay = "";
                       	}
                    	appendHtml += '<td>' + startDay + '</td>';
                    	appendHtml += '<td>' + endDay + '</td>';
                    	
                    	var startTime = value.startTime;
            			var endTime = value.endTime;
            			if((startTime == undefined || startTime == "") && (endTime == undefined || endTime == "") )
                       	{
                    		appendHtml += '<td class=" hidden-sm hidden-md">—</td>';
                       	}
                    	else if((startTime != undefined || startTime != "") && (endTime == undefined || endTime == "") )
                       	{
                    		appendHtml += '<td class=" hidden-sm hidden-md">' + startTime;
                       	}
                    	else if((startTime == undefined || startTime == "") && (endTime != undefined || endTime != "") )
                       	{
                    		appendHtml += '<td class=" hidden-sm hidden-md">' + startTime;
                       	}
                    	else
                       	{
                    		appendHtml += '<td class=" hidden-sm hidden-md">' + startTime + '—';
                       	}
            			if(endTime == undefined)
                       	{
            				endTime = "";
                       	}

            			appendHtml += endTime + '</td>';
            			
            			appendHtml += '<td class=" hidden-sm hidden-md hidden-lg">' + value.createTime + '</td>';
            			
            			appendHtml += '<td>';
            			
            			appendHtml += '<r:right rightCode="cameraManagerEditor">';
            			appendHtml += ' <a href="javascript:;" onclick="detailCamera(' + value.cameraId + ')">详情</a>';
            			appendHtml += ' <a href="javascript:;" onclick="modifyCamera(' + value.cameraId + ')">修改</a>';
            			appendHtml += '  <a href="javascript:;" onclick="deleteCamera(' + value.cameraId + ')">删除</a>';
            			appendHtml += '</r:right>';	
            			appendHtml += '</td>';
            			
           				appendHtml += '</tr>';
          			}); 
          			$("#tbody").html(appendHtml);
          			App.unblockUI(body);
				},
			});
		}
		//详情
         function detailCamera(cameraId){
        	 standardPost('<%=basePath%>camera/detailCamera.do',{cameraId:cameraId});
         }
		//点击编辑
		function modifyCamera(cameraId)
		{
			standardPost('<%=basePath%>camera/modifyCamera.do',{cameraId:cameraId});
		}
		
		//点击详情
		function cameraDetail(cameraId)
		{
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			standardPost('<%=basePath%>camera/toCameraDetail.do',{cameraId:cameraId,roleCode:roleCode});
		}

	  //删除摄像头
	  function deleteCamera(cameraId){
		 var l_=layer.confirm('确定删除？一旦删除数据不可恢复！', {
			  btn: ['确定','取消'] //按钮
			}, function(){
				layer.close(l_);
				var d_ = layer.msg('正在删除数据,请稍候。。。',{icon: 16,time:0,shade:0.3});
				 $.ajax({
			           type:"post",
			           url: "<%=basePath%>camera/deleteCamera.do",
					   data:{
							cameraId : cameraId
					   },
			           error:function(){ 
				           layer.close(l_);
				           layer.close(d_);
				       },
			           success:function(data){
			        	   var date = eval("(" + data + ")");
							if(date)
							{
								$('#myModal').modal('hide');
								var cPage = $("#currentPage").html();
								var cPageInt = parseInt(cPage);
								var roleCode = $("ul li.active").find('a').attr("attr2");
								loadCameraList(null,roleCode,cPageInt)
				        	    layer.close(l_);
				        	    layer.close(d_);
							}
			        	   
			           }
			    }); 
			}, function(){
			  layer.close(l_);
			});
		
		}
		
		//分页相关
		function jumpPage(type)
		{
			var cPage = $("#currentPage").html();
			var totalPage = $("#totalPage").html();

			var cPageInt = parseInt(cPage);
			var totalPageInt = parseInt(totalPage);
						
			var newCPageInt = returnCPageInt(type,cPageInt,totalPageInt);
			if(newCPageInt < 0){
				return;
			}else{
				cPageInt = newCPageInt;
			}

			var queryContent = $("#queryContent").val();
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			loadCameraList(queryContent,roleCode,cPageInt)
			
			$("#currentPage").html(cPageInt);
		}

	</script>
</html>