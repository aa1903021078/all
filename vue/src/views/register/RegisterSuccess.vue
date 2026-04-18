<template>
  <div class="success-page">
    <div class="success-card">
      <div class="icon-box">
        <el-icon :size="80" color="#52c41a"><CircleCheck /></el-icon>
      </div>
      <h2>申请提交成功！</h2>
      <p class="desc-text">您的回收员申请已提交，工作人员将在1-3个工作日内完成审核</p>

      <div class="info-list">
        <div class="list-item">
          <span class="item-label">当前状态：</span>
          <span class="item-value status-waiting">待审核</span>
        </div>
        <div class="list-item">
          <span class="item-label">审核结果：</span>
          <span class="item-value">将通过短信通知您</span>
        </div>
      </div>

      <div class="tip-box">
        <p>您可以通过"查询审核状态"功能随时查看进度</p>
      </div>

      <el-button type="primary" class="primary-btn" @click="goToQuery">
        查询审核状态
      </el-button>
      <el-button link class="text-btn" @click="goToHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()

const goToQuery = () => {
  router.push('/collector-status-query')
}

const goToHome = () => {
  router.push('/')
}
</script>

<style>
/* 页面容器 */
.success-page {
  min-height: 100vh;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* 成功卡片 */
.success-card {
  background: white;
  border-radius: 20px;
  padding: 40px 32px;
  text-align: center;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  animation: slideUp 0.4s ease;
}

/* 图标 */
.icon-box {
  margin-bottom: 24px;
}

/* 标题 */
.success-card h2 {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
}

/* 描述文本 */
.desc-text {
  color: #8c8c8c;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 24px 0;
}

/* 信息列表 */
.info-list {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.list-item:last-child {
  margin-bottom: 0;
}

.item-label {
  color: #8c8c8c;
}

.item-value {
  color: #595959;
  font-weight: 500;
}

.status-waiting {
  color: #faad14;
}

/* 提示框 */
.tip-box {
  background: #f6ffed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 24px;
}

.tip-box p {
  margin: 0;
  font-size: 13px;
  color: #389e0d;
}

/* 主要按钮 */
.primary-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border: none;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
}

/* 文字按钮 */
.text-btn {
  color: #8c8c8c;
  font-size: 14px;
}

.text-btn:hover {
  color: #1890ff;
}

/* 动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>