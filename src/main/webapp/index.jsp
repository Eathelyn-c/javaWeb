<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>购物网站 - 首页</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Microsoft YaHei", sans-serif;
        }

        body {
            background-color: #f5f5f5;
            color: #333;
            min-width: 1200px;
        }

        .header {
            background-color: #333;
            color: #fff;
            padding: 15px 0;
        }

        .header-container {
            width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 24px;
            font-weight: bold;
            color: #fff;
            text-decoration: none;
        }

        .search-form {
            display: flex;
        }

        .search-input {
            width: 300px;
            height: 38px;
            padding: 0 10px;
            border: none;
            border-radius: 4px 0 0 4px;
            outline: none;
        }

        .search-btn {
            width: 80px;
            height: 38px;
            background-color: #ff6700;
            color: #fff;
            border: none;
            border-radius: 0 4px 4px 0;
            cursor: pointer;
            font-size: 16px;
        }

        .search-btn:hover {
            background-color: #ff4500;
        }

        /* 广告轮播样式 */
        .ad-banner {
            width: 1200px;
            margin: 20px auto;
            height: 200px;
            background-color: #f8f8f8;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            position: relative;
        }

        .ad-container {
            width: 100%;
            height: 100%;
            position: relative;
        }

        .ad-slider {
            width: 100%;
            height: 100%;
            position: relative;
        }

        .ad-item {
            position: absolute;
            width: 100%;
            height: 100%;
            opacity: 0;
            transition: opacity 0.5s ease;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .ad-item.active {
            opacity: 1;
            z-index: 1;
        }

        .ad-item img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .ad-prev, .ad-next {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 40px;
            height: 40px;
            background-color: rgba(0,0,0,0.5);
            color: white;
            border: none;
            border-radius: 50%;
            font-size: 18px;
            cursor: pointer;
            z-index: 2;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background-color 0.3s;
        }

        .ad-prev:hover, .ad-next:hover {
            background-color: rgba(0,0,0,0.7);
        }

        .ad-prev {
            left: 10px;
        }

        .ad-next {
            right: 10px;
        }

        .ad-dots {
            position: absolute;
            bottom: 15px;
            left: 50%;
            transform: translateX(-50%);
            display: flex;
            gap: 8px;
            z-index: 2;
        }

        .ad-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background-color: rgba(255,255,255,0.5);
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .ad-dot.active {
            background-color: #ff6700;
        }

        .ad-dot:hover {
            background-color: rgba(255,255,255,0.8);
        }

        /* 分类导航 */
        .category-nav {
            background-color: #fff;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin: 20px 0;
        }

        .category-container {
            width: 1200px;
            margin: 0 auto;
            padding: 15px 0;
        }

        .category-list {
            list-style: none;
            display: flex;
            justify-content: center;
            gap: 30px;
        }

        .category-item a {
            color: #333;
            text-decoration: none;
            font-size: 16px;
            padding: 5px 15px;
            border-radius: 4px;
            transition: all 0.3s;
        }

        .category-item a:hover, .category-item a.active {
            background-color: #ff6700;
            color: #fff;
        }

        /* 商品列表容器 */
        .container {
            width: 1200px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            padding-bottom: 50px;
        }

        /* 商品卡片 */
        .product-card {
            background-color: #fff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .product-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }

        .product-img-container {
            width: 100%;
            height: 200px;
            background-color: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }

        .product-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s ease;
        }

        .product-card:hover .product-img {
            transform: scale(1.05);
        }

        .product-info {
            padding: 15px;
        }

        .product-name {
            font-size: 16px;
            margin-bottom: 8px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .product-price {
            color: #ff6700;
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 8px;
        }

        .product-desc {
            font-size: 14px;
            color: #666;
            line-height: 1.4;
            height: 42px;
            overflow: hidden;
            margin-bottom: 10px;
        }

        .product-link {
            display: inline-block;
            padding: 8px 15px;
            background-color: #ff6700;
            color: #fff;
            text-decoration: none;
            border-radius: 4px;
            font-size: 14px;
            transition: background-color 0.3s;
        }

        .product-link:hover {
            background-color: #ff4500;
        }

        /* 页脚 */
        .footer {
            background-color: #333;
            color: #fff;
            padding: 20px 0;
            text-align: center;
            margin-top: 50px;
        }

        .footer-container {
            width: 1200px;
            margin: 0 auto;
        }
    </style>
    <!-- 引入用户兴趣处理脚本 -->
    <script src="${pageContext.request.contextPath}/js/userInterest.js"></script>
</head>
<body>
<div class="header">
    <div class="header-container">
        <a href="product-list" class="logo">购物商城</a>
        <!-- 搜索表单：绑定提交事件（处理搜索行为加分） -->
        <form action="product-search" method="post" class="search-form" onsubmit="return handleSearchSubmit(event)">
            <input type="text" name="keyword" id="searchKeyword" class="search-input" placeholder="输入商品名称搜索...">
            <button type="submit" class="search-btn">搜索</button>
        </form>
    </div>
</div>

<!-- 广告轮播区域 -->
<div class="ad-banner" id="adBanner">
    <div class="ad-container">
        <!-- 广告图片容器 -->
        <div class="ad-slider" id="adContainer">
            <!-- 广告图片会动态加载到这里 -->
        </div>

        <!-- 左右箭头 -->
        <button class="ad-prev" onclick="prevAdSlide()">❮</button>
        <button class="ad-next" onclick="nextAdSlide()">❯</button>

        <!-- 指示器 -->
        <div class="ad-dots" id="adDots">
            <!-- 指示点会动态生成 -->
        </div>
    </div>
</div>

<div class="category-nav">
    <div class="category-container">
        <ul class="category-list">
            <li class="category-item">
                <a href="product-list" class="${empty currentCategory ? 'active' : ''}">全部商品</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=food" class="${currentCategory == 'food' ? 'active' : ''}"
                   onclick="addInterestScore('food', 'clickCategory')">食品</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=makeup" class="${currentCategory == 'makeup' ? 'active' : ''}"
                   onclick="addInterestScore('makeup', 'clickCategory')">美妆</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=digital" class="${currentCategory == 'digital' ? 'active' : ''}"
                   onclick="addInterestScore('digital', 'clickCategory')">数码</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=sport" class="${currentCategory == 'sport' ? 'active' : ''}"
                   onclick="addInterestScore('sport', 'clickCategory')">运动</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=clothes" class="${currentCategory == 'clothes' ? 'active' : ''}"
                   onclick="addInterestScore('clothes', 'clickCategory')">服装</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=book" class="${currentCategory == 'book' ? 'active' : ''}"
                   onclick="addInterestScore('book', 'clickCategory')">图书</a>
            </li>
            <li class="category-item">
                <a href="product-list?category=others" class="${currentCategory == 'others' ? 'active' : ''}"
                   onclick="addInterestScore('others', 'clickCategory')">其他</a>
            </li>
        </ul>
    </div>
</div>

<div class="container">
    <c:choose>
        <c:when test="${not empty productList}">
            <c:forEach var="product" items="${productList}">
                <div class="product-card">
                    <div class="product-img-container">
                        <img src="${pageContext.request.contextPath}/images/${product.imageUrl}"
                             alt="${product.name}" class="product-img">
                    </div>
                    <div class="product-info">
                        <h3 class="product-name">${product.name}</h3>
                        <div class="product-price">¥${product.price}</div>
                        <p class="product-desc">${product.description}</p>
                        <a href="product-detail?id=${product.id}" class="product-link"
                           onclick="return handleViewDetailClick(event, '${product.category}', ${product.id})">
                            查看详情
                        </a>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div style="width: 100%; text-align: center; padding: 50px; grid-column: 1 / span 4;">
                <p style="font-size: 18px; color: #666;">暂无商品数据</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="footer">
    <div class="footer-container">
        <p>© 2025 购物商城 版权所有</p>
    </div>
</div>
</body>
</html>
