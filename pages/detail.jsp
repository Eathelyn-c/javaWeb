<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% String ctx = request.getContextPath(); %>

<c:if test="${empty news}">
    <script>
        alert('新闻不存在或已删除');
        window.location.href = '<c:url value="/index"/>';
    </script>
</c:if>

<!DOCTYPE html>
<html>
<head>
    <title>${news.title} - 新闻详情</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
    <script src="<c:url value="/js/device-utils.js"/>"></script>
    <style>
        .detail-container {max-width: 1200px;margin: 0 auto;padding: 20px;line-height: 1.8;font-size: 16px;}
        .news-title {font-size: 24px;color: #333;text-align: center;margin: 20px 0;font-weight: bold;}
        .news-meta {text-align: center;color: #666;font-size: 14px;margin-bottom: 30px;border-bottom: 1px solid #eee;padding-bottom: 10px;}
        .news-meta span {margin: 0 10px;}
        .news-content {color: #333;margin-bottom: 30px;}
        .news-content img {max-width: 100%;height: auto;display: block;margin: 20px auto;border-radius: 8px;}
        .news-content p {margin: 15px 0;text-indent: 2em;}
        .like-btn {margin: 20px 0;padding: 8px 16px;background: #ff4400;color: white;border: none;border-radius: 4px;cursor: pointer;transition: background 0.3s;}
        .like-btn:hover {background: #e03c00;}
        .like-btn:disabled {background: #cccccc;cursor: not-allowed;}
        .back-index {display: inline-block;padding: 8px 16px;background: #f5f5f5;color: #333;text-decoration: none;border-radius: 4px;margin-top: 10px;}
        .back-index:hover {background: #eee;}
        @media (max-width: 768px) {.detail-container {padding: 10px;}.news-title {font-size: 20px;}}
    </style>
</head>
<body>
<%@ include file="/common/header.jsp" %>

<div class="detail-container">
    <h1 class="news-title">${news.title}</h1>
    <div class="news-meta">
        <span>分类：
            <c:choose>
                <c:when test="${news.categoryId == 1}">食品美食</c:when>
                <c:when test="${news.categoryId == 2}">美妆护肤</c:when>
                <c:when test="${news.categoryId == 3}">数码科技</c:when>
                <c:when test="${news.categoryId == 4}">运动户外</c:when>
                <c:when test="${news.categoryId == 5}">服装穿搭</c:when>
                <c:when test="${news.categoryId == 6}">书籍读物</c:when>
                <c:when test="${news.categoryId == 7}">其他分类</c:when>
                <c:otherwise>未分类</c:otherwise>
            </c:choose>
        </span>
        <span>发布时间：<fmt:formatDate value="${news.createTime}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
    </div>

    <div class="news-content">
        <c:choose>
            <c:when test="${fn:contains(news.content, 'src=\"/images/')}">
                <c:set var="searchPattern" value='src="/images/' />
                <c:set var="replacePattern" value='src="${pageContext.request.contextPath}/images/' />
                <c:set var="processedContent" value="${fn:replace(news.content, searchPattern, replacePattern)}" />
                <c:out value="${processedContent}" escapeXml="false"/>
            </c:when>
            <c:otherwise>
                <c:out value="${news.content}" escapeXml="false"/>
            </c:otherwise>
        </c:choose>
    </div>

    <button id="like-btn" class="like-btn">点赞</button>
    <a href="<c:url value="/index"/>" class="back-index">返回首页</a>
</div>

<script>
    // 基础变量
    const categoryId = ${empty news.categoryId ? 7 : news.categoryId};
    const newsId = ${empty news.newsId ? 0 : news.newsId};
    let newsTag = 'others';
    if (categoryId === 1) newsTag = 'food';
    else if (categoryId === 2) newsTag = 'makeup';
    else if (categoryId === 3) newsTag = 'digital';
    else if (categoryId === 4) newsTag = 'sport';
    else if (categoryId === 5) newsTag = 'clothes';
    else if (categoryId === 6) newsTag = 'book';

    // 点赞状态（仅本地记录，不上影响权重）
    let likedNews = [];
    try {
        likedNews = JSON.parse(localStorage.getItem('liked_news') || '[]');
        if (!Array.isArray(likedNews)) likedNews = [];
    } catch (e) {
        likedNews = [];
        localStorage.setItem('liked_news', '[]');
    }
    const likeBtn = document.getElementById('like-btn');
    if (likedNews.includes(newsId)) {
        likeBtn.disabled = true;
        likeBtn.innerText = '已点赞';
    }

    // 返回顶部按钮
    const backToTopBtn = document.createElement('button');
    backToTopBtn.innerText = '返回顶部';
    backToTopBtn.style.position = 'fixed';
    backToTopBtn.style.bottom = '30px';
    backToTopBtn.style.right = '30px';
    backToTopBtn.style.padding = '8px 12px';
    backToTopBtn.style.background = '#ff4400';
    backToTopBtn.style.color = 'white';
    backToTopBtn.style.border = 'none';
    backToTopBtn.style.borderRadius = '4px';
    backToTopBtn.style.cursor = 'pointer';
    backToTopBtn.style.display = 'none';
    document.body.appendChild(backToTopBtn);

    window.addEventListener('scroll', () => {
        backToTopBtn.style.display = window.scrollY > 300 ? 'block' : 'none';
    });
    backToTopBtn.addEventListener('click', () => {
        window.scrollTo({top: 0, behavior: 'smooth'});
    });

    // 浏览时长上报（仅上报，无本地权重）
    let hasReportedRead = false;
    const startTime = Date.now();
    window.addEventListener('beforeunload', () => {
        const staySeconds = Math.floor((Date.now() - startTime) / 1000);
        if (staySeconds >= 30 && !hasReportedRead) {
            reportUserBehavior(newsTag, 2); // 浏览行为score=2
            hasReportedRead = true;
        }
    });

    // 点赞上报（仅上报，无本地权重）
    likeBtn.addEventListener('click', function() {
        if (likedNews.includes(newsId)) return;
        this.disabled = true;
        this.innerText = '点赞中...';
        reportUserBehavior(newsTag, 3) // 点赞行为score=3
            .then(data => {
                if (data.code === 200) {
                    likedNews.push(newsId);
                    localStorage.setItem('liked_news', JSON.stringify(likedNews));
                    this.innerText = '已点赞';
                } else {
                    this.disabled = false;
                    this.innerText = '点赞';
                    alert('点赞失败，请重试');
                }
            })
            .catch(() => {
                this.disabled = false;
                this.innerText = '点赞';
                alert('点赞失败，请重试');
            });
    });
</script>

<%@ include file="/common/footer.jsp" %>
</body>
</html>