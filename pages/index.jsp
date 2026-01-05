<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>新闻首页</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
    <script src="<c:url value="/js/device-utils.js"/>"></script>
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="index-container">
    <h2>头条新闻</h2>
    <div class="news-list">
        <%-- 广告卡片固定在每页首位 --%>
        <div id="index-ad-container" class="news-card ad-card">
            <span class="ad-tag">广告</span>
            <img src="<c:url value="/images/ad-default.jpg"/>" alt="广告" class="news-img">
            <h3 class="news-title">【广告】猜你喜欢</h3>
        </div>

        <%-- 新闻列表（11条） --%>
        <c:choose>
            <c:when test="${empty newsList}">
                <div class="no-result">暂无新闻</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${newsList}" var="news" varStatus="status">
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

    <%-- 优化后的分页控件（仅保留上一页/下一页，移除总条数） --%>
    <div class="pagination">
        <button onclick="goPage(${pageNum - 1})" ${pageNum == 1 ? 'disabled' : ''}>上一页</button>
        <button onclick="goPage(${pageNum + 1})" ${pageNum == totalPage ? 'disabled' : ''}>下一页</button>
    </div>
</div>

<script>
    // 页面加载后请求广告
    window.onload = function() {
        getRecommendAds('index-ad-container', 'digital');
    }

    // 分页跳转
    function goPage(pageNum) {
        if (pageNum < 1 || pageNum > ${totalPage}) return;
        window.location.href = '<c:url value="/index"/>?pageNum=' + pageNum + '&pageSize=${pageSize}';
    }
</script>

<%@ include file="/common/footer.jsp" %>
</body>
</html>