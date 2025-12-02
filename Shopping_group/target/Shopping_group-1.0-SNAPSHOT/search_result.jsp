<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>搜索结果 - ${keyword}</title>
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
        }

        /* 头部导航栏 */
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

        /* 返回链接 */
        .back-link {
            width: 1200px;
            margin: 20px auto 10px;
        }

        .back-link a {
            color: #666;
            text-decoration: none;
            font-size: 14px;
        }

        .back-link a:hover {
            color: #ff6700;
        }

        /* 搜索结果提示 */
        .search-header {
            width: 1200px;
            margin: 0 auto 30px;
            padding: 25px 30px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            font-size: 18px;
            color: #333;
            border-left: 5px solid #ff6700;
        }

        .search-keyword {
            color: #ff6700;
            font-weight: bold;
            font-size: 20px;
        }

        .result-count {
            color: #ff6700;
            font-weight: bold;
            font-size: 24px;
        }

        /* 搜索结果列表容器 */
        .search-container {
            width: 1200px;
            margin: 0 auto;
            display: flex;
            flex-direction: column;
            gap: 25px;
            padding-bottom: 50px;
        }

        /* 单个搜索结果项 - 图片在左，文字在右 */
        .search-item {
            background-color: #fff;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
            display: flex;
            transition: all 0.3s ease;
        }

        .search-item:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
        }

        /* 左侧图片区域 */
        .search-img-container {
            flex: 0 0 250px;
            height: 250px;
            background-color: #f8f8f8;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            border-right: 1px solid #eee;
        }

        .search-img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 6px;
        }

        /* 右侧商品信息 */
        .search-info {
            flex: 1;
            padding: 30px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .search-name {
            font-size: 24px;
            margin-bottom: 15px;
            color: #333;
            font-weight: 600;
        }

        .search-price {
            color: #ff6700;
            font-size: 28px;
            font-weight: bold;
            margin-bottom: 20px;
        }

        .search-desc {
            font-size: 16px;
            color: #666;
            line-height: 1.6;
            margin-bottom: 25px;
            flex-grow: 1;
        }

        .search-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .search-category {
            font-size: 14px;
            color: #888;
            background-color: #f0f0f0;
            padding: 5px 12px;
            border-radius: 4px;
        }

        .search-link {
            display: inline-block;
            padding: 12px 30px;
            background-color: #ff6700;
            color: #fff;
            text-decoration: none;
            border-radius: 6px;
            font-size: 16px;
            transition: all 0.3s;
            font-weight: 500;
        }

        .search-link:hover {
            background-color: #ff4500;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(255,103,0,0.3);
        }

        /* 无搜索结果提示 */
        .no-result {
            width: 1200px;
            margin: 50px auto;
            text-align: center;
            padding: 100px 0;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
        }

        .no-result h2 {
            font-size: 28px;
            color: #666;
            margin-bottom: 20px;
        }

        .no-result p {
            font-size: 18px;
            color: #888;
            margin-bottom: 30px;
        }

        .no-result .keyword {
            color: #ff6700;
            font-weight: bold;
        }

        .suggestions {
            margin-top: 30px;
            font-size: 16px;
            color: #666;
        }

        .suggestions a {
            color: #ff6700;
            text-decoration: none;
            margin: 0 10px;
        }

        .suggestions a:hover {
            text-decoration: underline;
        }

        /* 页脚 */
        .footer {
            background-color: #333;
            color: #fff;
            padding: 25px 0;
            text-align: center;
        }

        .footer-container {
            width: 1200px;
            margin: 0 auto;
        }
    </style>
</head>
<body>
<div class="header">
    <div class="header-container">
        <a href="product-list" class="logo">购物商城</a>
        <form action="product-search" method="post" class="search-form">
            <input type="text" name="keyword" class="search-input"
                   placeholder="输入商品名称搜索..." value="${keyword}">
            <button type="submit" class="search-btn">搜索</button>
        </form>
    </div>
</div>

<div class="back-link">
    <a href="product-list">← 返回首页</a>
</div>

<div class="search-header">
    搜索关键词：<span class="search-keyword">"${keyword}"</span>，
    共找到 <span class="result-count">${searchResult.size()}</span> 件商品
</div>

<c:choose>
    <c:when test="${not empty searchResult && searchResult.size() > 0}">
        <div class="search-container">
            <c:forEach var="product" items="${searchResult}">
                <div class="search-item">
                    <!-- 左侧：商品图片 -->
                    <div class="search-img-container">
                        <img src="images/${product.imageUrl}"
                             alt="${product.name}"
                             class="search-img">
                    </div>

                    <div class="search-info">
                        <div>
                            <h3 class="search-name">${product.name}</h3>
                            <div class="search-price">¥${product.price}</div>
                            <p class="search-desc">${product.description}</p>
                        </div>
                        <div class="search-meta">
                            <span class="search-category">${product.category}</span>
                            <a href="product-detail?id=${product.id}" class="search-link">查看详情</a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <div class="no-result">
            <h2>没有找到相关商品</h2>
            <p>没有找到与"<span class="keyword">${keyword}</span>"相关的商品，请更换关键词重试！</p>
            <div style="margin-top: 30px;">
                <a href="" style="display: inline-block; padding: 12px 30px; background-color: #ff6700; color: #fff; text-decoration: none; border-radius: 6px; font-size: 16px;">返回首页浏览所有商品</a>
            </div>
            <div class="suggestions">
                热门搜索：
                <a href="product-search?keyword=牛奶">牛奶</a>
                <a href="product-search?keyword=耳机">耳机</a>
                <a href="product-search?keyword=口红">口红</a>
                <a href="product-search?keyword=图书">图书</a>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<div class="footer">
    <div class="footer-container">
        <p>© 2025 购物商城 版权所有</p>
    </div>
</div>
</body>
</html>