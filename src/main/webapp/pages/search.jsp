<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>搜索“${keyword}”</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="search-container">
    <h2>搜索“${keyword}”的结果</h2>
    <div class="news-list">
        <c:choose>
            <c:when test="${empty searchNewsList}">
                <div class="no-result">未找到相关新闻</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${searchNewsList}" var="news">
                    <div class="news-card">
                        <a href="<c:url value="/detail?id=${news.newsId}"/>">
                            <img src="<c:url value="/images/${news.coverImg}"/>" alt="${news.title}" class="news-img">
                            <h3 class="news-title">${news.title}</h3>
                        </a>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/common/footer.jsp" %>
</body>
</html>