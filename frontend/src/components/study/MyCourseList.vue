<script setup>
defineProps({
  courses: { type: Array, default: () => [] },
  loading: Boolean
})
</script>

<template>
  <el-card>
    <div v-if="loading" class="text-center">加载中...</div>
    <div v-else-if="courses.length === 0" class="text-center">暂无课程</div>
    <ul v-else>
      <li v-for="c in courses" :key="c.courseId" class="course-item">
        <span>{{ c.title }}</span>
        <span v-if="c.progress"> - {{ c.progress }}%</span>
      </li>
    </ul>
  </el-card>
</template>

<style scoped>
.course-item {
  padding: 4px 0;
}
</style>
