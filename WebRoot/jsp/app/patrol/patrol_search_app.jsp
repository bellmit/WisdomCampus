<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ include file="/basepath.jsp"%> 
<head>
	<title>校务巡更搜索</title>
  	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
	<!--公共css开始-->
	<%@ include file="/public_module/app_public_css.jsp"%>
	<!--公共css结束--> 
</head>
<body>
	<div class="app_head">
		<i class="m_icon-return icon_left" onclick="history.go(-1);"></i>校务巡更搜索
	</div>
	<div class="app_search">
		<form action="<%=basePath%>patrol/toPatrolListByApiKey.do" class="form-horizontal" method="post">
			<span>巡查日期</span>
			<p>
				<input readonly="readonly" name="createDate" id="createDate" value="${createDate }" type="text">
			</p>
			<span>巡查人员</span>
			<p><input type="text" name="queryContent" id="queryContent"></p>
			<p>
				<button type="submit">查找</button>
			</p>
			<input type="hidden" name="schoolId" value="${schoolId }">
			<input type="hidden" name="apiKey" value="${apiKey}">
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
		$("#createDate").val('').scroller('destroy').scroller($.extend(opt['date'], opt['default']));
   </script>

</html>