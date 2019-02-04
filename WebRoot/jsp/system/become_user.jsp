<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<head>
        <meta charset="utf-8" />
        <title>切换用户</title>
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
            <!-- END SIDEBAR -->
            <!-- BEGIN CONTENT -->
            <div class="page-content-wrapper">
                <!-- BEGIN CONTENT BODY -->
                <div class="page-content m_overflow_hidden m_page_content">
                	<div class="col-md-12 col-sm-12 m_page_con">
                    <div class="page-bar m_margin_0_0_0_0">
                        <ul class="page-breadcrumb">
                            <li><a href="">首页</a><i class="fa fa-circle"></i></li>
                            <li><a href="">系统管理</a><i class="fa fa-circle"></i></li>
                            <li><span>切换用户</span></li>
                        </ul>
                    </div>
                    <div class="row">
                        <div class="col-md-12 col-sm-12">
                            <div class="portlet light portlet-fit portlet-form bordered m_margin_15_auto_0" id="form_wizard_1">
                                <div class="portlet-title">
                                    <div class="caption">
                                        <i class=" icon-layers font-green"></i>
                                        <span class="caption-subject font-green sbold uppercase">切换用户</span>
                                    </div>
                                </div>
                                <div class="portlet-body">
                                    <!-- BEGIN FORM-->
                                    <form action="" class="form-horizontal" method="post" id="becomeUserNameForm">
                                        <div class="form-body">
                                            <div class="form-group">
                                                <label class="control-label col-md-3">用户名：</label>
                                                <div class="col-md-9 col-lg-4">
                                                    <input class="form-control" size="16" type="text" name="becomeUserName" id="becomeUserName"/>
                                                </div>
                                            </div>
                                            <!-- <div class="form-group">
                                                <label class="control-label col-md-3">密码：</label>
                                                <div class="col-md-9 col-lg-4">
                                               		<input class="form-control" size="16" type="password" name="passWord" id="passWord"/>
                                                </div>
                                            </div> -->
                                        </div>
                                        <div class="form-actions">
                                            <div class="row">
                                                <div class="col-md-offset-3 col-md-9">
	                                                <c:if test="${sessionScope.user.type != 2}">
														<button type="button" class="btn green" id="changeUser">确定</button>
													</c:if>
													<button type="button" class="btn" id="cancel">返回</button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>
                                    <!-- END FORM-->
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
		$(document).ready(function(){    
			loadSubMenu("systemManage"); 
			//选取当前菜单位置
			setActive("systemManage","becomeUser"); 
			$("#changeUser").click(function(){
				if (checkForm())
				{
					$.ajax({
		                cache: true,
		                type: "POST",
		                url:"<%=basePath%>user/doBecomeUser.do",
		                data:{
		                	becomeUserName:$('#becomeUserName').val()
			               	},
		                async: false,
		                success: function(data) {
		                	if(data == "" || data == "null" || data == null){
		                		layer.msg("该用户不存在!",{icon:2,time:1500});
				            }else{
				            	 var user = eval("(" + data + ")");
				                window.parent.location.href = "<%=basePath%>user/enterMain.do";  
					        }
		                }
		            });
				}
			});
		}); 
		function checkForm()
		{
			var becomeUserName = $.trim($("#becomeUserName").val());
			if ("" == becomeUserName)
			{
				layer.tips("请填写用户名","#becomeUserName");
				return false;
			}
			else if(!checkUserName(becomeUserName))
			{
				layer.tips("不存在此用户","#becomeUserName");
				return false;
			}
			return true;;
		}

		function checkUserName(userName)
		{
			var flag = false;
	
			$.ajax({
				async:false, 
				type: "post",
				data:{
					userName : userName,
				},
				url: "<%=basePath%>user/checkUserExist.do",
				success: function(data)
				{
					var result = eval("(" + data + ")");
					//此用户名存在
					if (result)
					{
						flag = true;
					}
					else
					{
						flag = false;
					}
					
				},
			});
			
			return flag;
		}
		
		//若是切换到sysadmin需要校验密码
		function checkPassWord(userName, passWord)
		{
			var flag = false;
	
			$.ajax({
				async:false, 
				type: "post",
				data:{
					userName : userName,
					passWord: passWord
				},
				url: "<%=basePath%>user/checkUserByNameAndPassWordExist.do",
				success: function(data)
				{
					var result = eval("(" + data + ")");
					//此用户名存在
					if ("success" == result.resultCode)
					{
						flag = true;
					}
					else
					{
						flag = false;
					}
					
				},
			});
			
			return flag;
		}
	</script>
</html>