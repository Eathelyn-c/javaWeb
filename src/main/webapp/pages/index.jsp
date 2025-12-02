<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <title>新闻首页</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="index-container">
    <h2>头条新闻</h2>
    <div class="news-list">
        <c:forEach items="${newsList}" var="news" varStatus="status">
            <%-- 随机位置插入广告 --%>
            <c:if test="${status.index == adPosition}">
                <div class="news-card ad-card">
                    <span class="ad-tag">广告</span>
                    <img src="<c:url value="/images/ad-placeholder.jpg"/>" alt="广告占位" class="news-img">
                    <h3 class="news-title">【广告】数码新品限时优惠 | 点击查看详情</h3>
                </div>
            </c:if>

            <%-- 新闻卡片（正确修改：保留你的newsId，只调整路径和图片） --%>
            <div class="news-card">
                <!-- 路径改对：/news/detail（匹配Servlet），但ID用你原来的newsId -->
                <a href="<c:url value="/detail?id=${news.newsId}"/>">
                    <img src="<c:url value="/images/${news.coverImg}"/>" alt="${news.title}" class="news-img">
                    <h3 class="news-title">${news.title}</h3>
                </a>
            </div>
        </c:forEach>
    </div>
</div>

<%@ include file="/common/footer.jsp" %>
</body>
</html>