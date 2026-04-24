# 百度大模型 API 申请步骤

> 推荐使用「千帆 ModelBuilder / 文心一言」多模态模型。
>
> 百度目前对接大模型有两条路:**千帆大模型平台(ModelBuilder)** 和 **智能云 Qianfan API**。
> 舌诊分析属于「图像理解 + 中医知识推理」,需要**多模态(图文)大模型**,
> 推荐使用 `ERNIE-4.5-VL` / `ERNIE-4.0-Turbo-VL` 等带视觉能力的模型。

---

## 步骤 1:注册百度智能云账号

1. 打开 <https://cloud.baidu.com>,点击「注册」,用手机号注册。
2. 登录后进入「控制台」,完成**实名认证**(个人或企业均可,个人认证免费,
   用身份证 + 人脸识别)。**没有实名认证无法调用大模型 API。**

## 步骤 2:开通千帆大模型平台

1. 在控制台搜索「**千帆 ModelBuilder**」(原名千帆大模型平台),进入产品页:
   <https://console.bce.baidu.com/qianfan/>。
2. 点击「立即开通」,勾选服务协议。开通是免费的,调用按量计费
   (新用户一般有免费额度 / 代金券)。
3. 进入「**模型广场**」→ 找到多模态模型
   (如 `ERNIE-4.5-VL`、`ERNIE-4.0-Turbo-VL`,或视觉理解模型 `Fuyu-8B`),
   点击「**开通服务**」或「**付费开通**」,**开通后才能调用**。

## 步骤 3:创建应用,获取 API Key / Secret Key

1. 千帆控制台左侧「**应用接入**」→「**创建应用**」。
2. 填写应用名称(如 `tongue-diagnosis`),勾选要使用的模型(多模态类)。
3. 创建后会得到:
   - `API Key`(即 **AK**)
   - `Secret Key`(即 **SK**)

   这两个值**妥善保管**,调用接口时需要用它们换取 `access_token`。
   **不要提交到 git**,请通过环境变量注入。

## 步骤 4:了解计费与免费额度

- 进入「**计费管理**」查看剩余代金券与单价。
- 新用户通常可领 **20 元代金券**,用于测试完全够用。
- 建议在「**用量告警**」中设置预算上限,防止超额。

## 步骤 5:阅读官方接口文档

- 千帆 API 文档总入口:<https://cloud.baidu.com/doc/WENXINWORKSHOP/index.html>
- 获取 `access_token`:
  `POST https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={AK}&client_secret={SK}`
  - 返回的 `access_token` 有效期 30 天,建议缓存。
- 多模态对话接口示例(ERNIE-VL 系):
  - `POST https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.5-vl?access_token=xxx`
  - 请求体 `messages` 中 `content` 支持
    `[{type:"text", ...}, {type:"image_url", image_url:{url|base64}}]` 结构。

---

## 备用方案

如果不想用千帆,百度还有「**AI 开放平台**」的单独接口 ——
**舌象分析没有官方专用接口**,但有「**中医体质辨识**」和通用图像识别,
效果不如大模型。因此**优先走千帆多模态大模型**。

---

## 本项目中如何注入 AK / SK

**macOS / Linux**

```bash
export BAIDU_AK=你的ApiKey
export BAIDU_SK=你的SecretKey
# 可选:切换到其它多模态 endpoint,默认 ernie-4.5-vl
# export BAIDU_MODEL_ENDPOINT=ernie-4.0-turbo-vl
```

**Windows PowerShell**

```powershell
$env:BAIDU_AK="你的ApiKey"
$env:BAIDU_SK="你的SecretKey"
```

配置对应 `rs/src/main/resources/application.yml` 中的
`baidu.qianfan.api-key` / `baidu.qianfan.secret-key` / `baidu.qianfan.model-endpoint`。
