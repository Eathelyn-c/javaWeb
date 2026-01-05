<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>${currentCategory.categoryName}</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
    <script src="<c:url value="/js/device-utils.js"/>"></script>
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
                <%-- 广告卡片固定在首位 --%>
                <div id="ad-container-0" class="news-card ad-card" data-tag="others">
                    <span class="ad-tag">广告</span>
                    <img src="<c:url value="/images/ad-default.jpg"/>" alt="广告加载中" class="news-img">
                    <h3 class="news-title">【广告】猜你喜欢</h3>
                </div>
                <script>
                    getRecommendAds('ad-container-0', 'others');
                </script>

                <%-- 新闻列表（11条） --%>
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

    <%-- 优化后的分页控件 --%>
    <div class="pagination">
        <button onclick="goPage(${pageNum - 1})" ${pageNum == 1 ? 'disabled' : ''}>上一页</button>
        <button onclick="goPage(${pageNum + 1})" ${pageNum == totalPage ? 'disabled' : ''}>下一页</button>
    </div>
</div>

<script>
    // 分页跳转
    function goPage(pageNum) {
        if (pageNum < 1 || pageNum > ${totalPage}) return;
        window.location.href = '<c:url value="/category"/>?categoryId=${currentCategory.categoryId}&pageNum=' + pageNum + '&pageSize=${pageSize}';
    }
</script>

<%@ include file="/common/footer.jsp" %>
</body>
</html>