package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.CourseListItemDTO;
import com.example.smarttrainingsystem.dto.UserStudyDTO;
import com.example.smarttrainingsystem.service.UserStudyService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-study")
@RequiredArgsConstructor
public class UserStudyController {

    private final UserStudyService userStudyService;

    @GetMapping("/overview")
    public Result<UserStudyDTO.Overview> getOverview() {
        String userId = SecurityUtils.getCurrentUserId();
        return Result.success(userStudyService.getOverview(userId));
    }

    @GetMapping("/courses")
    public Result<List<CourseListItemDTO>> getMyCourses() {
        String userId = SecurityUtils.getCurrentUserId();
        return Result.success(userStudyService.getMyCourses(userId));
    }

    @GetMapping("/recommendation")
    public Result<List<CourseListItemDTO>> getRecommendation() {
        String userId = SecurityUtils.getCurrentUserId();
        return Result.success(userStudyService.getRecommendations(userId));
    }
}
