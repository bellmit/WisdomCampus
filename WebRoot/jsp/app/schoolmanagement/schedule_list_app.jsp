<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<!DOCTYPE html>
<head>
	<title>作息</title>
  	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
	<!--公共css开始-->
	<%@ include file="/public_module/app_public_css.jsp"%>
	<!--公共css结束--> 
</head>

<body id="body">

	<div class="app_head">
		<i class="  m_icon-return icon_left" onclick="window.richbook.onFinish();"></i>作息时间
		
			<a href="<%=basePath%>jsp/app/safeSchool/abnormal_search_app.jsp">
				<i class="icon-plus icon_right"></i>
			</a>							
		
		
	</div>
	<ul class="inout_list">
		
		<li>
			<p class="depart_p"><span><em class="late">上</em></span><span>2016-9</span><span><i class="icon-time"></i> 8:00-18:00</span></p>
		</li>
		<li>
			<p class="depart_p"><span><em class="Push">下</em></span><span>2016-9</span><span><i class="icon-time"></i> 8:00-18:00</span></p>
		</li>
		<li>
			<p class="depart_p"><span><em class="late">上</em></span><span>2016-9</span><span><i class="icon-time"></i> 8:00-18:00</span></p>
		</li>
		<li>
			<p class="depart_p"><span><em class="Push">下</em></span><span>2016-9</span><span><i class="icon-time"></i> 8:00-18:00</span></p>
		</li>
				
			
	</ul>

	</body>
	<!--公共js开始-->
	<%@ include file="/public_module/app_public_js.jsp"%>
	<!--公共js结束--> 



</html>