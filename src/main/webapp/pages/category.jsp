<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>${currentCategory.categoryName}</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="category-container">
    <h2>${currentCategory.categoryName}</h2>
    <div class="news-list">
        <c:choose>
            <c:when test="${empty newsList}">
                <div class="no-result">暂无该分类的新闻</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${newsList}" var="news" varStatus="status">
                    <%-- 随机插入广告 --%>
                    <c:if test="${status.index == adPosition}">
                        <div class="news-card ad-card">
                            <span class="ad-tag">广告</span>
                            <img src="/images/ad-placeholder.jpg" alt="广告占位" class="news-img">
                            <h3 class="news-title">【广告】${currentCategory.categoryName}相关产品推荐</h3>
                        </div>
                    </c:if>

                    <%-- 新闻卡片 --%>
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