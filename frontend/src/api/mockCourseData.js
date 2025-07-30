// frontend/src/api/mockCourseData.js
// 模拟课程数据，用于开发测试

export const mockCourseDetail = {
  id: 'course-001',
  title: '产品基础培训',
  category: '产品培训',
  difficulty: '中级',
  totalDuration: '3小时',
  instructor: {
    name: '李经理',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor'
  },
  chapters: [
    {
      id: 'chapter-1',
      title: '第1章：产品概述',
      sortOrder: 1,
      completedLessons: 2,
      lessons: [
        {
          id: 'lesson-1-1',
          title: '1.1 什么是产品？',
          sortOrder: 1,
          contentType: 'video',
          duration: 8,
          completed: true,
          videoUrl: '/api/media/video/product-intro.mp4',
          description: '了解产品的基本概念和定义'
        },
        {
          id: 'lesson-1-2',
          title: '1.2 产品的生命周期',
          sortOrder: 2,
          contentType: 'document',
          duration: 12,
          completed: true,
          documentUrl: '/api/files/course/docs/product-lifecycle.pdf',
          documentName: '产品生命周期详解.pdf',
          description: '深入了解产品从诞生到退出的完整周期',
          supplementaryFiles: [
            {
              id: 'mat-1',
              name: '产品生命周期案例分析.xlsx',
              type: 'Excel文档',
              size: 2411520, // 2.3 MB
              url: '/api/files/materials/case-study.xlsx',
              updatedAt: '2025-01-22'
            },
            {
              id: 'mat-2',
              name: '产品分析模板.docx',
              type: 'Word文档',
              size: 466944, // 456 KB
              url: '/api/files/materials/template.docx',
              updatedAt: '2025-01-18'
            }
          ]
        },
        {
          id: 'lesson-1-3',
          title: '1.3 产品经理的角色',
          sortOrder: 3,
          contentType: 'video',
          duration: 15,
          completed: false,
          videoUrl: '/api/media/video/pm-role.mp4',
          description: '了解产品经理的职责和工作内容'
        }
      ]
    },
    {
      id: 'chapter-2',
      title: '第2章：市场分析',
      sortOrder: 2,
      completedLessons: 0,
      lessons: [
        {
          id: 'lesson-2-1',
          title: '2.1 市场调研方法',
          sortOrder: 1,
          contentType: 'document',
          duration: 18,
          completed: false,
          documentUrl: '/api/files/course/docs/market-research.pdf',
          documentName: '市场调研方法论.pdf',
          description: '掌握常用的市场调研方法和技巧'
        },
        {
          id: 'lesson-2-2',
          title: '2.2 竞品分析',
          sortOrder: 2,
          contentType: 'mixed',
          duration: 20,
          completed: false,
          videoUrl: '/api/media/video/competitor-analysis.mp4',
          documentUrl: '/api/files/course/docs/competitor-template.pdf',
          description: '学习如何进行有效的竞品分析',
          supplementaryFiles: [
            {
              id: 'mat-5',
              name: '竞品分析模板.xlsx',
              type: 'Excel文档',
              size: 1048576,
              url: '/api/files/materials/competitor-template.xlsx',
              updatedAt: '2025-01-20'
            }
          ]
        }
      ]
    }
  ]
}

// 模拟API响应的包装函数
export const wrapMockResponse = (data, delay = 500) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        success: true,
        code: 200,
        data: data,
        message: '操作成功'
      })
    }, delay)
  })
}
