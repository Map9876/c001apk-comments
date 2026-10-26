# c001apk

fake coolapk

> **本项目为 [bggRGjQaUbCoE/c001apk](https://github.com/bggRGjQaUbCoE/c001apk) 的修复版**，
> 目标是修复「评论接口异常，请尝试升级到新版酷安」导致游客无法看评论的问题。
>
> ⚠️ 仅伪装高版本号（[CoolapkReviver](https://github.com/fentia/CoolapkReviver) 方案，16.6.4）**并不能**修好评论区：
> 实测 16.6.4 会把详情页也一起打进「当前访问需要验证码」的 gate，反而更糟。
>
> **真正让游客可见评论的修复**（V3 请求签名 + 网易云盾人机验证）移植自
> [KiritoXDone/c001apk](https://github.com/KiritoXDone/c001apk)（AGPL-3.0）：
> - `util/TokenV3.kt`：bcrypt cost=4 + lookup 表生成 `v3...` 签名，替代旧版 V2 token。
> - `util/captcha/*` + `ui/captcha/CaptchaActivity`：服务端返回 `err_request_captcha_v2` 时，
>   在 WebView 加载 `cstaticdun.126.net` 网易验证，解出 `validate` 后以 `NEC:<id前8>:<validate>` 回放。
> - 客户端版本固定为酷安 **16.1.3**（API v16，versionCode 2604022），这是服务端对游客放行的最低版本门槛。
>
> | Header | 原值 | 修复后 |
> | --- | --- | --- |
> | `X-App-Version` | 13.4.1 | 16.1.3 |
> | `X-App-Code` | 2312121 | 2604022 |
> | `X-Api-Version` | 13 | 16 |
> | `User-Agent` | +CoolMarket/13.4.1-2312121 | +CoolMarket/16.1.3-2604022 |

## 下载（Release 直链）

GitHub Actions 自动构建 APK 并发布到 Release，`latest` 为固定直链，始终指向最新构建：

- Release 构建（推荐安装）：<https://github.com/Map9876/c001apk-comments/releases/latest/download/c001apk-release.apk>
- Debug 构建：<https://github.com/Map9876/c001apk-comments/releases/latest/download/c001apk-debug.apk>

> 未配置签名密钥时 Release 包使用 debug 密钥签名，覆盖安装旧版需先卸载。

## 评论接口修复细节

- 客户端版本固定为 `16.1.3`（`VERSION_CODE=2604022`，`API_VERSION=16`）。这是服务端对**游客**放行评论区的最低版本门槛：低于此（如原版 13.4.1）评论接口返回 `403 评论接口异常`；伪装成 16.6.4 又会触发全局验证码 gate，故取 16.1.3。
- **V3 请求签名**（`util/TokenV3.kt`，移植自 KiritoXDone/c001apk）：服务端对 16.x 客户端要求 `v3...` 签名（bcrypt cost=4 + lookup 表），旧的 V2 token 已不被接受。所有请求经 `AddCookiesInterceptor` 自动加签。
- **网易云盾验证码**（`util/captcha/*` + `ui/captcha/CaptchaActivity`）：服务端风控触发 `err_request_captcha_v2` 时弹 WebView 加载 `cstaticdun.126.net` 验证，解出 `validate` 后以 `NEC:<captchaId前8>:<validate>` 作为 `_v2_post_token` 查询参数或 `validate` Cookie 回放（24h 内复用）。
- 启动时从服务端同步酷安版本号的逻辑（`MainViewModel.fetchAppInfo`）增加了下限保护：只有同步到的版本不低于内置版本时才应用，避免回退到旧版本再次触发 403。
- 老用户升级后会自动迁移本地保存的旧版本参数（一次性），无需手动清除数据。

Token/LoginUtil from: [CoolbbsYou](https://github.com/WaitFme/CoolbbsYou)
评论区可见性修复（V3 签名 + 验证码）from: [KiritoXDone/c001apk](https://github.com/KiritoXDone/c001apk)（AGPL-3.0）

## 声明

1. 本项目属于个人学习项目，仅用于学习和测试，严禁滥用！
2. 使用本项目中造成的不良影响及后果与本人无关。

## 已实现

- [x] 首页
    - [x] 自定义Tab(关注、应用、头条、热榜、话题、数码、酷图)
    - [x] 关注：支持选择关注分组
    - [x] 应用：支持更新应用
    - [x] 头条：展示banner、iconLinkGridCard、广场和动态
    - [x] 热榜、话题、数码、酷图：一般类型展示

- [x] 动态
    - [x] 收藏
    - [x] 分享
    - [x] 举报
    - [x] 拉黑
    - [x] 删除
    - [x] 点赞
    - [x] 回复
    - [x] 评论排序
    - [x] 快捷查看评论
    - [x] 发布文本动态

- [x] 投票
    - [x] 展示选项和观点

- [x] 评论
    - [x] 举报
    - [x] 拉黑
    - [x] 删除
    - [x] 点赞
    - [x] 回复
    - [x] 查看对话

- [x] 图片
    - [x] 保存
    - [x] 分享
    - [x] 复制链接

- [x] 话题/数码
    - [x] 关注
    - [x] 拉黑
    - [x] 讨论排序(数码)

- [x] 用户
    - [x] 关注
    - [x] 分享
    - [x] 举报
    - [x] 拉黑

- [x] 应用
    - [x] 关注
    - [x] 评论
    - [x] 下载

- [x] 搜索
    - [x] 搜索历史
    - [x] 动态搜索类型、排序
    - [x] 动态、应用、数码、用户、话题
    - [x] 在应用、数码、用户、话题中搜索

- [x] 消息
    - [x] 登录
    - [x] 动态
    - [x] 关注(用户、话题、数码、应用)
    - [x] 粉丝
    - [x] 本地收藏
    - [x] 浏览历史
    - [x] 我的常去
    - [x] 我的收藏
    - [x] 我的赞
    - [x] 我的回复
    - [x] @我的动态
    - [x] @我的评论
    - [x] 我收到的赞
    - [x] 好友关注
    - [x] 消息通知

- [x] 设置
    - [x] 随机/自定义机型参数
    - [x] 自定义主题
    - [x] 用户/话题黑名单
    - [x] 外部浏览器打开链接
    - [x] 字体大小调节
    - [x] 清理缓存
    - [x] 其他

- [x] 其他
    - [x] 文本超链接跳转(话题、用户、动态、应用)
    - [x] 支持打开链接
    - [x] 浏览器动态跳转
    - [x] 表情展示
    - [x] 回复表情选择
    - [x] 文本自由复制