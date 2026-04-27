<template>
  <div class="print-area sheet">
    <div class="sheet-head">
      <div class="hospital">{{ data.hospital?.name || '医院' }}</div>
      <div class="title">病 情 诊 断 单</div>
      <div class="meta">
        <span>编号：M-{{ pad(data.record?.id || data.appointment?.id) }}</span>
        <span>开具时间：{{ now }}</span>
      </div>
    </div>

    <div class="patient-info">
      <div><label>姓名：</label>{{ data.patient?.realName }}</div>
      <div><label>性别：</label>{{ data.patient?.gender || '-' }}</div>
      <div><label>电话：</label>{{ data.patient?.phone || '-' }}</div>
      <div><label>身份证：</label>{{ mask(data.patient?.idCard) }}</div>
      <div><label>科室：</label>{{ data.department?.name }}</div>
      <div><label>主治医师：</label>{{ data.doctor?.realName }}（{{ data.doctor?.title || '医师' }}）</div>
    </div>

    <section>
      <h4>主诉</h4>
      <p>{{ data.record?.chiefComplaint || '—' }}</p>
    </section>

    <section>
      <h4>诊断意见</h4>
      <p class="emphasis">{{ data.record?.diagnosis || '—' }}</p>
    </section>

    <section>
      <h4>处方</h4>
      <p class="prescription">{{ data.record?.prescription || '—' }}</p>
    </section>

    <section>
      <h4>医嘱与建议</h4>
      <p>{{ data.record?.advice || '—' }}</p>
    </section>

    <section v-if="data.record?.imageUrl">
      <h4>检查影像 / 附件</h4>
      <img :src="data.record.imageUrl" class="attach" alt="附件" />
    </section>

    <div class="sign">
      <div>
        <div class="muted">医生签名</div>
        <div class="sign-name">{{ data.doctor?.realName }}</div>
      </div>
      <div>
        <div class="muted">日期</div>
        <div>{{ formatDate(data.record?.createdAt) }}</div>
      </div>
      <div class="seal">医院公章</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({ data: { type: Object, required: true } })
const now = dayjs().format('YYYY-MM-DD HH:mm')
const pad = (n) => String(n || 0).padStart(6, '0')
const mask = (s) => s ? s.replace(/^(.{4}).+(.{4})$/, '$1********$2') : '-'
const formatDate = (s) => s ? dayjs(s).format('YYYY-MM-DD') : '-'
</script>

<style scoped>
.sheet { font-family: 'PingFang SC', 'Microsoft YaHei', serif; padding: 16px 4px; color: #1e293b; }
.sheet-head { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 10px; margin-bottom: 14px; }
.hospital { font-size: 16px; color: #475569; }
.title { font-size: 22px; font-weight: 700; letter-spacing: 6px; margin-top: 4px; }
.meta { display: flex; justify-content: space-between; margin-top: 10px; font-size: 12px; color: #64748b; }

.patient-info {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px 24px;
  font-size: 13px; padding: 12px 4px; border-bottom: 1px dashed #cbd5e1; margin-bottom: 14px;
}
.patient-info label { color: #64748b; }

section { margin-bottom: 14px; }
section h4 { font-size: 14px; margin: 0 0 4px; color: #0f172a; border-left: 4px solid #0ea5e9; padding-left: 6px; }
section p { margin: 0; line-height: 1.7; font-size: 13px; white-space: pre-wrap; word-break: break-all; }
section .emphasis { font-weight: 600; color: #0c4a6e; }
section .prescription { font-family: monospace; }

.attach { max-width: 320px; border: 1px solid #e2e8f0; border-radius: 4px; }
.sign {
  margin-top: 30px; display: grid; grid-template-columns: 1fr 1fr 1fr;
  align-items: end; font-size: 13px;
}
.sign .muted { color: #94a3b8; font-size: 12px; }
.sign-name {
  font-family: 'KaiTi', '楷体', serif; font-size: 22px; color: #b91c1c;
  border-bottom: 1px solid #cbd5e1; display: inline-block; padding: 0 18px 2px;
}
.seal { color: #b91c1c; font-weight: 700; border: 2px solid #b91c1c; padding: 24px 18px;
  border-radius: 50%; text-align: center; justify-self: end; opacity: .85; }
</style>
