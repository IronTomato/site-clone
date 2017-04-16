<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Site Clone</title>
    <base href="${pageContext.request.contextPath}">
    <script src="/js/jquery-3.2.1.js"></script>
    <script src="/js/index.js"></script>
</head>
<body>
<p>
    <label>Origin Url:</label>
    <input id="origin-url" type="text" width="1000"/>
    <input id="clone-button" type="button" value="clone">
</p>
<div>
    <ol id="origin-url-list">

    </ol>
</div>
</body>
</html>
