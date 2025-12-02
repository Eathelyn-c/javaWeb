<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <%-- Jakarta JSTL标签库 --%>
<div class="header">
    <div class="header-inner">
        <h1 class="logo"><a href="<c:url value="/index"/>">新闻资讯网</a></h1> <!-- 改这里 -->
        <!-- 导航栏 -->
        <nav class="nav">
            <a href="<c:url value="/index"/>">首页</a> <!-- 改这里 -->
            <c:forEach items="${categoryList}" var="category">
                <a href="<c:url value="/category?categoryId=${category.categoryId}"/>">${category.categoryName}</a> <!-- 改这里 -->
            </c:forEach>
        </nav>
        <!-- 搜索框 -->
        <div class="search-box">
            <form action="<c:url value="/search"/>" method="get"> <!-- 改这里 -->
                <input type="text" name="keyword" placeholder="搜索新闻..." required>
                <button type="submit">搜索</button>
            </form>
        </div>
    </div>
</div>