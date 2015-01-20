<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix ="c" %>
<!DOCTYPE HTML>

<html>
<head>
<title>URL Path Listing</title>
</head>
<body>


<table>
	
	<c:forEach var="url" items="${urls}">
		<tr>
			<td>${url}</td>
			
		</tr>
	</c:forEach>
</table>

</body>
</html>