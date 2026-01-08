<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="header">
    <div class="header-inner">
        <h1 class="logo"><a href="<c:url value="/index"/>">新闻资讯网</a></h1>
        <!-- 导航栏：新增active类判断 -->
        <nav class="nav">
            <!-- 首页：currentCategoryId=0时高亮 -->
            <a href="<c:url value="/index"/>" class="${currentCategoryId == 0 ? 'active' : ''}">首页</a>
            <!-- 分类列表：currentCategoryId等于分类ID时高亮 -->
            <c:forEach items="${categoryList}" var="category">
                <a href="<c:url value="/category?categoryId=${category.categoryId}"/>"
                   class="${currentCategoryId == category.categoryId ? 'active' : ''}">
                        ${category.categoryName}
                </a>
            </c:forEach>
        </nav>
        <!-- 搜索框 -->
        <div class="search-box">
            <form action="<c:url value="/search"/>" method="get">
                <input type="text" name="keyword" placeholder="搜索新闻..." required>
                <button type="submit">搜索</button>
            </form>
        </div>
    </div>
</div>