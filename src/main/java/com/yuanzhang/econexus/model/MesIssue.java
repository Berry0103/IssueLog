package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pm_issue")
public class MesIssue {

    @Id
    @UuidGenerator
    @Column(name = "issue_index", nullable = false)
    private String issueIndex; // 事务唯一标识

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_index", referencedColumnName = "project_index")
    private Project project; // 关联项目

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 事务描述

    @Column(name = "description_cn", columnDefinition = "TEXT")
    private String description_cn; // 事务描述

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String description_en; // 事务描述

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_status", referencedColumnName = "dic_index")
    private Dictionary issueStatus; // 事务状态 (关联字典)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority", referencedColumnName = "dic_index")
    private Dictionary priority; // 优先级 (关联字典)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_owner", referencedColumnName = "user_index")
    private User actionOwner; // 处理人

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_owner", referencedColumnName = "user_index")
    private User issueOwner; // 负责人

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_reporter", referencedColumnName = "user_index")
    private User issueReporter; // 报告人

    @Column(name = "start_time", columnDefinition = "datetime(0)")
    private LocalDateTime startTime; // 开始时间

    @Column(name = "deadline", columnDefinition = "datetime(0)")
    private LocalDateTime deadline; // 截止时间

}