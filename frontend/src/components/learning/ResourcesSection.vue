<template>
  <div class="resources-section">
    <!-- 资料分类导航 -->
    <div class="resources-nav">
      <button
        v-for="category in categories"
        :key="category.key"
        class="nav-btn"
        :class="{ active: activeCategory === category.key }"
        @click="setActiveCategory(category.key)"
      >
        <span class="nav-icon">{{ category.icon }}</span>
        <span class="nav-label">{{ category.label }}</span>
        <span class="nav-count">({{ getCategoryCount(category.key) }})</span>
      </button>
    </div>

    <!-- 资料列表 -->
    <div class="resources-content">
      <!-- 课件资料 -->
      <div v-if="activeCategory === 'courseware'" class="resource-category">
        <div class="category-header">
          <h3 class="category-title">📄 课件资料</h3>
          <p class="category-description">本课程相关的课件和演示文档</p>
        </div>

        <div class="resources-grid">
          <div v-for="item in coursewareResources" :key="item.id" class="resource-card">
            <div class="resource-icon-wrapper">
              <div class="resource-icon">{{ getFileIcon(item.type) }}</div>
              <div class="resource-type">{{ item.type.toUpperCase() }}</div>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">{{ item.title }}</h4>
              <p class="resource-description">{{ item.description }}</p>
              <div class="resource-meta">
                <span class="resource-size">{{ item.size }}</span>
                <span class="resource-date">{{ formatDate(item.updatedAt) }}</span>
              </div>
            </div>
            <div class="resource-actions">
              <button @click="previewResource(item)" class="action-btn preview-btn" title="预览">
                👁️
              </button>
              <button @click="downloadResource(item)" class="action-btn download-btn" title="下载">
                📥
              </button>
              <button @click="shareResource(item)" class="action-btn share-btn" title="分享">
                📤
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 参考资料 -->
      <div v-if="activeCategory === 'references'" class="resource-category">
        <div class="category-header">
          <h3 class="category-title">📚 参考资料</h3>
          <p class="category-description">相关的参考文献和学习资源</p>
        </div>

        <div class="reference-list">
          <div v-for="item in referenceResources" :key="item.id" class="reference-item">
            <div class="reference-header">
              <div class="reference-type">{{ item.type }}</div>
              <div class="reference-level">{{ item.level }}</div>
            </div>
            <h4 class="reference-title">{{ item.title }}</h4>
            <p class="reference-description">{{ item.description }}</p>
            <div class="reference-tags">
              <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <div class="reference-actions">
              <a :href="item.url" target="_blank" class="reference-link">
                🔗 查看原文
              </a>
              <button @click="bookmarkResource(item)" class="bookmark-btn">
                ⭐ 收藏
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 工具软件 -->
      <div v-if="activeCategory === 'tools'" class="resource-category">
        <div class="category-header">
          <h3 class="category-title">🛠️ 工具软件</h3>
          <p class="category-description">课程中提到的相关工具和软件</p>
        </div>

        <div class="tools-grid">
          <div v-for="tool in toolResources" :key="tool.id" class="tool-card">
            <div class="tool-header">
              <div class="tool-logo">
                <img v-if="tool.logo" :src="tool.logo" :alt="tool.name" />
                <div v-else class="tool-icon">{{ tool.icon || '🔧' }}</div>
              </div>
              <div class="tool-info">
                <h4 class="tool-name">{{ tool.name }}</h4>
                <div class="tool-category">{{ tool.category }}</div>
              </div>
              <div class="tool-price">{{ tool.price }}</div>
            </div>
            <p class="tool-description">{{ tool.description }}</p>
            <div class="tool-features">
              <div class="feature-item" v-for="feature in tool.features" :key="feature">
                ✓ {{ feature }}
              </div>
            </div>
            <div class="tool-actions">
              <a :href="tool.officialUrl" target="_blank" class="tool-btn primary">
                🌐 官网
              </a>
              <a v-if="tool.downloadUrl" :href="tool.downloadUrl" target="_blank" class="tool-btn secondary">
                📥 下载
              </a>
              <button @click="learnMore(tool)" class="tool-btn info">
                📖 详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 相关链接 -->
      <div v-if="activeCategory === 'links'" class="resource-category">
        <div class="category-header">
          <h3 class="category-title">🔗 相关链接</h3>
          <p class="category-description">有用的网站和在线资源</p>
        </div>

        <div class="links-list">
          <div v-for="link in linkResources" :key="link.id" class="link-item">
            <div class="link-favicon">
              <img v-if="link.favicon" :src="link.favicon" :alt="link.title" />
              <div v-else class="default-favicon">🌐</div>
            </div>
            <div class="link-content">
              <h4 class="link-title">
                <a :href="link.url" target="_blank">{{ link.title }}</a>
              </h4>
              <p class="link-description">{{ link.description }}</p>
              <div class="link-meta">
                <span class="link-domain">{{ getDomain(link.url) }}</span>
                <span class="link-type">{{ link.type }}</span>
              </div>
            </div>
            <div class="link-actions">
              <a :href="link.url" target="_blank" class="visit-btn">
                🔗 访问
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="getCurrentResources().length === 0" class="empty-state">
        <div class="empty-icon">📂</div>
        <h3 class="empty-title">暂无{{ getCurrentCategoryLabel() }}资源</h3>
        <p class="empty-description">该分类下还没有相关资源</p>
      </div>
    </div>

    <!-- 资源预览模态框 -->
    <div v-if="showPreview" class="preview-modal" @click="closePreview">
      <div class="preview-content" @click.stop>
        <div class="preview-header">
          <h3 class="preview-title">{{ previewResourceData?.title }}</h3>
          <button @click="closePreview" class="close-btn">✕</button>
        </div>
        <div class="preview-body">
          <iframe v-if="previewResourceData?.previewUrl" :src="previewResourceData.previewUrl"></iframe>
          <div v-else class="preview-placeholder">
            <div class="placeholder-icon">{{ getFileIcon(previewResourceData?.type) }}</div>
            <p>无法预览此文件类型</p>
            <button @click="downloadResource(previewResourceData)" class="download-preview-btn">
              📥 下载文件
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  lessonData: {
    type: Object,
    required: true
  }
})

