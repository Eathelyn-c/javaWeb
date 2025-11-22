# GitHub 上传指南

由于网络连接问题，无法直接推送到 GitHub。以下是几种解决方案：

## 方案 1: 使用 GitHub Desktop（推荐）

1. 下载并安装 [GitHub Desktop](https://desktop.github.com/)
2. 登录你的 GitHub 账号
3. 点击 "File" -> "Add Local Repository"
4. 选择项目目录：`/Users/eathelyn/Desktop/广告投放系统`
5. 点击 "Publish repository" 按钮
6. 选择推送到 `Eathelyn-c/javaWeb` 仓库

## 方案 2: 配置代理（如果你有 VPN）

如果你使用代理或 VPN，可以配置 git 使用代理：

```bash
# HTTP 代理
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy https://127.0.0.1:7890

# 或 SOCKS5 代理
git config --global http.proxy socks5://127.0.0.1:7890
git config --global https.proxy socks5://127.0.0.1:7890

# 然后再次尝试推送
git push -u origin main

# 推送成功后可以取消代理设置
git config --global --unset http.proxy
git config --global --unset https.proxy
```

## 方案 3: 使用 SSH（需要配置密钥）

```bash
# 1. 生成 SSH 密钥
ssh-keygen -t ed25519 -C "your_email@example.com"

# 2. 查看公钥
cat ~/.ssh/id_ed25519.pub

# 3. 复制公钥内容，添加到 GitHub
#    访问: https://github.com/settings/keys
#    点击 "New SSH key"，粘贴公钥

# 4. 更改远程仓库 URL 为 SSH
cd /Users/eathelyn/Desktop/广告投放系统
git remote set-url origin git@github.com:Eathelyn-c/javaWeb.git

# 5. 推送
git push -u origin main
```

## 方案 4: 手动上传（临时方案）

1. 访问 https://github.com/Eathelyn-c/javaWeb
2. 点击 "Add file" -> "Upload files"
3. 将整个项目文件夹拖入浏览器
4. 填写提交信息，点击 "Commit changes"

## 当前项目状态

✅ 项目已准备就绪：
- Git 仓库已初始化
- 所有文件已添加和提交
- 远程仓库已配置为: https://github.com/Eathelyn-c/javaWeb.git
- 分支名称: main
- 提交信息已添加

只需要解决网络连接问题即可推送成功。

## 验证推送成功

推送成功后，访问以下 URL 验证：
https://github.com/Eathelyn-c/javaWeb

你应该能看到：
- 45 个文件
- README.md 显示项目说明
- database/ 目录包含数据库脚本
- src/ 目录包含所有源代码
- frontend/ 目录包含前端页面

---

**推荐**: 使用 GitHub Desktop（方案 1）最简单快捷！
