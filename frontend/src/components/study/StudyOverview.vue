<script setup>
import { formatDuration } from '@/utils/formatUtils'

defineProps({
  data: { type: Object, default: () => ({}) },
  loading: Boolean
})
</script>

<template>
  <el-card>
    <div v-if="loading" class="text-center">加载中...</div>
    <div v-else class="overview-wrap">
      <div class="item">
        <div class="num">{{ data.completed || 0 }}</div>
        <div class="label">已完成</div>
      </div>
      <div class="item">
        <div class="num">{{ data.inProgress || 0 }}</div>
        <div class="label">进行中</div>
      </div>
      <div class="item">
        <div class="num">{{ data.notStarted || 0 }}</div>
        <div class="label">待开始</div>
      </div>
      <div class="item">
        <div class="num">{{ formatDuration(data.totalTime || 0) }}</div>
        <div class="label">学习时长</div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.overview-wrap {
  display: flex;
  justify-content: space-between;
}
.item {
  text-align: center;
  flex: 1;
}
.num {
  font-size: 20px;
  font-weight: bold;
}
.label {
  color: #666;
}
</style>
