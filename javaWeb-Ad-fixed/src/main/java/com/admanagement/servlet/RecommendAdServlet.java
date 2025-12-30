package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.model.UserBehavior;
import com.admanagement.service.AdService;
import com.admanagement.service.UserBehaviorService;
import com.admanagement.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/recommendAd")
public class RecommendAdServlet extends HttpServlet {

    private final UserBehaviorService behaviorService = new UserBehaviorService();
    private final AdService adService = new AdService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 解析前端 JSON 请求
        String userId = request.getParameter("anonymousUserId");
        String platform = request.getParameter("platform");

        if (userId == null || platform == null) {
            ResponseUtil.sendError(response, "Missing required parameters");
            return;
        }

        // 获取用户行为记录
        List<UserBehavior> behaviors = behaviorService.getBehaviorsByUserId(userId);

        if (behaviors.isEmpty()) {
            ResponseUtil.sendError(response, "No behavior found for this user");
            return;
        }

        // 找到分数最高的 tag
        Optional<UserBehavior> topBehavior = behaviors.stream()
                .filter(b -> platform.equalsIgnoreCase(b.getPlatform()))
                .max(Comparator.comparingInt(UserBehavior::getScore));

        if (!topBehavior.isPresent()) {
            ResponseUtil.sendError(response, "No behavior for this platform");
            return;
        }

        String topTag = topBehavior.get().getTag();

        // 根据 tag 找广告类别
        List<Advertisement> ads = adService.getActiveAdvertisements(); // 先拿所有 active 广告

        Optional<Advertisement> matchedAd = ads.stream()
                .filter(ad -> ad.getCategoryName() != null && ad.getCategoryName().equalsIgnoreCase(topTag))
                .filter(ad -> ad.getImageUrl() != null && !ad.getImageUrl().isEmpty())
                .findFirst();

        if (!matchedAd.isPresent()) {
            ResponseUtil.sendError(response, "No matching ad found");
            return;
        }

        Advertisement ad = matchedAd.get();

        // 返回图片 URL
        ResponseUtil.sendSuccess(response, Map.of(
                "imageUrl", ad.getImageUrl(),
                "adId", ad.getAdId(),
                "title", ad.getTitle(),
                "targetUrl", ad.getTargetUrl()
        ));
    }
}
