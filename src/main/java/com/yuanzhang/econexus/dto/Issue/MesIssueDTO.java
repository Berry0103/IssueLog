package com.yuanzhang.econexus.dto.Issue;

import com.yuanzhang.econexus.model.*;
import com.yuanzhang.econexus.util.BaiduTranslateUtil;
import com.yuanzhang.econexus.util.SpringContextUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
public class MesIssueDTO {
    private String issueIndex;
    private String projectName; // 项目名称
    private String projectIndex;
    private String description;
    private String description_cn;
    private String description_en;
    private String issueStatusName; // 状态名称
    private String issueStatusIndex; // 状态索引
    private String issueStatusColor; // 状态颜色
    private String priorityName; // 优先级名称
    private String priorityColor; // 优先级颜色
    private String actionOwnerName; // 处理人姓名
    private String actionOwnerIndex; // 处理人索引
    private String actionOwnerQywx;
    private String issueOwnerName; // 负责人姓名
    private String issueOwnerIndex; // 负责人索引
    private String issueOwnerQywx;
    private String issueReporterName; // 报告人姓名
    private String issueReporterQywx;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime lastUpdateTime;
    private String issueTypeName; // 事务类型名称
    private List<MesIssueUpdate> updateList;

    // 从 MesIssue 实体转换为 DTO
    public static MesIssueDTO fromEntity(MesIssue issue,List<MesIssueUpdate> updateList) {
        MesIssueDTO dto = new MesIssueDTO();
        dto.setIssueIndex(issue.getIssueIndex());
        dto.setDescription(issue.getDescription());
        dto.setDescription_cn(issue.getDescription_cn());
        dto.setDescription_en(issue.getDescription_en());

        dto.setStartTime(issue.getStartTime());
        dto.setDeadline(issue.getDeadline());

        if (issue.getProject() != null) {
            dto.setProjectName(issue.getProject().getProjectName());
            dto.setProjectIndex(issue.getProject().getProjectIndex());
        }
        if (issue.getIssueStatus() != null) {
            dto.setIssueStatusName(issue.getIssueStatus().getDicName());
            dto.setIssueStatusIndex(issue.getIssueStatus().getDicIndex());
            dto.setIssueStatusColor(issue.getIssueStatus().getDicColor());
        }
        if (issue.getPriority() != null) {
            dto.setPriorityName(issue.getPriority().getDicName());
            dto.setPriorityColor(issue.getPriority().getDicColor());
        }
        if (issue.getActionOwner() != null) {
            dto.setActionOwnerName(issue.getActionOwner().getFullname());
            dto.setActionOwnerIndex(issue.getActionOwner().getUserIndex());
            dto.setActionOwnerQywx(issue.getActionOwner().getQywxId());
        }
        if (issue.getIssueOwner() != null) {
            dto.setIssueOwnerName(issue.getIssueOwner().getFullname());
            dto.setIssueOwnerIndex(issue.getIssueOwner().getUserIndex());
            dto.setIssueOwnerQywx(issue.getIssueOwner().getQywxId());
        }
        if (issue.getIssueReporter() != null) {
            dto.setIssueReporterName(issue.getIssueReporter().getFullname());
            dto.setIssueReporterQywx(issue.getIssueReporter().getQywxId());
        }
        if (updateList.size() > 0) {
            updateList.sort(Comparator.comparing(MesIssueUpdate::getUpdateTime).reversed());
            dto.setUpdateList(updateList);
            MesIssueUpdate lastRecord = updateList.get(0);
            dto.setLastUpdateTime(lastRecord.getUpdateTime());
        }

        return dto;
    }
}