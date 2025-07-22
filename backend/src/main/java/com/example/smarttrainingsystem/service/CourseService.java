package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.CourseDTO;
import com.example.smarttrainingsystem.dto.CourseChapterDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.entity.CourseChapter;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.CourseChapterRepository;
import com.example.smarttrainingsystem.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程业务服务类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseChapterRepository courseChapterRepository;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建课程
     */
    @Transactional
    public CourseDTO.Response createCourse(CourseDTO.CreateRequest request, String userId) {
        log.info("创建课程: title={}, userId={}", request.getTitle(), userId);

        Course course = new Course();
        BeanUtils.copyProperties(request, course);

        course.setInstructorId(userId);
        course.setCreateBy(userId);
        course.setCreateTime(System.currentTimeMillis());

        // 🔧 处理学习资料信息
        handleCourseMaterials(course, request);

        // 🔧 处理视频资料信息
        handleCourseVideos(course, request);

        Course savedCourse = courseRepository.save(course);

        // 创建章节（如果有）
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            createChaptersForCourse(savedCourse.getId(), request.getChapters());
        }

        log.info("课程创建成功: courseId={}", savedCourse.getId());
        return convertToResponse(savedCourse);
    }

    /**
     * 更新课程
     */
    @Transactional
    public CourseDTO.Response updateCourse(String courseId, CourseDTO.UpdateRequest request, String userId) {
        log.info("更新课程: courseId={}, userId={}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 权限检查
        if (!course.getInstructorId().equals(userId)) {
            throw new BusinessException(2002, "无权限修改此课程");
        }

        // 更新基本信息
        BeanUtils.copyProperties(request, course, "id", "createTime", "createBy", "instructorId");
        course.setUpdateTime(System.currentTimeMillis());
        course.setUpdateBy(userId);

        // 🔧 处理学习资料信息
        handleCourseMaterials(course, request);

        // 🔧 处理视频资料信息
        handleCourseVideos(course, request);

        Course savedCourse = courseRepository.save(course);

        // 更新章节信息
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            updateChaptersForCourse(savedCourse.getId(), request.getChapters());
        }

        log.info("课程更新成功: courseId={}", savedCourse.getId());
        return convertToResponse(savedCourse);
    }

    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(String courseId, String userId) {
        log.info("删除课程: courseId={}, userId={}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 权限检查
        if (!course.getInstructorId().equals(userId)) {
            throw new BusinessException(2002, "无权限删除此课程");
        }

        // 检查课程状态
        if (course.isPublished()) {
            throw new BusinessException(2003, "已发布的课程不能直接删除，请先下架");
        }

        courseRepository.delete(course);
        log.info("课程删除成功: courseId={}", courseId);
    }

    /**
     * 获取课程详情
     */
    public CourseDTO.Response getCourseDetail(String courseId) {
        log.info("获取课程详情: courseId={}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 增加浏览次数
        course.incrementViewCount();
        courseRepository.save(course);

        return convertToResponse(course);
    }

    /**
     * 获取课程列表
     */
    public Page<CourseDTO.ListItem> getCourseList(CourseDTO.SearchRequest searchRequest, Pageable pageable) {
        log.info("获取课程列表: searchRequest={}", searchRequest);

        Specification<Course> spec = buildSearchSpecification(searchRequest);
        Sort sort = buildSort(searchRequest.getSortBy(), searchRequest.getSortOrder());

        // 🔧 修正：创建新的 PageRequest 而不是使用 withSort
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Course> coursePage = courseRepository.findAll(spec, sortedPageable);
        return coursePage.map(this::convertToListItem);
    }

    // ==================== 课程状态管理 ====================

    /**
     * 发布课程
     */
    @Transactional
    public CourseDTO.Response publishCourse(String courseId, String userId) {
        log.info("发布课程: courseId={}, userId={}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 权限检查
        if (!course.getInstructorId().equals(userId)) {
            throw new BusinessException(2002, "无权限发布此课程");
        }

        // 验证课程是否可以发布
        validateCourseForPublish(course);

        course.publish();
        Course savedCourse = courseRepository.save(course);

        log.info("课程发布成功: courseId={}", courseId);
        return convertToResponse(savedCourse);
    }

    /**
     * 下架课程
     */
    @Transactional
    public CourseDTO.Response unpublishCourse(String courseId, String userId) {
        log.info("下架课程: courseId={}, userId={}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 权限检查
        if (!course.getInstructorId().equals(userId)) {
            throw new BusinessException(2002, "无权限下架此课程");
        }

        course.unpublish();
        Course savedCourse = courseRepository.save(course);

        log.info("课程下架成功: courseId={}", courseId);
        return convertToResponse(savedCourse);
    }

    // ==================== 课程搜索和筛选 ====================

    /**
     * 搜索课程
     */
    public Page<CourseDTO.ListItem> searchCourses(CourseDTO.SearchRequest searchRequest, Pageable pageable) {
        log.info("搜索课程: keyword={}, category={}", searchRequest.getKeyword(), searchRequest.getCategory());

        Specification<Course> spec = buildSearchSpecification(searchRequest);
        Sort sort = buildSort(searchRequest.getSortBy(), searchRequest.getSortOrder());

        // 🔧 修正：创建新的 PageRequest 而不是使用 withSort
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Course> coursePage = courseRepository.findAll(spec, sortedPageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取推荐课程
     */
    public Page<CourseDTO.ListItem> getRecommendedCourses(Pageable pageable) {
        log.info("获取推荐课程");

        Page<Course> coursePage = courseRepository.findRecommendedCourses(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取热门课程
     */
    public Page<CourseDTO.ListItem> getPopularCourses(Pageable pageable) {
        log.info("获取热门课程");

        Page<Course> coursePage = courseRepository.findByStatusOrderByStudentCountDescCreateTimeDesc(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取最新课程
     */
    public Page<CourseDTO.ListItem> getLatestCourses(Pageable pageable) {
        log.info("获取最新课程");

        Long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        Page<Course> coursePage = courseRepository.findByStatusAndPublishTimeAfterOrderByPublishTimeDesc(1, oneWeekAgo, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取课程统计
     */
    public CourseDTO.Statistics getCourseStatistics() {
        log.info("获取课程统计");

        CourseDTO.Statistics statistics = new CourseDTO.Statistics();

        // 基础统计
        statistics.setTotalCourses(courseRepository.count());
        statistics.setPublishedCourses(courseRepository.countByStatus(1));
        statistics.setDraftCourses(courseRepository.countByStatus(0));
        statistics.setUnpublishedCourses(courseRepository.countByStatus(2));

        return statistics;
    }

    /**
     * 获取课程分类列表
     */
    public List<String> getCourseCategories() {
        return courseRepository.findDistinctCategoriesByStatus(1);
    }

    // ==================== 章节管理方法 ====================

    /**
     * 为课程创建章节
     */
    @Transactional
    public void createChaptersForCourse(String courseId, List<CourseChapterDTO.CreateRequest> chapterRequests) {
        for (CourseChapterDTO.CreateRequest chapterRequest : chapterRequests) {
            CourseChapter chapter = new CourseChapter();
            BeanUtils.copyProperties(chapterRequest, chapter);
            chapter.setCourseId(courseId);

            // 如果没有指定排序序号，自动设置
            if (chapter.getSortOrder() == null) {
                Integer maxOrder = courseChapterRepository.findMaxSortOrderByCourseId(courseId);
                chapter.setSortOrder(maxOrder == null ? 1 : maxOrder + 1);
            }

            courseChapterRepository.save(chapter);
        }
    }

    /**
     * 更新课程章节（根据ID存在则更新，否则创建）
     */
    @Transactional
    public void updateChaptersForCourse(String courseId, List<CourseChapterDTO.UpdateRequest> chapterRequests) {
        for (CourseChapterDTO.UpdateRequest chapterRequest : chapterRequests) {
            if (StringUtils.hasText(chapterRequest.getId())) {
                // 更新已有章节
                CourseChapter chapter = courseChapterRepository.findById(chapterRequest.getId())
                        .orElse(new CourseChapter());
                // 如果查询到的章节不属于当前课程，则忽略
                if (chapter.getId() != null && !courseId.equals(chapter.getCourseId())) {
                    continue;
                }
                BeanUtils.copyProperties(chapterRequest, chapter, "id", "courseId");
                chapter.setCourseId(courseId);
                courseChapterRepository.save(chapter);
            } else {
                // 新建章节
                CourseChapter chapter = new CourseChapter();
                BeanUtils.copyProperties(chapterRequest, chapter);
                chapter.setCourseId(courseId);
                if (chapter.getSortOrder() == null) {
                    Integer maxOrder = courseChapterRepository.findMaxSortOrderByCourseId(courseId);
                    chapter.setSortOrder(maxOrder == null ? 1 : maxOrder + 1);
                }
                courseChapterRepository.save(chapter);
            }
        }
    }

    // ==================== 🔧 文件处理方法 ====================

    /**
     * 处理课程学习资料信息
     */
    private void handleCourseMaterials(Course course, Object request) {
        List<CourseDTO.MaterialInfo> materials = null;
        String materialUrls = null;

        if (request instanceof CourseDTO.CreateRequest) {
            CourseDTO.CreateRequest createRequest = (CourseDTO.CreateRequest) request;
            materials = createRequest.getMaterials();
            materialUrls = createRequest.getMaterialUrls();
        } else if (request instanceof CourseDTO.UpdateRequest) {
            CourseDTO.UpdateRequest updateRequest = (CourseDTO.UpdateRequest) request;
            materials = updateRequest.getMaterials();
            materialUrls = updateRequest.getMaterialUrls();
        }

        if (materials != null && !materials.isEmpty()) {
            // 🔧 优先使用包含文件名的新格式
            List<String> urls = new ArrayList<>();
            List<String> names = new ArrayList<>();

            for (CourseDTO.MaterialInfo material : materials) {
                urls.add(material.getUrl());
                names.add(material.getName() != null ? material.getName() :
                        (material.getOriginalName() != null ? material.getOriginalName() : "学习资料"));
            }

            course.setMaterialsWithNames(urls, names);
            log.debug("设置学习资料: urls={}, names={}", urls, names);
        } else if (StringUtils.hasText(materialUrls)) {
            // 🔧 兼容旧格式：只有URL字符串 - 修正为Java 8兼容
            List<String> urls = Arrays.asList(materialUrls.split(","));
            course.setMaterialsWithNames(urls, null);
            log.debug("设置学习资料(兼容格式): urls={}", urls);
        }
    }

    /**
     * 处理课程视频资料信息
     */
    private void handleCourseVideos(Course course, Object request) {
        List<CourseDTO.VideoInfo> videos = null;
        String videoUrls = null;

        if (request instanceof CourseDTO.CreateRequest) {
            CourseDTO.CreateRequest createRequest = (CourseDTO.CreateRequest) request;
            videos = createRequest.getVideos();
            videoUrls = createRequest.getVideoUrls();
        } else if (request instanceof CourseDTO.UpdateRequest) {
            CourseDTO.UpdateRequest updateRequest = (CourseDTO.UpdateRequest) request;
            videos = updateRequest.getVideos();
            videoUrls = updateRequest.getVideoUrls();
        }

        if (videos != null && !videos.isEmpty()) {
            // 🔧 优先使用包含文件名的新格式
            List<String> urls = new ArrayList<>();
            List<String> names = new ArrayList<>();

            for (CourseDTO.VideoInfo video : videos) {
                urls.add(video.getUrl());
                names.add(video.getName() != null ? video.getName() :
                        (video.getOriginalName() != null ? video.getOriginalName() : "视频资料"));
            }

            course.setVideosWithNames(urls, names);
            log.debug("设置视频资料: urls={}, names={}", urls, names);
        } else if (StringUtils.hasText(videoUrls)) {
            // 🔧 兼容旧格式：只有URL字符串 - 修正为Java 8兼容
            List<String> urls = Arrays.asList(videoUrls.split(","));
            course.setVideosWithNames(urls, null);
            log.debug("设置视频资料(兼容格式): urls={}", urls);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证课程是否可以发布
     */
    private void validateCourseForPublish(Course course) {
        if (!StringUtils.hasText(course.getTitle())) {
            throw new BusinessException(2003, "课程标题不能为空");
        }
        if (!StringUtils.hasText(course.getDescription())) {
            throw new BusinessException(2003, "课程描述不能为空");
        }
        if (!StringUtils.hasText(course.getCategory())) {
            throw new BusinessException(2003, "课程分类不能为空");
        }

        // 检查是否有章节
        Long chapterCount = courseChapterRepository.countByCourseId(course.getId());
        if (chapterCount == 0) {
            throw new BusinessException(2003, "课程至少需要一个章节才能发布");
        }
    }

    /**
     * 构建搜索条件
     */
    private Specification<Course> buildSearchSpecification(CourseDTO.SearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索
            if (StringUtils.hasText(searchRequest.getKeyword())) {
                String keyword = "%" + searchRequest.getKeyword() + "%";
                Predicate titlePredicate = criteriaBuilder.like(root.get("title"), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(root.get("description"), keyword);
                Predicate tagsPredicate = criteriaBuilder.like(root.get("tags"), keyword);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate, tagsPredicate));
            }

            // 分类筛选
            if (StringUtils.hasText(searchRequest.getCategory())) {
                predicates.add(criteriaBuilder.equal(root.get("category"), searchRequest.getCategory()));
            }

            // 难度筛选
            if (searchRequest.getDifficultyLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficultyLevel"), searchRequest.getDifficultyLevel()));
            }

            // 讲师筛选
            if (StringUtils.hasText(searchRequest.getInstructorId())) {
                predicates.add(criteriaBuilder.equal(root.get("instructorId"), searchRequest.getInstructorId()));
            }

            // 状态筛选
            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            } /*else {
                // 默认只显示已发布的课程
                predicates.add(criteriaBuilder.equal(root.get("status"), 1));
            }*/

            // 价格筛选
            /*if (searchRequest.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), searchRequest.getMinPrice()));
            }*/
            if (searchRequest.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), searchRequest.getMaxPrice()));
            }

            // 是否必修筛选
            if (searchRequest.getIsRequired() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRequired"), searchRequest.getIsRequired()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 🔧 转换为响应DTO
     */
    private CourseDTO.Response convertToResponse(Course course) {
        CourseDTO.Response response = new CourseDTO.Response();
        BeanUtils.copyProperties(course, response);

        // 设置状态和难度文本
        response.setStatusText(course.getStatusText());
        response.setDifficultyText(course.getDifficultyText());

        // 🔧 重要：设置新格式的材料列表（包含文件名）
        response.setMaterialList(convertMaterialInfoList(course.getMaterialInfoList()));
        response.setVideoList(convertVideoInfoList(course.getVideoInfoList()));

        // 🔧 保持兼容：同时设置旧格式字段
        response.setMaterialUrls(course.getMaterialUrls());
        response.setMaterialNames(course.getMaterialNames());
        response.setVideoUrls(course.getVideoUrls());
        response.setVideoNames(course.getVideoNames());

        // 设置章节信息
        if (course.getChapters() != null) {
            List<CourseChapterDTO.Response> chapterResponses = course.getChapters().stream()
                    .map(this::convertChapterToResponse)
                    .collect(Collectors.toList());
            response.setChapters(chapterResponses);

            // 计算统计信息
            response.setTotalChapters(chapterResponses.size());
            response.setTotalDuration(chapterResponses.stream()
                    .mapToInt(chapter -> chapter.getDuration() != null ? chapter.getDuration() : 0)
                    .sum());
        }

        return response;
    }

    /**
     * 🔧 转换为列表项DTO
     */
    private CourseDTO.ListItem convertToListItem(Course course) {
        CourseDTO.ListItem item = new CourseDTO.ListItem();
        BeanUtils.copyProperties(course, item);

        // 设置状态和难度文本
        item.setStatusText(course.getStatusText());
        item.setDifficultyText(course.getDifficultyText());

        // 🔧 重要：设置新格式的材料列表（包含文件名）
        item.setMaterialList(convertMaterialInfoList(course.getMaterialInfoList()));
        item.setVideoList(convertVideoInfoList(course.getVideoInfoList()));

        // 设置统计信息
        if (course.getChapters() != null) {
            item.setTotalChapters(course.getChapters().size());
            item.setTotalDuration(course.getChapters().stream()
                    .mapToInt(chapter -> chapter.getDuration() != null ? chapter.getDuration() : 0)
                    .sum());
        }

        return item;
    }

    /**
     * 🔧 转换材料信息列表
     */
    private List<CourseDTO.MaterialInfo> convertMaterialInfoList(List<Course.MaterialInfo> courseInfoList) {
        if (courseInfoList == null || courseInfoList.isEmpty()) {
            return new ArrayList<>();
        }

        return courseInfoList.stream()
                .map(courseInfo -> {
                    CourseDTO.MaterialInfo dtoInfo = new CourseDTO.MaterialInfo();
                    dtoInfo.setUrl(courseInfo.getUrl());
                    dtoInfo.setName(courseInfo.getName());
                    dtoInfo.setSize(courseInfo.getSize());
                    dtoInfo.setContentType(courseInfo.getContentType());
                    return dtoInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 🔧 转换视频信息列表
     */
    private List<CourseDTO.VideoInfo> convertVideoInfoList(List<Course.VideoInfo> courseInfoList) {
        if (courseInfoList == null || courseInfoList.isEmpty()) {
            return new ArrayList<>();
        }

        return courseInfoList.stream()
                .map(courseInfo -> {
                    CourseDTO.VideoInfo dtoInfo = new CourseDTO.VideoInfo();
                    dtoInfo.setUrl(courseInfo.getUrl());
                    dtoInfo.setName(courseInfo.getName());
                    dtoInfo.setSize(courseInfo.getSize());
                    dtoInfo.setDuration(courseInfo.getDuration());
                    return dtoInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 🔧 转换章节为响应DTO
     */
    private CourseChapterDTO.Response convertChapterToResponse(CourseChapter chapter) {
        CourseChapterDTO.Response response = new CourseChapterDTO.Response();
        BeanUtils.copyProperties(chapter, response);
        return response;
    }

    /**
     * 构建排序参数
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        switch (sortBy) {
            case "rating":
                return Sort.by(direction, "rating", "createTime");
            case "studentCount":
                return Sort.by(direction, "studentCount", "createTime");
            case "publishTime":
                return Sort.by(direction, "publishTime", "createTime");
            case "title":
                return Sort.by(direction, "title");
            case "price":
                return Sort.by(direction, "price", "createTime");
            default:
                return Sort.by(direction, "createTime");
        }
    }
}