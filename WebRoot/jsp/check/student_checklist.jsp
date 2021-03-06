<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]--><!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]--><!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<head>
    <meta charset="utf-8" />
    <title>学生考勤</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
    <!--公共css开始-->
	<%@ include file="/public_module/public_css_new.jsp"%>
 	<!--公共css结束-->
 	<style type="text/css">
 	.xdsoft_datetimepicker{z-index:99999}
 	</style>        
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
	                        <li><a href="javascript:void(0);">师生考勤</a><i class="fa fa-circle"></i></li>
	                        <li><span>学生考勤</span></li>
	                    </ul>
	                </div>
	                <h3 class="page-title">学生考勤</h3>
	                <div class="row">
	                    <div class="col-md-12 col-sm-12">
	                    	<div class="m_margin_0_0_15_0">
	                    		<!-- 角色切换开始 -->
	                    		<ul class="nav nav-tabs"  id="userTab">
	                                <c:forEach items="${sessionScope.user.roleList}" var="roleList">
										<c:if test="${roleList.roleCode eq 'admin' || roleList.roleCode eq 'president' || roleList.roleCode eq 'parent' || roleList.roleCode eq 'student'|| roleList.roleCode eq 'classLeader'}">
											<li class="roleLi">
												<a attr1="${sessionScope.user.userId}" attr2="${roleList.roleCode}" >${sessionScope.user.realName}(${roleList.roleName})
												</a>
											</li>
										</c:if>
									</c:forEach>
									<input type="hidden" name="userId" id="userId" value="${sessionScope.user.userId}"/>
	                            </ul>
	                    		<!-- 角色切换结束 -->
	                        </div>
	                        <!-- 添加功能菜单开始 -->
	                        <div class="portlet light form-fit m_margin_0_0_15_0">
	                        	 <c:if test="${sessionScope.user.type != 2}">
							    	<button class="btn btn-default" type="button" data-toggle="modal" href="#small">导出</button>
								</c:if>
	                        </div>
	                        <!-- 添加功能菜单结束 -->
	                        <!-- 页面搜索开始 -->
	                        <div class="portlet light form-fit bordered search_box">
	                         <div class="row m_margin_10_auto">
		                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
		                         		<span class="m_span">选择班级：</span>
		                         		<select class="m_select form-control" id="gradeSelect" name="gradeId"></select>
		                         	</div>
		                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
			                         		<span class="m_span">开始时间：</span>
			                         		<input type="text" name="startTime" id="startTime"  class="m_input form-control"/>
			                        </div>
		                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
		                         		<span class="m_span">搜索内容：</span>
		                         		<input type="text" class="m_input form-control" id="queryContent"/>
															
		                         	</div>
		                         	<div class="col-md-4 col-sm-4 col-lg-3 m_margin_top-15">
		                         		<button type="button" class="btn green" id="search">查找</button>
		                         	</div>
	                         	
	                         </div>
	                        </div>
	                       	<div class="portlet box green m_margin_15_auto_0">
	                            <div class="portlet-title">
	                                <div class="caption">
	                                    <i class="fa fa-list"></i>学生考勤</div>
	                            </div>
	                            <div class="portlet-body flip-scroll">
	                                <table class="table table-bordered table-hover">
	                                    <thead id="thead" class="flip-content">

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
	    <div class="modal fade" id="small" tabindex="-1" role="dialog" aria-hidden="true">
	        <div class="modal-dialog modal-md">
	            <div class="modal-content">
	                <div class="modal-header">
	                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
	                    <h4 class="modal-title">导出</h4>
	                </div>
	                <div class="modal-body">
	                	<form action="#" class="form-horizontal">
                                        <div class="form-body">
                                            <div class="form-group">
                                                <label class="control-label col-md-3">选择部门</label>
                                                <div class="col-md-8">
                                                    <select class="form-control" id="gradeSelect_dialog">
                                                    
		                                            </select>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-md-3">开始时间</label>
                                                <div class="col-md-8">
                                                    <input type="text" class="form-control time" id="startTime_dialog" readonly="readonly"/>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-md-3">结束时间</label>
                                                <div class="col-md-8">
                                                    <input type="text" class="form-control time"  id="endTime_dialog" readonly="readonly"/> </div>
                                            </div>
                                        </div>
                                    </form>
	                </div>
	                <div class="modal-footer">
	                    <button type="button" class="btn green" data-dismiss="modal" id="export">导出</button>
	                    <button type="button" class="btn" data-dismiss="modal">取消</button>
	                </div>
	            </div>
	            <!-- /.modal-content -->
	        </div>
	        <!-- /.modal-dialog -->
	    </div>
	</body>
    <!-- 公共js开始 -->
    <%@ include file="/public_module/public_js.jsp" %>
    <!-- 公共js结束 -->
	<script>
		jQuery(document).ready(function() { 
			loadSubMenu("attendance"); 
			//选取当前菜单位置
			setActive("attendance","studentAttend"); 
			$('#startTime,.time').datetimepicker({
				 format:"Y-m-d",
				 timepicker:false
			});
			var myDate=new Date();
			var m=myDate.getMonth()+1;
			var d=myDate.getDate();
			m=m<10?"0"+m:m;
			d=d<10?"0"+d:d;
			$("#startTime").val(myDate.getFullYear()+"-"+m+"-"+d);

			//页面初始化时候的数据加载
			var userId = $("#userId").val();
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			
			//初始化年级select
			loadAllClassList(userId, 'studentAttendanceManager',roleCode);
			var clazzId = $("#gradeSelect").val();
			loadStudentCheckList(clazzId, null, userId, roleCode, null);
			//点击tab页加载
			$("#userTab li a").click(function(){
				$("#userTab li").removeAttr("class");
				$(this).parent().addClass("active");
				//用户ID
				var userId = $(this).attr("attr1");
				var roleCode = $(this).attr("attr2");
				loadAllClassList(userId, 'studentAttendanceManager',roleCode);
				loadStudentCheckList(null, null, userId, roleCode, null);
			});

			$("#search").click(function(){
				//用户ID
				var userId = $("#userTab li.active").find('a').attr("attr1");
				//角色ID
				var roleCode = $("#userTab li.active").find('a').attr("attr2");
				
				//年级ID
				var clazzId = $("#gradeSelect").val();
				//卡号
				var queryContent = $("#queryContent").val();

				loadStudentCheckList(clazzId, queryContent, userId, roleCode, null);
			});

			//导出数据
			$("#export").click(function(){
				//开始时间
				var startTime = $("#startTime_dialog").val();
				//结束时间
				var endTime = $("#endTime_dialog").val();

		        if ("" == startTime){
		            layer.tips('开始日期不能为空', '#startTime_dialog');
		            return false;
		        }
		        if ("" == endTime){
		            layer.tips('结束日期不能为空', '#endTime_dialog');
		            return false;
		        }
		        var startMill = (new Date(startTime)).getTime();
		        var endMill = (new Date(endTime)).getTime();
		        if(endMill < startMill){
			        layer.tips('结束日期不能小于开始日期', '#endTime_dialog');
			        return false;
		        }
		        
				//班级ID
				var clazzId = $("#gradeSelect_dialog").val();
				var roleCode = $("#userTab li.active").find('a').attr("attr2");
				standardPost('<%=basePath%>check/doExportExcelForStudent.do',{clazzId:clazzId,roleCode:roleCode,startTime:startTime,endTime:endTime});
				});
			
			$("#search").click();
		});

		//根据学校ID加载该学校所有的年级数据
		function loadAllClassList(userId, permissionCode,roleCode)
		{
			$.ajax({
				type: "post",
				async:false,
				url: "<%=basePath%>check/loadClazzList.do",
				data:{
					userId : userId,
					permissionCode : permissionCode,
					roleCode : roleCode
					},
				success: function(data){
					var gradeList = eval("(" + data + ")");
					var appendHtml = '';
					var appendHtml_dialog = '<option value="all">--请选择班级--</option>';
					if(gradeList==null || gradeList.length==0){
						appendHtml = '<option value="0">--请选择班级--</option>';
					 }
					$.each(gradeList, function(n, value) {  
           				appendHtml += '<option value="' + value.id + '">' + value.clazzName + '</option>';
           				appendHtml_dialog += '<option value="' + value.id + '">' + value.clazzName + '</option>';
          			}); 
          			$("#gradeSelect").html(appendHtml);
          			$("#gradeSelect_dialog").html(appendHtml_dialog);
				},
			});
		}

		
		//加载教师考勤页面
		//参数说明: gradeId:年级ID
		//		 code:工号
		//       name:姓名
		//       phone:手机号码
		//       cardCode:电子卡号
		function loadStudentCheckList(clazzId, queryContent, userId, roleCode, cPage)
		{
			var todayTime=$("#startTime").val();
		    var body = $("#body");
			App.blockUI(body);
			$.ajax({
				type: "post",
				url: "<%=basePath%>check/loadStudentCheckList.do",
				data:{
						from : '1',
						clazzId : clazzId,
						queryContent : queryContent,
						userId : userId,
						roleCode : roleCode,
						cPage : cPage,
						todayTime:todayTime
					},
				success: function(data){
					var page = eval("(" + data + ")");
					$("#currentPage").html(page.currentPage);
					$("#totalPage").html(page.totalPage);
					var scheduleType = 0;
					var headHtml = '';
					var appendHtml = '';
					$.each(page.list, function(n, value) { 
						scheduleType = value.scheduleType; 
						if (n%2 == 0)
						{
							appendHtml += '<tr style="background-color: #ededed">';
						}
						else
						{
							appendHtml += '<tr>';
						}	
           				appendHtml += '<td>' + value.gradeName + '</td>';
            			appendHtml += '<td>' + value.code + '</td>';
            			appendHtml += '<td> '+ value.name+'</td>';
//             			appendHtml += '<td>' + value.statusName.replace('旷工', '旷课').replace("班","课") + '</td>';
            			var attendanceList = value.currentDateInoutlist;
           				var amColspanHtml = '<td colspan="2">';
           				var pmColspanHtml = '<td colspan="2">';
           				var nightColspanHtml = '<td colspan="2">';
           				var allColspanHtml = '<td colspan="2">';
           				$.each(attendanceList, function(y, inout) {
								var status = inout.status;
								if(status <= 7){
									//上午
									if(status == 1){
										amColspanHtml += '<span><em class="kuang">矿</em> 无刷卡</span>&nbsp&nbsp&nbsp';
									}else if(status == 2){
										amColspanHtml += '<span><em class="chi">迟</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 3){
										amColspanHtml += '<span><em class="zhao">早</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 4){
										amColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 5){
										amColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 6){
										amColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 7){
										amColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}								
									
								}else if(status <= 14 && status >= 8){
									//下午
									
									if(status == 8){
										pmColspanHtml += '<span><em class="kuang">矿</em> 无刷卡</span>&nbsp&nbsp&nbsp';
									}else if(status == 9){
										pmColspanHtml += '<span><em class="chi">迟</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 10){
										pmColspanHtml += '<span><em class="zhao">早</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 11){
										pmColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 12){
										pmColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 13){
										pmColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 14){
										pmColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}

									
								}else if(status <= 21 && status >= 15){
									//晚上
									
									if(status == 15){
										nightColspanHtml += '<span><em class="kuang">矿</em> 无刷卡</span>&nbsp&nbsp&nbsp';
									}else if(status == 16){
										nightColspanHtml += '<span><em class="chi">迟</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 17){
										nightColspanHtml += '<span><em class="zhao">早</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 18){
										nightColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 19){
										nightColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 20){
										nightColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 21){
										nightColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}
									
								}else if(status <= 28 && status >= 22){
									//全天
									
									if(status == 22){
										allColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 23){
										allColspanHtml += '<span><em class="chi">迟</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 24){
										allColspanHtml += '<span><em class="zhao">早</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 25){
										allColspanHtml += '<span><em class="zheng">正</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 26){
										allColspanHtml += '<span><em class="kuang">矿</em> 无刷卡</span>&nbsp&nbsp&nbsp';
									}else if(status == 27){
										allColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}else if(status == 28){
										allColspanHtml += '<span><em class="fei">非</em>'+inout.inoutTime+'</span>&nbsp&nbsp&nbsp';
									}
								}

               				});
           				if(scheduleType == 0){
               				appendHtml += amColspanHtml+"</td>";
               				appendHtml += pmColspanHtml+"</td>";
               				//appendHtml += nightColspanHtml+"</td>";
               			}else{
               				appendHtml += allColspanHtml+"</td>";
                   		}
            			appendHtml += '<td>' + value.updateTime + '</td>';
           				appendHtml += '</tr>';
          			}); 
   					headHtml += '<tr>';
   					headHtml += '<th>班级</th>';
   					headHtml += '<th>学号</th>';
   					headHtml += '<th>姓名</th>';
   					if(scheduleType == 0){
   						headHtml += '<th colspan="2">上午</th>';
   						headHtml += '<th colspan="2">下午</th>';

   						//headHtml += '<th colspan="2">晚上</th>';
   	   				}else{
   	   					headHtml += '<th colspan="2">全天</th>';
   	   	   			}
   					headHtml += '<th>考勤日期</th>';
   					headHtml += '</tr>';
   					$("#thead").html(headHtml);
          			$("#tbody").html(appendHtml);
          			
          				//解锁UI
            	       App.unblockUI(body);
				},
				error:function(data){
					//解锁UI
         	       App.unblockUI(body);
				}
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
			//用户ID
			var userId = $("#userTab li.active").find('a').attr("attr1");
			var roleCode = $("#userTab li.active").find('a').attr("attr2");
			//年级ID
			var clazzId = $("#gradeSelect").val();
			//卡号
			var queryContent = $("#queryContent").val();
			
			loadStudentCheckList(clazzId, queryContent, userId, roleCode, cPageInt)
			$("#currentPage").html(cPageInt);
		}
	</script>
</html>