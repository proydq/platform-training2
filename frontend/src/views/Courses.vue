<template>
  <div class="courses-page">
    <CourseRecommendation :courses="recommendations" :loading="loadingRec" />
    <StudyOverview :data="overview" :loading="loadingOverview" />
    <MyCourseList :courses="courses" :loading="loadingCourses" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import StudyOverview from '@/components/study/StudyOverview.vue'
import MyCourseList from '@/components/study/MyCourseList.vue'
import CourseRecommendation from '@/components/study/CourseRecommendation.vue'
import { getStudyOverview, getMyCourses, getRecommendations } from '@/api/userStudy'

const overview = ref({})
const courses = ref([])
const recommendations = ref([])
const loadingOverview = ref(false)
const loadingCourses = ref(false)
const loadingRec = ref(false)

const fetchData = async () => {
  loadingOverview.value = loadingCourses.value = loadingRec.value = true
  try {
    const [o, c, r] = await Promise.all([
      getStudyOverview(),
      getMyCourses(),
      getRecommendations()
    ])
    overview.value = o.data || o
    courses.value = c.data || c
    recommendations.value = r.data || r
  } catch (e) {
    console.error(e)
  } finally {
    loadingOverview.value = loadingCourses.value = loadingRec.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.courses-page {
  padding: 20px;
}
</style>
