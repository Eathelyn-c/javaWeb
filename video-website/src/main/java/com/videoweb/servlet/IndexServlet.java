package com.videoweb.servlet;

import com.videoweb.dao.VideoDao;
import com.videoweb.model.Video;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// 首页入口：处理分页请求，查询视频列表并传递给JSP
@WebServlet("/index")
public class IndexServlet extends HttpServlet {
    private VideoDao videoDao = new VideoDao();
    private static final int PAGE_SIZE = 30;  // 每页显示30个视频（6行5列）

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 获取当前页码（默认第1页）
        String pageNumStr = request.getParameter("page");
        int pageNum = 1;
        if (pageNumStr != null && !pageNumStr.isEmpty()) {
            try {
                pageNum = Integer.parseInt(pageNumStr);
            } catch (NumberFormatException e) {
                pageNum = 1;  // 页码格式错误，默认第1页
            }
        }

        // 2. 查询当前页视频列表和总条数
        List<Video> videoList = videoDao.getVideoList(pageNum, PAGE_SIZE);
        int totalCount = videoDao.getTotalVideoCount();
        int totalPage = (int) Math.ceil((double) totalCount / PAGE_SIZE);  // 总页数（向上取整）

        // 3. 防止页码越界（小于1或大于总页数时跳回第1页）
        if (pageNum < 1 || (totalPage > 0 && pageNum > totalPage)) {
            response.sendRedirect(request.getContextPath() + "/index?page=1");
            return;
        }

        // 4. 将数据传递给JSP
        request.setAttribute("videoList", videoList);
        request.setAttribute("pageNum", pageNum);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("totalCount", totalCount);

        // 5. 转发到首页JSP
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}