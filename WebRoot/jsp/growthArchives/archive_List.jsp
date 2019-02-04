<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]--><!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]--><!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<head>
    <meta charset="utf-8" />
    <title>成长档案</title>
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
	                        <li><a href="">电子动力成长档案</a><i class="fa fa-circle"></i></li>
	                        <li><span>成长档案</span></li>
	                    </ul>
	                </div>
	                <h3 class="page-title">成长档案</h3>
	                <div class="row">
	                    <div class="col-md-12 col-sm-12">
	                    	<div class="m_margin_0_0_15_0">
	                    		<!-- 角色切换开始 -->
	                    		<ul class="nav nav-tabs"  id="userTab">
	                                <c:forEach items="${sessionScope.user.roleList}" var="roleList">
										<c:if test="${roleList.roleCode eq 'admin' || roleList.roleCode eq 'chairman' || roleList.roleCode eq 'departManager' || roleList.roleCode eq 'president' || roleList.roleCode eq 'teacher' || roleList.roleCode eq 'parent'}">
											<li class="roleLi">
												<a attr1="${sessionScope.user.userId}" attr2="${roleList.roleCode}" >${sessionScope.user.realName}(${roleList.roleName})
												</a>
											</li>
										</c:if>
									</c:forEach>
	                            </ul>
	                    		<!-- 角色切换结束 -->
	                        </div>
	                        <!-- 页面搜索开始 -->
	                        <div class="portlet light form-fit bordered search_box">
	                       	<form action="#" class="form-horizontal">
	                         <div class="row m_margin_10_auto">
	
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		<span class="m_span">选择年级：</span>
	                         		<select  id="grade" class="form-control m_select" name="grade" onchange="loadClass()"></select>
	                         	</div>
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		<span class="m_span">选择班级：</span>
	                         		<select  id="clazz" class="form-control m_input" name="clazz"></select>
	                         	</div>
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		<span class="m_span">搜索内容：</span>
	                         		<input type="text" class="m_input form-control" name="queryContent" id="queryContent"/>
	                         	</div>
	                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
	                         		 <button type="submit" class="btn green" id="search">查找</button>
	                         	</div>
	                         </div>
	                         </form>
	                        </div>
	                       	<div class="portlet box green m_margin_15_auto_0">
	                            <div class="portlet-title">
	                                <div class="caption">
	                                    <i class="fa fa-list"></i>成长档案</div>
	                            </div>
	                            <div class="portlet-body flip-scroll">
	                                <table class="table table-bordered table-hover">
	                                    <thead class="flip-content">
	                                        <tr>
	                                           <th>名称</th>
												<th>年级</th>
												<th>班级</th>
												<th>家长</th>
												<th>班主任</th>
												<th>模板名称</th>
												<th>更新时间</th>
												<th>操作</th>
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
	<script>

		jQuery(document).ready(function() {
			loadSubMenu("growthArchives"); 
			//选取当前菜单位置
			setActive("growthArchives","growth");    
			loadGradeList();
			var userId = $("#userId").val();
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			$("#search").on("click",function(){
				loadArchiveList(userId,roleCode,null);
			})
			loadArchiveList(userId,roleCode,null);
			//点击tab页加载
			$("#userTab li a").click(function(){
				$("#userTab li").removeAttr("class");
				$(this).parent().addClass("active");
				//用户ID
				var userId = $(this).attr("attr1");
				var roleCode = $(this).attr("attr2");
				loadArchiveList(userId,roleCode, null);
			});
		});
		
		//加载年级信息
		function loadGradeList()
		{
			$.ajax({
				type: "post",
				url: "<%=basePath%>baseData/getGradeList.do",
				success: function(data){
					var gradeList = eval("(" + data + ")");

					//年级select页面html
					var gradeSelectHtml = '<option>请选择</option>';

					$.each(gradeList, function(n, value) {  
						//select页面
						gradeSelectHtml += '<option value="'+value.id+'">' + value.name + '</option>';
						
	          		});

          			$("#grade").html(gradeSelectHtml);
          			
				},
			});
		} 
		function loadClass(){
			  $.ajax({
					type: "post",
					url: "<%=basePath%>baseData/getClazzListByGradeId4App.do",
					data:{
						gradeId : $("#grade").val()
					},
					success:function(data){
						var clazzList = eval("(" + data + ")");
						
						//年级select页面html
						var clazzSelectHtml = '<option value="">请选择</option>';

						$.each(clazzList, function(n, value) {
							//select页面
							clazzSelectHtml += '<option value="'+value.id+'">' + value.clazzName + '</option>';
						
		          		});

	        			$("#clazz").html(clazzSelectHtml);
	        			
					},
				});
		  }
		//加载
		function loadArchiveList( userId, roleId, cPage){
			var body = $("#body");
			App.blockUI(body);
			
			$.ajax({
				type: "post",
				url: "<%=basePath%>growth/getGrowthArchiveList.do",
				data:{
				    	clazzId : $("#clazz").val(),
					    queryContent : $("#queryContent").val(),
						userId : userId,
						roleId : roleId,
						cPage : cPage
					},
				success: function(data){
					var data = $.parseJSON(data);
					var appendHtml = '';
					$("#currentPage").html(data.currentPage);
					$("#totalPage").html(data.totalPage);
					$.each(data.list,function(n,value){
						appendHtml += '<tr>';
						appendHtml += '<td>' + value.realName + '</td>';
						appendHtml += '<td>' + value.gradeName + '</td>';
						appendHtml += '<td>' + value.clazzName + '</td>';
						if (typeof(value.parentName)=="undefined"){
							appendHtml += '<td></td>';
						}else{
							appendHtml += '<td>' + value.parentName + '</td>';
						}
						if (typeof(value.leaderName)=="undefined"){
							appendHtml += '<td></td>';
						}else{
							appendHtml += '<td>' + value.leaderName + '</td>';
						}
						if (typeof(value.templateName)=="undefined"){
							appendHtml += '<td></td>';
						}else{
							appendHtml += '<td>' + value.templateName + '</td>';
						}
						
						var time = value.updateTime;
						if (typeof(time)=="undefined"){
							time = "";
						}else{
							time = time.substr(0, time.length-2)
						}
	           			appendHtml += '<td>' + time+ '</td>';
						appendHtml += '<td><a href="javascript:;" class="btn btn-sm green" onclick="showDetail(\''+ value.userId +'\',\''+value.realName+'\',\''+value.gradeName+'\',\''+value.clazzName+'\',\''+value.parentName+'\',\''+value.leaderName+'\',\''+value.templateName+'\')" >详情</a> ';
						if (!value.isExist){
							appendHtml += '<a href="javascript:;" class="btn btn-sm blue" onclick="addArchive('+ value.userId +')" >新增</a>';
						}
						
						appendHtml +='</td>';
						appendHtml += '</tr>';
					});
          			$("#tbody").html(appendHtml);
          			//解锁UI
          			App.unblockUI(body);
          			
				},
			});
		}
		//查看
		function showDetail(userId,realName,gradeName,clazzName,parentName,leaderName,templateName){
			
			standardPost('<%=basePath%>growth/toGrowthDetail.do',{userId:userId,leaderName:'+encodeURI(encodeURI(leaderName))+',realName:'+encodeURI(encodeURI(realName))+'},gradeName:' + encodeURI(encodeURI(gradeName))+',clazzName:' + encodeURI(encodeURI(clazzName))+',parentName=' + encodeURI(encodeURI(parentName)));
			}
		//新增
		function addArchive(userId){
			standardPost('<%=basePath%>growth/toAddGrowth.do',{userId:userId});
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
				//用户ID
				var userId = $("#userId").val();
				//角色ID
				var roleCode = $("ul li.active").find('a').attr("attr2");

				
				loadArchiveList(userId, roleCode, cPageInt);
			
			   $("#currentPage").html(cPageInt);
		}
	</script>
</html>