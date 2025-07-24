<template>
  <div class="course-intro">
    <!-- 课程概览 -->
    <div class="intro-section">
      <div class="section-header">
        <h2 class="section-title">📖 课程概览</h2>
      </div>
      <div class="course-overview">
        <p class="course-description">
          {{ (lessonData && lessonData.description) || '本课程将为您详细介绍相关知识点，通过理论学习和实践操作帮助您快速掌握核心技能。' }}
        </p>

        <!-- 课程信息卡片 -->
        <div class="info-cards">
          <div class="info-card">
            <div class="card-icon">⏱️</div>
            <div class="card-content">
              <h4>课程时长</h4>
              <p>{{ (lessonData && lessonData.duration) || '45分钟' }}</p>
            </div>
          </div>
          <div class="info-card">
            <div class="card-icon">🎯</div>
            <div class="card-content">
              <h4>难度等级</h4>
              <p>{{ (lessonData && lessonData.difficulty) || '中级' }}</p>
            </div>
          </div>
          <div class="info-card">
            <div class="card-icon">👥</div>
            <div class="card-content">
              <h4>适用对象</h4>
              <p>{{ (lessonData && lessonData.target) || '全体员工' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习目标 -->
    <div class="intro-section">
      <div class="section-header">
        <h2 class="section-title">🎯 学习目标</h2>
      </div>
      <div class="learning-objectives">
        <ul class="objectives-list">
          <li v-for="(objective, index) in objectives" :key="index" class="objective-item">
            <span class="objective-number">{{ index + 1 }}</span>
            <span class="objective-text">{{ objective }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 学习建议 -->
    <div class="intro-section">
      <div class="section-header">
        <h2 class="section-title">💡 学习建议</h2>
      </div>
      <div class="study-tips">
        <div class="tip-card" v-for="(tip, index) in studyTips" :key="index">
          <div class="tip-icon">{{ tip.icon }}</div>
          <div class="tip-content">
            <h4 class="tip-title">{{ tip.title }}</h4>
            <p class="tip-description">{{ tip.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 课后思考 -->
    <div class="intro-section">
      <div class="section-header">
        <h2 class="section-title">🤔 课后思考</h2>
      </div>
      <div class="reflection-questions">
        <div class="question-card" v-for="(question, index) in reflectionQuestions" :key="index">
          <div class="question-number">Q{{ index + 1 }}</div>
          <p class="question-text">{{ question }}</p>
        </div>
      </div>
    </div>

    <!-- 延伸阅读 -->
    <div class="intro-section">
      <div class="section-header">
        <h2 class="section-title">📚 延伸阅读</h2>
      </div>
      <div class="extended-reading">
        <div class="reading-item" v-for="(item, index) in extendedReading" :key="index">
          <div class="reading-icon">{{ item.type === 'article' ? '📄' : item.type === 'video' ? '🎥' : '📖' }}</div>
          <div class="reading-content">
            <h4 class="reading-title">{{ item.title }}</h4>
            <p class="reading-description">{{ item.description }}</p>
            <a :href="item.url" class="reading-link" target="_blank">
              立即查看 <span class="link-arrow">→</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  lessonData: {
    type: Object,
    default: () => ({}) // 提供默认空对象，避免undefined错误
  }
})

// 学习目标 - 使用安全的访问方式
const objectives = computed(() => {
  if (props.lessonData && props.lessonData.objectives) {
    return props.lessonData.objectives
  }
  return [
    '掌握产品设计的基本原理和方法',
    '了解用户体验设计的核心概念',
    '学会使用相关工具进行产品原型设计',
    '能够独立完成产品需求分析和功能设计'
  ]
})

// 学习建议
const studyTips = [
  {
    icon: '📝',
    title: '记好笔记',
    description: '建议在重要知识点处记录笔记，方便后续复习和实践应用'
  },
  {
    icon: '🎬',
    title: '暂停思考',
    description: '遇到复杂概念时，暂停视频进行思考，确保理解透彻再继续'
  },
  {
    icon: '💬',
    title: '互动交流',
    description: '积极参与课程讨论，与同事分享学习心得和实践经验'
  },
  {
    icon: '🔄',
    title: '反复练习',
    description: '理论学习后要及时进行实践练习，加深对知识点的理解'
  }
]

// 课后思考题
const reflectionQuestions = [
  '在您的工作中，如何应用本课程学到的知识？',
  '您认为当前团队在相关方面还有哪些提升空间？',
  '学习完本课程后，您计划制定什么样的行动计划？'
]

// 延伸阅读
const extendedReading = [
  {
    type: 'article',
    title: '产品设计最佳实践指南',
    description: '深入了解产品设计的高级技巧和行业最佳实践',
    url: '#'
  },
  {
    type: 'video',
    title: '用户研究方法论',
    description: '通过视频学习更多用户研究的实用方法',
    url: '#'
  },
  {
    type: 'book',
    title: '产品经理必读书单',
    description: '推荐产品管理领域的经典书籍和最新读物',
    url: '#'
  }
]
</script>

<style scoped>
.course-intro {
  color: #333;
  line-height: 1.6;
  padding: 30px;
  max-height: 100%;
  overflow-y: auto;
}

/* 章节样式 */
.intro-section {
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #667eea;
  display: inline-block;
}

/* 课程概览 */
.course-description {
  font-size: 16px;
  color: #555;
  margin-bottom: 25px;
  line-height: 1.8;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.info-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.card-icon {
  font-size: 24px;
  margin-right: 15px;
}

.card-content h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  opacity: 0.9;
}

.card-content p {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

/* 学习目标 */
.objectives-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.objective-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
}

.objective-item:hover {
  background: #e3f2fd;
  transform: translateX(5px);
}

.objective-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 15px;
  flex-shrink: 0;
}

.objective-text {
  flex: 1;
  color: #2c3e50;
  font-weight: 500;
}

/* 学习建议 */
.study-tips {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.tip-card {
  display: flex;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.tip-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.tip-icon {
  font-size: 32px;
  margin-right: 15px;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.tip-description {
  margin: 0;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

/* 课后思考 */
.reflection-questions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.question-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border-radius: 12px;
  border-left: 4px solid #ffc107;
  transition: all 0.3s ease;
}

.question-card:hover {
  background: linear-gradient(135deg, #fff3cd, #ffffff);
  transform: translateX(5px);
}

.question-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #ffc107;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 15px;
  flex-shrink: 0;
}

.question-text {
  margin: 0;
  color: #2c3e50;
  font-weight: 500;
  line-height: 1.6;
}

/* 延伸阅读 */
.extended-reading {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reading-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.reading-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.reading-icon {
  font-size: 32px;
  margin-right: 20px;
  flex-shrink: 0;
}

.reading-content {
  flex: 1;
}

.reading-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.reading-description {
  margin: 0 0 12px 0;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

.reading-link {
  display: inline-flex;
  align-items: center;
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
}

.reading-link:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

.link-arrow {
  margin-left: 5px;
  transition: transform 0.3s ease;
}

.reading-link:hover .link-arrow {
  transform: translateX(3px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-intro {
    padding: 20px;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }

  .study-tips {
    grid-template-columns: 1fr;
  }

  .tip-card,
  .reading-item {
    flex-direction: column;
    text-align: center;
  }

  .tip-icon,
  .reading-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