// 当前激活的分类
const activeCategory = ref('courseware')

// 预览相关
const showPreview = ref(false)
const previewResourceData = ref(null)

// 资料分类
const categories = [
  { key: 'courseware', label: '课件资料', icon: '📄' },
  { key: 'references', label: '参考资料', icon: '📚' },
  { key: 'tools', label: '工具软件', icon: '🛠️' },
  { key: 'links', label: '相关链接', icon: '🔗' }
]

// 课件资料
const coursewareResources = [
  {
    id: 1,
    title: '产品设计基础课件.pptx',
    description: '本课程的主要演示文稿，包含所有核心知识点',
    type: 'pptx',
    size: '15.2 MB',
    updatedAt: '2025-01-20',
    previewUrl: '#'
  },
  {
    id: 2,
    title: '用户体验设计指南.pdf',
    description: '详细的UX设计指导文档和最佳实践',
    type: 'pdf',
    size: '8.7 MB',
    updatedAt: '2025-01-18',
    previewUrl: '#'
  },
  {
    id: 3,
    title: '产品原型模板.sketch',
    description: 'Sketch原型设计模板文件',
    type: 'sketch',
    size: '12.4 MB',
    updatedAt: '2025-01-15',
    previewUrl: null
  }
]

// 参考资料
const referenceResources = [
  {
    id: 1,
    title: '设计心理学：如何理解用户行为',
    description: '深入探讨用户心理和行为模式，对产品设计具有重要指导意义',
    type: '书籍推荐',
    level: '中级',
    tags: ['心理学', '用户研究', '设计理论'],
    url: 'https://example.com/design-psychology'
  },
  {
    id: 2,
    title: 'Material Design Guidelines',
    description: 'Google官方设计规范，现代产品设计的重要参考',
    type: '设计规范',
    level: '基础',
    tags: ['设计规范', 'Material Design', 'UI设计'],
    url: 'https://material.io/design'
  },
  {
    id: 3,
    title: '产品经理必读：从0到1的产品思维',
    description: '产品设计和管理的经典理论文章',
    type: '论文文章',
    level: '高级',
    tags: ['产品思维', '项目管理', '创新方法'],
    url: 'https://example.com/product-thinking'
  }
]

