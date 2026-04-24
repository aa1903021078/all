# 舌诊 AI(百度千帆多模态大模型)

本功能支持:前端上传一张舌头照片 → 后端调用百度千帆(ModelBuilder)多模态大模型 →
返回舌质 / 舌苔 / 舌型 / 证型提示 / 调养建议的结构化结果。

> ⚠️ 本功能仅供健康参考,**不构成医疗诊断**,如有不适请及时就医。

---

## 一、申请百度大模型 API(必做,先拿到 AK/SK)

### 步骤 1:注册百度智能云账号
1. 打开 <https://cloud.baidu.com>,点击「注册」,用手机号注册。
2. 登录后进入「控制台」,完成**实名认证**(个人或企业均可,个人认证免费,
   用身份证 + 人脸识别)。**没有实名认证无法调用大模型 API。**

### 步骤 2:开通千帆大模型平台(ModelBuilder)
1. 控制台搜索「**千帆 ModelBuilder**」,或直接打开
   <https://console.bce.baidu.com/qianfan/>。
2. 点击「立即开通」,勾选服务协议。开通免费,按量计费,
   新用户一般有免费额度 / 代金券。
3. 进入「**模型广场**」→ 找到多模态模型
   (推荐 `ERNIE-4.5-VL`,也可选 `ERNIE-4.0-Turbo-VL`),
   点击「**开通服务**」。**开通后才能调用**。

### 步骤 3:创建应用,获取 API Key / Secret Key
1. 千帆控制台左侧「**应用接入**」→「**创建应用**」。
2. 填写应用名称(如 `tongue-diagnosis`),勾选要使用的多模态模型。
3. 创建后会得到:
   - **API Key**(AK)
   - **Secret Key**(SK)

   这两个值请**妥善保管**,**不要提交到 git**。

### 步骤 4:查看计费与设置预算告警
- 控制台「**计费管理**」查看剩余代金券与单价。
- 「**用量告警**」中建议设置月度预算上限,避免被刷单。

### 步骤 5:阅读官方文档
- 总入口:<https://cloud.baidu.com/doc/WENXINWORKSHOP/index.html>
- 获取 access_token:
  `POST https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={AK}&client_secret={SK}`
  返回的 `access_token` 有效期 30 天,建议缓存(本项目已实现)。
- 多模态对话接口:
  `POST https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/{endpoint}?access_token=xxx`

---

## 二、本地运行

### 1. 注入 AK/SK(环境变量)

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

### 2. 启动后端(Spring Boot,模块 `rs`)

```bash
cd rs
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080`,接口:

- `POST /api/tongue/analyze`,`multipart/form-data`,字段名 `file`(图片文件)
- 成功返回 `TongueReport` JSON
- 失败返回 `{"error": "..."}`,HTTP 400 / 422 / 502 等

### 3. 启动前端(Vite + Vue3,模块 `vite`)

```bash
cd vite
npm install
npm run dev
```

前端 `vite.config.js` 已配置 `/api` 代理到 `http://localhost:8080`,
无需关心跨域。打开浏览器访问 Vite 输出的地址即可使用舌诊页面。

---

## 三、支持的图片格式与限制

- 格式:JPEG / PNG / WEBP(通过文件头 magic number 校验,不仅看后缀)
- 单图 ≤ 4MB
- 上传大小限制可在 `rs/src/main/resources/application.yml` 的
  `spring.servlet.multipart.max-file-size` 和
  `baidu.qianfan.max-image-bytes` 调整

---

## 四、隐私与合规

- 后端**不会落盘存储**用户上传的舌头图像,内存处理完即释放。
- 调用日志**不记录**图像 base64 与 AK/SK/access_token。
- 结果页固定展示免责声明,明确告知「仅供参考,不构成医疗诊断」。
