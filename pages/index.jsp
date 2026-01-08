<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>新闻首页</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
    <script src="<c:url value="/js/device-utils.js"/>"></script>
    <script src="<c:url value="/js/ad-preview.js"/>"></script>
</head>
<body>
<%@ include file="/common/header.jsp" %>
<%@ include file="/common/cookie-modal.jsp" %>
<div class="index-container">
    <h2>头条新闻</h2>
    <div class="news-list">
        <%-- 广告卡片 --%>
        <div id="index-ad-container" class="news-card ad-card">
            <span class="ad-tag">广告</span>
            <img src="<c:url value="/images/ad-default.jpg"/>" alt="广告" class="news-img">
            <h3 class="news-title">【广告】猜你喜欢</h3>
        </div>

        <%-- 新闻列表（11条）--%>
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
        window.location.href = '<c:url value="/index"/>?pageNum=' + pageNum + '&pageSize=${pageSize}';
    }

    // Cookie弹窗逻辑（仅修改广告请求调用，不传tag）
    document.addEventListener('DOMContentLoaded', function() {
        // 广告预览初始化（未改动）
        if (window.initAdPreview) {
            setTimeout(initAdPreview, 300);
        }

        // 检查是否首次访问（未改动）
        const hasSeenModal = localStorage.getItem('hasSeenWelcomeModal');
        if (!hasSeenModal) {
            setTimeout(showWelcomeModal, 800);
        }
        bindModalEvents();


    });

    // 显示欢迎弹窗（未改动）
    function showWelcomeModal() {
        const overlay = document.getElementById('cookie-modal-overlay');
        const modal = document.getElementById('cookie-modal');
        if (overlay && modal) {
            overlay.style.display = 'block';
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden';
            console.log('显示首次访问欢迎弹窗');
        }
    }

    // 关闭弹窗（未改动）
    function closeWelcomeModal() {
        const overlay = document.getElementById('cookie-modal-overlay');
        const modal = document.getElementById('cookie-modal');
        if (overlay && modal) {
            overlay.style.display = 'none';
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
            localStorage.setItem('hasSeenWelcomeModal', 'true');
            console.log('关闭欢迎弹窗');
        }
    }

    // 绑定弹窗事件（未改动）
    function bindModalEvents() {
        const confirmBtn = document.getElementById('cookie-confirm-btn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', closeWelcomeModal);
        }
        const closeBtn = document.querySelector('.cookie-close-btn');
        if (closeBtn) {
            closeBtn.addEventListener('click', closeWelcomeModal);
        }
        const overlay = document.getElementById('cookie-modal-overlay');
        if (overlay) {
            overlay.addEventListener('click', closeWelcomeModal);
        }
    }
</script>

<%@ include file="/common/footer.jsp" %>
</body>
</html>