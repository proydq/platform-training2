// frontend/src/utils/authImage.js
/**
 * 认证图片工具函数
 * 处理需要JWT认证的图片加载
 */

/**
 * 获取认证token
 */
const getAuthToken = () => {
    return localStorage.getItem('token')
  }
  
  /**
   * 通过认证获取图片Blob URL
   * @param {string} imageUrl - 图片URL
   * @returns {Promise<string>} Blob URL
   */
  export const getAuthImageUrl = async (imageUrl) => {
    if (!imageUrl) {
      return null
    }
  
    // 如果不是需要认证的图片，直接返回
    if (!imageUrl.includes('/api/files/')) {
      return imageUrl
    }
  
    try {
      const token = getAuthToken()
      if (!token) {
        return null
      }
  
      const response = await fetch(imageUrl, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
  
      if (!response.ok) {
        return null
      }
  
      const blob = await response.blob()
      const blobUrl = URL.createObjectURL(blob)
      
      // 设置一个清理定时器，防止内存泄漏
      setTimeout(() => {
        URL.revokeObjectURL(blobUrl)
      }, 5 * 60 * 1000) // 5分钟后清理
  
      return blobUrl
    } catch (error) {
      return null
    }
  }
  
  /**
   * 批量加载认证图片
   * @param {Array<string>} imageUrls - 图片URL数组
   * @returns {Promise<Array<string|null>>} Blob URL数组
   */
  export const getAuthImageUrls = async (imageUrls) => {
    const promises = imageUrls.map(url => getAuthImageUrl(url))
    return Promise.all(promises)
  }
  
  /**
   * 清理Blob URL
   * @param {string} blobUrl - 要清理的Blob URL
   */
  export const revokeAuthImageUrl = (blobUrl) => {
    if (blobUrl && blobUrl.startsWith('blob:')) {
      URL.revokeObjectURL(blobUrl)
    }
  }
  
  /**
   * 检查是否需要认证的图片
   * @param {string} imageUrl - 图片URL
   * @returns {boolean}
   */
  export const isAuthImage = (imageUrl) => {
    return imageUrl && imageUrl.includes('/api/files/')
  }