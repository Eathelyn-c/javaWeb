<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>视频网站 - 首页</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
</head>
<body>
<div class="container">
    <!-- 页面标题 -->
    <h1 class="page-title">热门视频</h1>

    <!-- 视频网格布局（5列） -->
    <div class="video-grid">
        <c:forEach items="${videoList}" var="video">
            <!-- 单个视频项（封面+标题） -->
            <a href="${pageContext.request.contextPath}/play?id=${video.id}" class="video-item">
                <div class="cover-container">
                    <img src="${pageContext.request.contextPath}${video.coverPath}" alt="${video.title}" class="video-cover">
                </div>
                <div class="video-title">${video.title}</div>
            </a>
        </c:forEach>
    </div>

    <!-- 无视频时显示 -->
    <c:if test="${totalCount == 0}">
        <div class="no-video">暂无视频上传，请先添加视频到数据库～</div>
    </c:if>

    <!-- 分页控件（视频数>30时显示） -->
    <c:if test="${totalPage > 1}">
        <div class="pagination">
            <!-- 上一页 -->
            <a href="${pageContext.request.contextPath}/index?page=${pageNum > 1 ? pageNum - 1 : 1}"
               class="page-btn ${pageNum == 1 ? 'disabled' : ''}">上一页</a>

            <!-- 页码列表（最多显示10个页码） -->
            <div class="page-numbers">
                <c:set var="startPage" value="${pageNum - 4 > 1 ? pageNum - 4 : 1}"/>
                <c:set var="endPage" value="${startPage + 9 < totalPage ? startPage + 9 : totalPage}"/>

                <c:forEach begin="${startPage}" end="${endPage}" var="page">
                    <a href="${pageContext.request.contextPath}/index?page=${page}"
                       class="page-num ${page == pageNum ? 'active' : ''}">${page}</a>
                </c:forEach>
            </div>

            <!-- 下一页 -->
            <a href="${pageContext.request.contextPath}/index?page=${pageNum < totalPage ? pageNum + 1 : totalPage}"
               class="page-btn ${pageNum == totalPage ? 'disabled' : ''}">下一页</a>
        </div>
    </c:if>
</div>
</body>
</html>