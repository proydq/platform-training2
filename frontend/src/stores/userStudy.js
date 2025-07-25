import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getStudyOverview, getMyCourses, getRecommendations } from '@/api/userStudy'

export const useUserStudyStore = defineStore('userStudy', () => {
  const overview = ref(null)
  const courses = ref([])
  const recommendations = ref([])
  const loading = ref(false)

  const fetchAll = async () => {
    loading.value = true
    try {
      const [o, c, r] = await Promise.all([
        getStudyOverview(),
        getMyCourses(),
        getRecommendations()
      ])
      overview.value = o.data || o
      courses.value = c.data || c
      recommendations.value = r.data || r
    } finally {
      loading.value = false
    }
  }

  return { overview, courses, recommendations, loading, fetchAll }
})
