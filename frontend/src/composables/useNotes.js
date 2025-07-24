// composables/useNotes.js
import { ref, computed } from 'vue'

export function useNotes(currentTime) {
  const noteSearchKeyword = ref('')
  const showNoteModal = ref(false)
  const newNote = ref({
    timestamp: '04:23',
    tag: '重要概念',
    content: ''
  })

  const notes = ref([
    {
      id: 1,
      time: '2分钟前',
      location: '第1章第2节',
      timestamp: '02:15',
      content: '产品生命周期的四个阶段很重要，需要根据不同阶段制定不同的策略。引入期主要是市场教育，成长期要扩大份额。',
      createdAt: new Date().toISOString()
    },
    {
      id: 2,
      time: '5分钟前',
      location: '第1章第2节',
      timestamp: '05:30',
      content: '成熟期的特点：竞争激烈、价格战、利润率下降。这时候需要寻找新的增长点，可能需要产品创新或进入新市场。',
      createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      time: '昨天',
      location: '第1章第1节',
      timestamp: '08:45',
      content: '产品经理的核心职责是连接用户需求和技术实现，需要具备市场洞察力、沟通能力和项目管理能力。',
      createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
    }
  ])

  const filteredNotes = computed(() => {
    if (!noteSearchKeyword.value) return notes.value
    return notes.value.filter(note =>
      note.content.toLowerCase().includes(noteSearchKeyword.value.toLowerCase())
    )
  })

  const showAddNoteModal = () => {
    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }
    newNote.value.timestamp = formatTime(currentTime.value)
    showNoteModal.value = true
  }

  const hideNoteModal = () => {
    showNoteModal.value = false
    newNote.value.content = ''
  }

  const saveNote = (noteData) => {
    const note = {
      id: Date.now(),
      time: '刚刚',
      location: '第1章第2节',
      timestamp: noteData.timestamp || newNote.value.timestamp,
      content: noteData.content || newNote.value.content,
      createdAt: new Date().toISOString()
    }
    notes.value.unshift(note)
    hideNoteModal()
  }

  const editNote = (noteToEdit) => {
    // 设置编辑模式的笔记数据
    newNote.value = {
      ...noteToEdit,
      id: noteToEdit.id
    }
    showNoteModal.value = true
  }

  const deleteNote = (id) => {
    notes.value = notes.value.filter(note => note.id !== id)
  }

  const shareNote = (noteToShare) => {
    // 实现分享逻辑
    console.log('分享笔记:', noteToShare.content)
  }

  const jumpToTime = (timestamp) => {
    const [minutes, seconds] = timestamp.split(':').map(Number)
    currentTime.value = minutes * 60 + seconds
    console.log(`已跳转到 ${timestamp}`)
  }

  return {
    notes,
    noteSearchKeyword,
    showNoteModal,
    newNote,
    filteredNotes,
    showAddNoteModal,
    hideNoteModal,
    saveNote,
    editNote,
    deleteNote,
    shareNote,
    jumpToTime
  }
}
