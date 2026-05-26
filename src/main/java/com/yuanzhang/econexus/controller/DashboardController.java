package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.dto.Issue.IssueStatisticsDTO;
import com.yuanzhang.econexus.dto.Issue.MesIssueDTO;
import com.yuanzhang.econexus.dto.Issue.ProjectDistributionDTO;
import com.yuanzhang.econexus.dto.Issue.StatusRatioDTO;
import com.yuanzhang.econexus.service.MesIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private MesIssueService issueDashboardService;

    /**
     * 接口1：获取用户事务统计数据
     * GET /api/issues/statistics?userId=xxx
     */
    @GetMapping("/statistics")
    public ResponseEntity<IssueStatisticsDTO> getIssueStatistics(
            @RequestParam String userId) {
        IssueStatisticsDTO statistics = issueDashboardService.getIssueStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 接口2：获取用户事务-项目分布数据
     * GET /api/issues/project-distribution?userId=xxx
     */
    @GetMapping("/project-distribution")
    public ResponseEntity<List<ProjectDistributionDTO>> getProjectDistribution(
            @RequestParam String userId) {
        List<ProjectDistributionDTO> distribution = issueDashboardService.getProjectDistribution(userId);
        return ResponseEntity.ok(distribution);
    }

    /**
     * 接口3：获取用户事务-状态占比数据
     * GET /api/issues/status-ratio?userId=xxx
     */
    @GetMapping("/status-ratio")
    public ResponseEntity<List<StatusRatioDTO>> getStatusRatio(
            @RequestParam String userId) {
        List<StatusRatioDTO> ratio = issueDashboardService.getStatusRatio(userId);
        return ResponseEntity.ok(ratio);
    }

    /**
     * 接口4：获取用户近期事务
     * GET /api/issues/recent?userId=xxx&size=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<MesIssueDTO>> getRecentIssues(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") Integer size) {
        List<MesIssueDTO> recentIssues = issueDashboardService.getRecentIssues(userId, size);
        return ResponseEntity.ok(recentIssues);
    }

    /**
     * 接口4：获取用户排程
     * GET /api/issues/recent?userId=xxx&size=10
     */
    @GetMapping("/gantt")
    public ResponseEntity<List<MesIssueDTO>> getIssuesGantt(
            @RequestParam String userId) {
        List<MesIssueDTO> recentIssues = issueDashboardService.getIssuesGantt(userId);
        return ResponseEntity.ok(recentIssues);
    }
}
