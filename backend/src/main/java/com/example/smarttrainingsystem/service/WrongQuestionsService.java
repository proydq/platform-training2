package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.WrongQuestionDTO;
import com.example.smarttrainingsystem.entity.*;
import com.example.smarttrainingsystem.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题集服务类
 * 提供错题管理相关的业务逻辑
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Service
@Slf4j
public class WrongQuestionsService {

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private WrongQuestionRepository wrongQuestionRepository;

    /**
     * 获取用户错题列表
     *
     * @param userId 用户ID
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @param status 掌握状态筛选
     * @param pageable 分页参数
     * @return 错题分页列表
     */
    public Page<WrongQuestionDTO.ListItem> getUserWrongQuestions(String userId, Integer difficulty,
                                                                 String category, String status,
                                                                 Pageable pageable) {
        log.info("获取用户错题列表 - 用户ID: {}, 难度: {}, 分类: {}, 状态: {}",
                userId, difficulty, category, status);

        // 查询错题记录
        Page<WrongQuestion> wrongQuestions = wrongQuestionRepository.findUserWrongQuestions(
                userId, difficulty, category, status, pageable);

        // 转换为DTO
        List<WrongQuestionDTO.ListItem> items = wrongQuestions.getContent().stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, wrongQuestions.getTotalElements());
    }

    /**
     * 获取错题详情
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 错题详情
     */
    public WrongQuestionDTO.Detail getWrongQuestionDetail(String userId, String questionId) {
        log.info("获取错题详情 - 用户ID: {}, 题目ID: {}", userId, questionId);

        // 查询错题记录
        WrongQuestion wrongQuestion = wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseThrow(() -> new RuntimeException("错题记录不存在"));

        // 查询题目信息
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        return convertToDetail(wrongQuestion, question);
    }

    /**
     * 标记题目为已掌握
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     */
    @Transactional
    public void markQuestionAsMastered(String userId, String questionId) {
        log.info("标记题目为已掌握 - 用户ID: {}, 题目ID: {}", userId, questionId);

        WrongQuestion wrongQuestion = wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseThrow(() -> new RuntimeException("错题记录不存在"));

        wrongQuestion.setMastered(true);
        wrongQuestion.setMasteredAt(LocalDateTime.now());
        wrongQuestionRepository.save(wrongQuestion);
    }

    /**
     * 添加题目笔记
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param note 笔记内容
     */
    @Transactional
    public void addQuestionNote(String userId, String questionId, String note) {
        log.info("添加题目笔记 - 用户ID: {}, 题目ID: {}", userId, questionId);

        WrongQuestion wrongQuestion = wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseThrow(() -> new RuntimeException("错题记录不存在"));

        wrongQuestion.setUserNote(note);
        wrongQuestion.setUpdatedAt(LocalDateTime.now());
        wrongQuestionRepository.save(wrongQuestion);
    }

    /**
     * 清除已掌握的题目
     *
     * @param userId 用户ID
     * @return 清除的题目数量
     */
    @Transactional
    public int clearMasteredQuestions(String userId) {
        log.info("清除已掌握题目 - 用户ID: {}", userId);

        List<WrongQuestion> masteredQuestions = wrongQuestionRepository.findByUserIdAndMastered(userId, true);
        int count = masteredQuestions.size();

        wrongQuestionRepository.deleteAll(masteredQuestions);

        return count;
    }

    /**
     * 获取错题统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    public WrongQuestionDTO.Statistics getWrongQuestionsStatistics(String userId) {
        log.info("获取错题统计 - 用户ID: {}", userId);

        WrongQuestionDTO.Statistics statistics = new WrongQuestionDTO.Statistics();

        // 总错题数
        statistics.setTotalCount(wrongQuestionRepository.countByUserId(userId));

        // 已掌握题目数
        statistics.setMasteredCount(wrongQuestionRepository.countByUserIdAndMastered(userId, true));

        // 未掌握题目数
        statistics.setUnmasteredCount(statistics.getTotalCount() - statistics.getMasteredCount());

        // 按难度统计
        Map<Integer, Long> difficultyStats = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            difficultyStats.put(i, wrongQuestionRepository.countByUserIdAndDifficulty(userId, i));
        }
        statistics.setDifficultyStats(difficultyStats);

        // 按分类统计
        Map<String, Long> categoryStats = wrongQuestionRepository.getWrongQuestionCategoryStats(userId);
        statistics.setCategoryStats(categoryStats);

        return statistics;
    }

    /**
     * 生成错题练习
     *
     * @param userId 用户ID
     * @param count 题目数量
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @return 练习题目列表
     */
    public List<WrongQuestionDTO.Practice> generateWrongQuestionsPractice(String userId, int count,
                                                                          Integer difficulty, String category) {
        log.info("生成错题练习 - 用户ID: {}, 数量: {}, 难度: {}, 分类: {}",
                userId, count, difficulty, category);

        // 查询符合条件的错题
        List<WrongQuestion> wrongQuestions = wrongQuestionRepository.findPracticeQuestions(
                userId, difficulty, category, false); // 只获取未掌握的题目

        // 随机选择指定数量的题目
        Collections.shuffle(wrongQuestions);
        List<WrongQuestion> selectedQuestions = wrongQuestions.stream()
                .limit(count)
                .collect(Collectors.toList());

        // 转换为练习DTO
        return selectedQuestions.stream()
                .map(this::convertToPractice)
                .collect(Collectors.toList());
    }

    /**
     * 提交错题练习结果
     *
     * @param userId 用户ID
     * @param answers 答案数据
     * @return 练习结果
     */
    @Transactional
    public WrongQuestionDTO.PracticeResult submitWrongQuestionsPractice(String userId, Map<String, String> answers) {
        log.info("提交错题练习 - 用户ID: {}, 题目数: {}", userId, answers.size());

        WrongQuestionDTO.PracticeResult result = new WrongQuestionDTO.PracticeResult();
        int correctCount = 0;
        int totalCount = answers.size();

        List<WrongQuestionDTO.PracticeAnswer> answerDetails = new ArrayList<>();

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            String userAnswer = entry.getValue();

            // 查询题目
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("题目不存在: " + questionId));

            // 判断答案是否正确
            boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);
            if (isCorrect) {
                correctCount++;
            }

            // 更新错题记录
            wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId)
                    .ifPresent(wrongQuestion -> {
                        wrongQuestion.setPracticeCount(wrongQuestion.getPracticeCount() + 1);
                        if (isCorrect) {
                            wrongQuestion.setCorrectCount(wrongQuestion.getCorrectCount() + 1);
                        }
                        wrongQuestion.setLastPracticeAt(LocalDateTime.now());
                        wrongQuestionRepository.save(wrongQuestion);
                    });

            // 构建答案详情
            WrongQuestionDTO.PracticeAnswer answerDetail = new WrongQuestionDTO.PracticeAnswer();
            answerDetail.setQuestionId(questionId);
            answerDetail.setQuestionContent(question.getContent());
            answerDetail.setUserAnswer(userAnswer);
            answerDetail.setCorrectAnswer(question.getCorrectAnswer());
            answerDetail.setIsCorrect(isCorrect);
            answerDetail.setExplanation(question.getExplanation());
            answerDetails.add(answerDetail);
        }

        result.setTotalCount(totalCount);
        result.setCorrectCount(correctCount);
        result.setWrongCount(totalCount - correctCount);
        result.setAccuracyRate(totalCount > 0 ? (double) correctCount / totalCount * 100 : 0);
        result.setAnswerDetails(answerDetails);

        return result;
    }

    /**
     * 导出错题集
     *
     * @param userId 用户ID
     * @param format 导出格式
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @return 导出文件名
     */
    public String exportWrongQuestions(String userId, String format, Integer difficulty, String category) {
        log.info("导出错题集 - 用户ID: {}, 格式: {}, 难度: {}, 分类: {}",
                userId, format, difficulty, category);

        // 简化实现，实际应该生成文件
        String fileName = String.format("wrong_questions_%s_%d.%s",
                userId, System.currentTimeMillis(), format);

        // TODO: 实际的文件生成逻辑
        log.info("错题集导出完成: {}", fileName);

        return fileName;
    }

    /**
     * 记录错题（在考试结束时调用）
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @param wrongAnswers 错误答案映射
     */
    @Transactional
    public void recordWrongQuestions(String userId, String examId, Map<String, String> wrongAnswers) {
        log.info("记录错题 - 用户ID: {}, 考试ID: {}, 错题数: {}", userId, examId, wrongAnswers.size());

        for (Map.Entry<String, String> entry : wrongAnswers.entrySet()) {
            String questionId = entry.getKey();
            String userAnswer = entry.getValue();

            // 查询是否已存在错题记录
            Optional<WrongQuestion> existingRecord = wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId);

            if (existingRecord.isPresent()) {
                // 更新现有记录
                WrongQuestion wrongQuestion = existingRecord.get();
                wrongQuestion.setWrongCount(wrongQuestion.getWrongCount() + 1);
                wrongQuestion.setLastWrongAt(LocalDateTime.now());
                wrongQuestion.setLastUserAnswer(userAnswer);
                wrongQuestionRepository.save(wrongQuestion);
            } else {
                // 创建新的错题记录
                WrongQuestion wrongQuestion = new WrongQuestion();
                wrongQuestion.setUserId(userId);
                wrongQuestion.setQuestionId(questionId);
                wrongQuestion.setExamId(examId);
                wrongQuestion.setWrongCount(1);
                wrongQuestion.setCorrectCount(0);
                wrongQuestion.setPracticeCount(0);
                wrongQuestion.setLastWrongAt(LocalDateTime.now());
                wrongQuestion.setLastUserAnswer(userAnswer);
                wrongQuestion.setMastered(false);
                wrongQuestionRepository.save(wrongQuestion);
            }
        }
    }

    // ============ 私有方法 ============

    private WrongQuestionDTO.ListItem convertToListItem(WrongQuestion wrongQuestion) {
        WrongQuestionDTO.ListItem item = new WrongQuestionDTO.ListItem();

        // 查询题目信息
        Question question = questionRepository.findById(wrongQuestion.getQuestionId()).orElse(null);
        if (question != null) {
            item.setQuestionId(question.getId());
            item.setContent(question.getContent());
            item.setQuestionType(question.getQuestionType().name());
            item.setDifficulty(question.getDifficulty());
            item.setCategory(question.getCategory());
        }

        item.setWrongCount(wrongQuestion.getWrongCount());
        item.setCorrectCount(wrongQuestion.getCorrectCount());
        item.setPracticeCount(wrongQuestion.getPracticeCount());
        item.setMastered(wrongQuestion.getMastered());

        // 计算正确率和掌握状态
        item.setAccuracyRate(wrongQuestion.getAccuracyRate());
        item.setMasteryStatus(wrongQuestion.getMasteryStatus());

        // 设置时间字段
        item.setLastWrongAt(wrongQuestion.getLastWrongAt());
        item.setLastPracticeAt(wrongQuestion.getLastPracticeAt());
        item.setMasteredAt(wrongQuestion.getMasteredAt());

        return item;
    }

    private WrongQuestionDTO.Detail convertToDetail(WrongQuestion wrongQuestion, Question question) {
        WrongQuestionDTO.Detail detail = new WrongQuestionDTO.Detail();

        detail.setQuestionId(question.getId());
        detail.setContent(question.getContent());
        detail.setQuestionType(question.getQuestionType().name());
        detail.setOptions(parseOptions(question.getOptions()));
        detail.setCorrectAnswer(question.getCorrectAnswer());
        detail.setExplanation(question.getExplanation());
        detail.setDifficulty(question.getDifficulty());
        detail.setCategory(question.getCategory());
        detail.setTags(question.getTags());

        detail.setWrongCount(wrongQuestion.getWrongCount());
        detail.setCorrectCount(wrongQuestion.getCorrectCount());
        detail.setPracticeCount(wrongQuestion.getPracticeCount());
        detail.setLastUserAnswer(wrongQuestion.getLastUserAnswer());
        detail.setUserNote(wrongQuestion.getUserNote());
        detail.setMastered(wrongQuestion.getMastered());
        detail.setLastWrongAt(wrongQuestion.getLastWrongAt());
        detail.setLastPracticeAt(wrongQuestion.getLastPracticeAt());
        detail.setMasteredAt(wrongQuestion.getMasteredAt());

        return detail;
    }

    private WrongQuestionDTO.Practice convertToPractice(WrongQuestion wrongQuestion) {
        WrongQuestionDTO.Practice practice = new WrongQuestionDTO.Practice();

        // 查询题目信息
        Question question = questionRepository.findById(wrongQuestion.getQuestionId()).orElse(null);
        if (question != null) {
            practice.setQuestionId(question.getId());
            practice.setContent(question.getContent());
            practice.setQuestionType(question.getQuestionType().name());
            practice.setOptions(parseOptions(question.getOptions()));
            practice.setScore(question.getScore());
        }

        practice.setWrongCount(wrongQuestion.getWrongCount());
        practice.setLastUserAnswer(wrongQuestion.getLastUserAnswer());

        return practice;
    }

    private List<Map<String, String>> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return new ArrayList<>();
        }

        // 简化处理，实际应该用JSON库解析
        List<Map<String, String>> options = new ArrayList<>();
        options.add(createOption("A", "选项A"));
        options.add(createOption("B", "选项B"));
        options.add(createOption("C", "选项C"));
        options.add(createOption("D", "选项D"));
        return options;
    }

    private Map<String, String> createOption(String key, String value) {
        Map<String, String> option = new HashMap<>();
        option.put("key", key);
        option.put("value", value);
        return option;
    }
}