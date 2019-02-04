<%
   String  apiKey=request.getParameter("apiKey");
   String schoolId =request.getParameter("schoolId");
 %>
<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%>
<head>
	<title>营养食谱搜索</title>
  	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
	<!--公共css开始-->
	<%@ include file="/public_module/app_public_css.jsp"%>
	<!--公共css结束--> 
</head>


<body>

	<div class="app_head">
		<i class="m_icon-return icon_left" onclick="history.go(-1);"></i>啦啦啦
	</div>
	<div class="app_search">
	
	<form action="<%=basePath%>recipe/toRecipeListByApiKey.do" class="form-horizontal" method="post">
			
			<span>选择时间</span>
			<p>
				<input readonly="readonly" name="parmDate" id="startDate" type="text">
			</p>
			<p>
				<button type="submit">查找</button>
			</p>
			
			<input type="hidden" name="schoolId" value="<%=schoolId%>">
			<input type="hidden" name="apiKey" value="<%=apiKey%>">
			
		</form>
	</div>

	</body>
	<!--公共js开始-->
	<%@ include file="/public_module/app_public_js.jsp"%>
	<!--公共js结束-->  
	
	
	<script type="text/javascript">

  		var opt={};
		var currYear = (new Date()).getFullYear();
		opt.date = {preset : 'date'};
		opt.datetime = {preset : 'datetime'};
		opt.time = {preset : 'time'};
		opt.default = {
			theme: 'android-ics light', //皮肤样式
	        display: 'modal', //显示方式 
	        mode: 'scroller', //日期选择模式
			dateFormat: 'yyyy-mm-dd',
			lang: 'zh',
			showNow: true,
			nowText: "当前",
	        startYear: currYear-10, //开始年份
	        endYear: currYear + 10 //结束年份
		};
		var optDateTime = $.extend(opt['datetime'], opt['default']);
	  	var optTime = $.extend(opt['time'], opt['default']);
	    /*$("#startday,#endday").mobiscroll(optDateTime).datetime(optDateTime);*/
	    $("#startDate,#endDate").mobiscroll($.extend(opt['date'], opt['default']));
	    $(".time").mobiscroll(optTime).time(optTime);
   </script>




</html>