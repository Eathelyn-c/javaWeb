<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>
        <c:choose>
            <c:when test="${not empty product}">${product.name} - 商品详情</c:when>
            <c:otherwise>商品详情</c:otherwise>
        </c:choose>
    </title>
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

        /* 商品详情容器 - 图片在左，文字在右 */
        .detail-container {
            width: 1200px;
            margin: 0 auto 40px;
            background-color: #fff;
            border-radius: 8px;
            padding: 40px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
            display: flex;
            gap: 60px;
            min-height: 500px;
        }

        /* 左侧图片区域 */
        .detail-img-container {
            flex: 0 0 500px;
            height: 500px;
            background-color: #f8f8f8;
            border-radius: 8px;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .detail-img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 4px;
        }

        /* 右侧商品信息 */
        .detail-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .detail-name {
            font-size: 32px;
            margin-bottom: 25px;
            color: #333;
            line-height: 1.3;
            font-weight: 600;
        }

        .detail-price {
            font-size: 40px;
            color: #ff6700;
            margin-bottom: 30px;
            border-bottom: 1px solid #eee;
            padding-bottom: 25px;
        }

        .detail-desc {
            font-size: 18px;
            color: #666;
            line-height: 1.8;
            margin-bottom: 35px;
            padding: 15px;
            background-color: #fafafa;
            border-radius: 6px;
            border-left: 4px solid #ff6700;
        }

        .detail-meta {
            display: flex;
            flex-direction: column;
            gap: 20px;
            margin-bottom: 40px;
            padding: 20px;
            background-color: #f9f9f9;
            border-radius: 6px;
        }

        .detail-stock, .detail-category {
            font-size: 18px;
            color: #444;
            display: flex;
            align-items: center;
        }

        .detail-stock:before, .detail-category:before {
            content: "•";
            color: #ff6700;
            margin-right: 10px;
            font-size: 24px;
        }

        .detail-stock .stock-count {
            color: #ff6700;
            font-weight: bold;
            margin-left: 5px;
        }

        .detail-category .category-name {
            color: #333;
            font-weight: 500;
            margin-left: 5px;
            background-color: #f0f0f0;
            padding: 3px 8px;
            border-radius: 4px;
        }

        .detail-btn {
            padding: 18px 40px;
            background-color: #ff6700;
            color: #fff;
            border: none;
            border-radius: 6px;
            font-size: 20px;
            cursor: pointer;
            transition: all 0.3s;
            font-weight: 600;
            letter-spacing: 1px;
            width: 300px;
            box-shadow: 0 4px 12px rgba(255,103,0,0.3);
        }

        .detail-btn:hover {
            background-color: #ff4500;
            transform: translateY(-2px);
            box-shadow: 0 6px 15px rgba(255,103,0,0.4);
        }

        /* 无商品信息 */
        .no-product {
            width: 1200px;
            margin: 50px auto;
            text-align: center;
            padding: 80px 0;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
        }

        .no-product h2 {
            color: #666;
            margin-bottom: 30px;
            font-size: 28px;
        }

        .no-product p {
            color: #999;
            margin-bottom: 40px;
            font-size: 16px;
        }

        .home-btn {
            display: inline-block;
            padding: 12px 30px;
            background-color: #ff6700;
            color: #fff;
            text-decoration: none;
            border-radius: 4px;
            font-size: 16px;
            transition: background-color 0.3s;
        }

        .home-btn:hover {
            background-color: #ff4500;
        }

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
            <input type="text" name="keyword" class="search-input" placeholder="输入商品名称搜索...">
            <button type="submit" class="search-btn">搜索</button>
        </form>
    </div>
</div>

<div class="back-link">
    <a href="product-list">← 返回首页</a>
</div>

<c:choose>
    <c:when test="${not empty product}">
        <div class="detail-container">
            <div class="detail-img-container">
                <img src="images/${product.imageUrl}"
                     alt="${product.name}"
                     class="detail-img">
            </div>

            <div class="detail-info">
                <h1 class="detail-name">${product.name}</h1>
                <div class="detail-price">¥${product.price}</div>
                <p class="detail-desc">${product.description}</p>
                <div class="detail-meta">
                    <div class="detail-stock">
                        库存：<span class="stock-count">${product.stock}</span> 件
                    </div>
                    <div class="detail-category">
                        分类：<span class="category-name">${product.category}</span>
                    </div>
                </div>
                <button class="detail-btn">加入购物车</button>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="no-product">
            <h2>商品不存在</h2>
            <p>抱歉，您查看的商品不存在或已被删除</p>
            <a href="product-list" class="home-btn">返回首页</a>
        </div>
    </c:otherwise>
</c:choose>

<div class="footer">
    <div class="footer-container">
        <p>© 2025 购物商城 版权所有</p>
    </div>
</div>

<script>
    document.querySelector('.detail-btn')?.addEventListener('click', function () {
        alert('商品已加入购物车！');
    });
</script>
</body>
</html>