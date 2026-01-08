// 广告预览层初始化（核心：绑定广告卡片点击事件+设置data-tag属性）
function initAdPreview() {
    // 创建预览层（仅首次执行时创建）
    let previewLayer = document.getElementById('ad-preview-layer');
    if (!previewLayer) {
        previewLayer = document.createElement('div');
        previewLayer.id = 'ad-preview-layer';
        // 预览层样式（全屏遮罩）
        previewLayer.style.position = 'fixed';
        previewLayer.style.top = '0';
        previewLayer.style.left = '0';
        previewLayer.style.width = '100vw';
        previewLayer.style.height = '100vh';
        previewLayer.style.backgroundColor = 'rgba(0,0,0,0.85)';
        previewLayer.style.display = 'none';
        previewLayer.style.justifyContent = 'center';
        previewLayer.style.alignItems = 'center';
        previewLayer.style.zIndex = '9999';
        previewLayer.style.cursor = 'pointer';

        // 预览层图片元素
        const previewImg = document.createElement('img');
        previewImg.id = 'ad-preview-img';
        previewImg.style.maxWidth = '90%';
        previewImg.style.maxHeight = '90%';
        previewImg.style.objectFit = 'contain';

        previewLayer.appendChild(previewImg);
        document.body.appendChild(previewLayer);

        // 点击预览层关闭
        previewLayer.addEventListener('click', function() {
            this.style.display = 'none';
        });

        // ESC键关闭预览层
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                previewLayer.style.display = 'none';
            }
        });
    }

    // 绑定所有广告卡片的点击事件（核心：设置data-tag+触发上报）
    const adCards = document.querySelectorAll('.ad-card');
    adCards.forEach(card => {
        // 确保广告卡片有data-tag属性（无则用当前最高权重tag）
        const currentTopTag = window.getTopRecentTag ? window.getTopRecentTag() : 'digital';
        if (!card.getAttribute('data-tag')) {
            card.setAttribute('data-tag', currentTopTag);
        }

        // 广告卡片点击逻辑
        card.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            // 获取广告ID和对应的tag
            const adId = this.getAttribute('data-adid');
            const adTag = this.getAttribute('data-tag') || 'others';

            // 触发广告点击上报（调用device-utils.js的reportAdClick）
            if (window.reportAdClick && adId) {
                window.reportAdClick(adId)
                    .then(data => {
                        console.log('广告点击上报成功：', data);
                    }).catch(err => {
                    console.error('广告点击上报失败：', err);
                });
            }

            // 显示广告图片预览
            const adImg = this.querySelector('.news-img');
            if (adImg) {
                const previewImg = document.getElementById('ad-preview-img');
                previewImg.src = adImg.src;
                previewImg.alt = adImg.alt || '广告预览';
                previewLayer.style.display = 'flex';
            }
        });
    });
}

// 页面加载完成后初始化（兼容延迟加载）
document.addEventListener('DOMContentLoaded', function() {
    // 延迟300ms初始化，确保广告卡片已渲染
    setTimeout(initAdPreview, 300);
});

// 页面跳转/刷新前重新初始化（适配动态加载的广告卡片）
document.addEventListener('click', function(e) {
    if (e.target.tagName === 'A' && e.target.href) {
        setTimeout(initAdPreview, 100);
    }
});