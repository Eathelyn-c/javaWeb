<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>搜索"${keyword}"</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
    <script src="<c:url value="/js/device-utils.js"/>"></script>
    <script src="<c:url value="/js/ad-preview.js"/>"></script>
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="search-container">
    <h2>搜索"${keyword}"的结果</h2>
    <div class="news-list">
        <%-- 广告卡片 --%>
        <div id="search-ad-container" class="news-card ad-card">
            <span class="ad-tag">广告</span>
            <img src="<c:url value="/images/ad-default.jpg"/>" alt="广告加载中" class="news-img">
            <h3 class="news-title">【广告】猜你喜欢</h3>
        </div>

        <%-- 搜索结果（11条）--%>
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

    <%-- 分页控件 --%>
    <div class="pagination">
        <button onclick="goPage(${pageNum - 1})" ${pageNum == 1 ? 'disabled' : ''}>上一页</button>
        <button onclick="goPage(${pageNum + 1})" ${pageNum == totalPage ? 'disabled' : ''}>下一页</button>
    </div>
</div>

<script>
    // 分页跳转（未改动）
    function goPage(pageNum) {
        if (pageNum < 1 || pageNum > ${totalPage}) return;
        window.location.href = '<c:url value="/search"/>?keyword=${keyword}&pageNum=' + pageNum + '&pageSize=${pageSize}';
    }

    // 核心修改：广告请求不传tag
    document.addEventListener('DOMContentLoaded', function() {
        if (window.initAdPreview) {
            setTimeout(initAdPreview, 300);
        }

    });
</script>

<%@ include file="/common/footer.jsp" %>
</body>
</html>