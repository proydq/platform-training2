<script setup>
defineProps({
  courses: { type: Array, default: () => [] },
  loading: Boolean
})
</script>

<template>
  <el-card class="mb-3">
    <h3>推荐课程</h3>
    <div v-if="loading" class="text-center">加载中...</div>
    <div v-else-if="courses.length === 0" class="text-center">暂无推荐</div>
    <ul v-else>
      <li v-for="c in courses" :key="c.courseId" class="course-item">
        {{ c.title }}
      </li>
    </ul>
  </el-card>
</template>

<style scoped>
.course-item {
  padding: 4px 0;
}
.mb-3 { margin-bottom: 1rem; }
</style>
