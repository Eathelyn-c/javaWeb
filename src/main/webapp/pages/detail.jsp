<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% String ctx = request.getContextPath(); %>

<!DOCTYPE html>
<html>
<head>
    <title>${news.title} - 新闻详情</title>
    <link rel="stylesheet" href="<c:url value="/css/style.css"/>">
</head>
<!-- 其余代码不变 -->
<body>
<%@ include file="/common/header.jsp" %> <!-- 复用首页头部，保持一致 -->

<div class="detail-container">
    <!-- 新闻标题 -->
    <h1 class="news-title">${news.title}</h1>

    <!-- 新闻元信息：分类、发布时间 -->
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

    <!-- 新闻正文（自动渲染多图，无需额外处理） -->
    <div class="news-content">
        <c:out value="${news.content}" escapeXml="false"/>
    </div>

    <!-- 返回首页按钮 -->
    <a href="<c:url value="/index"/>" class="back-index">返回首页</a>
</div>

<%@ include file="/common/footer.jsp" %> <!-- 复用首页底部 -->
</body>
</html>