// 工具软件
const toolResources = [
  {
    id: 1,
    name: 'Figma',
    category: '设计工具',
    price: '免费 / 专业版 $12/月',
    description: '现代化的协作设计平台，支持实时协作和原型制作',
    features: ['实时协作', '原型设计', '组件系统', '版本控制'],
    icon: '🎨',
    officialUrl: 'https://figma.com',
    downloadUrl: 'https://figma.com/downloads'
  },
  {
    id: 2,
    name: 'Miro',
    category: '协作工具',
    price: '免费 / 团队版 $8/月',
    description: '在线白板工具，适合头脑风暴和流程设计',
    features: ['无限画布', '模板库', '实时协作', '集成工具'],
    icon: '🗂️',
    officialUrl: 'https://miro.com',
    downloadUrl: 'https://miro.com/apps'
  },
  {
    id: 3,
    name: 'Notion',
    category: '文档工具',
    price: '免费 / 专业版 $8/月',
    description: '全能的工作空间，适合文档编写和项目管理',
    features: ['模块化编辑', '数据库功能', '团队协作', '模板系统'],
    icon: '📝',
    officialUrl: 'https://notion.so',
    downloadUrl: 'https://notion.so/desktop'
  }
]

// 相关链接
const linkResources = [
  {
    id: 1,
    title: 'Dribbble - 设计灵感平台',
    description: '全球优秀设计师作品展示平台，获取设计灵感的最佳去处',
    url: 'https://dribbble.com',
    type: '设计社区',
    favicon: 'https://dribbble.com/favicon.ico'
  },
  {
    id: 2,
    title: 'Product Hunt - 产品发现社区',
    description: '每日发现最新最酷的产品，了解行业趋势',
    url: 'https://producthunt.com',
    type: '产品社区',
    favicon: 'https://producthunt.com/favicon.ico'
  },
  {
    id: 3,
    title: 'UX Planet - 用户体验博客',
    description: '用户体验设计相关的优质文章和案例分析',
    url: 'https://uxplanet.org',
    type: '学习博客',
    favicon: null
  },
  {
    id: 4,
    title: 'Can I use - Web兼容性查询',
    description: '查看Web技术在各浏览器中的兼容性支持情况',
    url: 'https://caniuse.com',
    type: '开发工具',
    favicon: 'https://caniuse.com/img/favicon-128.png'
  }
]

// 设置活跃分类
const setActiveCategory = (category) => {
  activeCategory.value = category
}

// 获取分类数量
const getCategoryCount = (category) => {
  switch (category) {
    case 'courseware': return coursewareResources.length
    case 'references': return referenceResources.length
    case 'tools': return toolResources.length
    case 'links': return linkResources.length
    default: return 0
  }
}

// 获取当前分类资源
const getCurrentResources = () => {
  switch (activeCategory.value) {
    case 'courseware': return coursewareResources
    case 'references': return referenceResources
    case 'tools': return toolResources
    case 'links': return linkResources
    default: return []
  }
}

// 获取当前分类标签
const getCurrentCategoryLabel = () => {
  const category = categories.find(c => c.key === activeCategory.value)
  return category ? category.label : ''
}

