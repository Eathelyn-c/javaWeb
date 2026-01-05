<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>视频网站 - 首页</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <style>
        /* 隐私政策弹窗样式 */
        .privacy-modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.7);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        }
        .privacy-content {
            background: white;
            padding: 30px;
            border-radius: 8px;
            max-width: 500px;
            text-align: center;
        }
        .privacy-btn-group {
            margin-top: 20px;
            display: flex;
            gap: 15px;
            justify-content: center;
        }
        .privacy-btn {
            padding: 8px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .accept-btn {
            background: #4CAF50;
            color: white;
        }
        .reject-btn {
            background: #f44336;
            color: white;
        }
    </style>
</head>
<body>
<!-- 隐私政策弹窗 -->
<div id="privacyModal" class="privacy-modal" style="display: none;">
    <div class="privacy-content">
        <h3>隐私政策告知</h3>
        <p>本网站为优化广告投放体验，会生成匿名标识追踪您的视频观看行为（仅记录视频标签、观看进度，无个人敏感信息）。</p>
        <p>您可选择接受（正常使用）或拒绝（禁用行为追踪，不影响视频播放）。</p>
        <div class="privacy-btn-group">
            <button class="privacy-btn accept-btn" onclick="acceptPrivacy()">接受</button>
            <button class="privacy-btn reject-btn" onclick="rejectPrivacy()">拒绝</button>
        </div>
    </div>
</div>

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

<script>
    // 初始化匿名用户ID + 隐私弹窗控制
    (function() {
        const accepted = localStorage.getItem("privacy_accepted");
        const rejected = localStorage.getItem("privacy_rejected");

        // 未选择则显示弹窗
        if (!accepted && !rejected) {
            document.getElementById("privacyModal").style.display = "flex";
        }

        // 生成/获取匿名ID（核心方法）
        window.getAnonymousUserId = function() {
            // 1. 先从LocalStorage读取
            let userId = localStorage.getItem("anonymous_user_id");
            if (userId) return userId;

            // 2. 读取Cookie（备用）
            userId = getCookie("anonymous_user_id");
            if (userId) {
                localStorage.setItem("anonymous_user_id", userId);
                return userId;
            }

            // 3. 生成新UUID
            userId = 'user_' + ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );

            // 4. 双存储保障
            localStorage.setItem("anonymous_user_id", userId);
            setCookie("anonymous_user_id", userId, 365);
            return userId;
        };

        // Cookie工具函数
        function setCookie(name, value, days) {
            const date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            const expires = "expires=" + date.toUTCString();
            document.cookie = name + "=" + value + ";" + expires + ";path=/";
        }

        function getCookie(name) {
            const nameEQ = name + "=";
            const ca = document.cookie.split(';');
            for(let i=0; i < ca.length; i++) {
                let c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1,c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
            }
            return null;
        }

        // 暴露Cookie方法到全局
        window.setCookie = setCookie;
        window.getCookie = getCookie;
    })();

    // 隐私政策处理函数
    function acceptPrivacy() {
        localStorage.setItem("privacy_accepted", "true");
        document.getElementById("privacyModal").style.display = "none";
        // 初始化匿名ID
        window.getAnonymousUserId();
    }

    function rejectPrivacy() {
        localStorage.setItem("privacy_rejected", "true");
        // 清空已生成的匿名ID
        localStorage.removeItem("anonymous_user_id");
        document.cookie = "anonymous_user_id=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.getElementById("privacyModal").style.display = "none";
    }
</script>
</body>
</html>