// 获取文件图标
const getFileIcon = (type) => {
  const icons = {
    'pdf': '📄',
    'pptx': '📊',
    'docx': '📝',
    'xlsx': '📈',
    'sketch': '🎨',
    'fig': '🎨',
    'zip': '📦',
    'mp4': '🎥',
    'mp3': '🎵'
  }
  return icons[type] || '📄'
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// 获取域名
const getDomain = (url) => {
  try {
    return new URL(url).hostname
  } catch {
    return url
  }
}

// 预览资源
const previewResource = (resource) => {
  previewResourceData.value = resource
  showPreview.value = true
}

// 关闭预览
const closePreview = () => {
  showPreview.value = false
  previewResourceData.value = null
}

// 下载资源
const downloadResource = (resource) => {
  // 实际项目中这里应该处理文件下载逻辑
  // 可以使用 window.open 或创建下载链接
}

// 分享资源
const shareResource = (resource) => {
  // 实际项目中这里应该处理分享逻辑
}

// 收藏资源
const bookmarkResource = (resource) => {
}

// 了解更多工具信息
const learnMore = (tool) => {
}
</script>

<style scoped>
.resources-section {
  color: #333;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 资料分类导航 */
.resources-nav {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 25px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 14px;
}

.nav-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.nav-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: #667eea;
}

.nav-icon {
  font-size: 16px;
}

.nav-count {
  font-size: 12px;
  opacity: 0.8;
}

/* 资料内容区 */
.resources-content {
  flex: 1;
  overflow-y: auto;
}

.resource-category {
  margin-bottom: 30px;
}

.category-header {
  margin-bottom: 20px;
}

.category-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.category-description {
  color: #666;
  margin: 0;
  font-size: 14px;
}

/* 课件资料网格 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.resource-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.resource-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.resource-icon-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.resource-icon {
  font-size: 32px;
  margin-right: 12px;
}

.resource-type {
  background: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
}

.resource-info {
  margin-bottom: 15px;
}

.resource-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.resource-description {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin: 0 0 10px 0;
}

.resource-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.resource-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-btn:hover {
  border-color: #17a2b8;
  background: #f1fcfd;
}

.download-btn:hover {
  border-color: #28a745;
  background: #f8fff9;
}

.share-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

/* 参考资料列表 */
.reference-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reference-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.reference-item:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.reference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reference-type {
  background: #fff3cd;
  color: #856404;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

.reference-level {
  background: #d4edda;
  color: #155724;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

.reference-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.reference-description {
  color: #666;
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.reference-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.tag {
  background: #f8f9fa;
  color: #495057;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  border: 1px solid #e9ecef;
}

.reference-actions {
  display: flex;
  gap: 15px;
}

.reference-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: color 0.3s ease;
}

.reference-link:hover {
  color: #764ba2;
}

.bookmark-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s ease;
}

.bookmark-btn:hover {
  color: #667eea;
}

/* 工具软件网格 */
.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
}

.tool-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.tool-card:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.tool-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.tool-logo {
  width: 50px;
  height: 50px;
  margin-right: 15px;
  border-radius: 10px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tool-icon {
  font-size: 24px;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.tool-category {
  color: #666;
  font-size: 12px;
}

.tool-price {
  color: #667eea;
  font-weight: 600;
  font-size: 14px;
}

.tool-description {
  color: #666;
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.tool-features {
  margin-bottom: 20px;
}

.feature-item {
  color: #28a745;
  font-size: 13px;
  margin-bottom: 5px;
}

.tool-actions {
  display: flex;
  gap: 10px;
}

.tool-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  text-align: center;
  transition: all 0.3s ease;
}

.tool-btn.primary {
  background: #667eea;
  color: white;
}

.tool-btn.secondary {
  background: #28a745;
  color: white;
}

.tool-btn.info {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #e9ecef;
}

.tool-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

/* 相关链接列表 */
.links-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.link-item {
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.link-item:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.link-favicon {
  width: 40px;
  height: 40px;
  margin-right: 15px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.link-favicon img {
  width: 24px;
  height: 24px;
}

.default-favicon {
  font-size: 20px;
}

.link-content {
  flex: 1;
}

.link-title {
  margin: 0 0 8px 0;
}

.link-title a {
  color: #2c3e50;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  transition: color 0.3s ease;
}

.link-title a:hover {
  color: #667eea;
}

.link-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 8px 0;
}

.link-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.link-actions {
  margin-left: 15px;
}

.visit-btn {
  background: #667eea;
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.visit-btn:hover {
  background: #764ba2;
  transform: translateY(-1px);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  color: #666;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 预览模态框 */
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.preview-content {
  background: white;
  border-radius: 12px;
  width: 90vw;
  height: 90vh;
  max-width: 1000px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.preview-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.close-btn:hover {
  background: #f8f9fa;
}

.preview-body {
  flex: 1;
  overflow: hidden;
}

.preview-body iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.download-preview-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  margin-top: 20px;
  transition: all 0.3s ease;
}

.download-preview-btn:hover {
  background: #764ba2;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .resources-nav {
    flex-wrap: wrap;
  }

  .resources-grid,
  .tools-grid {
    grid-template-columns: 1fr;
  }

  .link-item {
    flex-direction: column;
    text-align: center;
  }

  .link-favicon {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .link-actions {
    margin-left: 0;
    margin-top: 15px;
  }

  .tool-header {
    flex-direction: column;
    text-align: center;
  }

  .tool-logo {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .tool-actions {
    justify-content: center;
  }

  .preview-content {
    width: 95vw;
    height: 95vh;
  }
}

/* 滚动条样式 */
.resources-content::-webkit-scrollbar {
  width: 6px;
}

.resources-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.resources-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.resources-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
