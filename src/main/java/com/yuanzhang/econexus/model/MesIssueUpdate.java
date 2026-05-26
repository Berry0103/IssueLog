package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pm_issueupdate")
public class MesIssueUpdate {

    @Id
    @UuidGenerator
    @Column(name = "update_index", nullable = false)
    private String updateIndex; // 更新记录唯一标识

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_index", nullable = false, referencedColumnName = "issue_index")
    private MesIssue issue; // 关联的事务

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "update_owner", referencedColumnName = "user_index")
    private User updateOwner; // 更新人

    @Column(name = "status_update", columnDefinition = "TEXT")
    private String statusUpdate; // 更新内容/备注

    @Column(name = "update_cn", columnDefinition = "TEXT")
    private String update_cn; // 更新内容/备注

    @Column(name = "update_en", columnDefinition = "TEXT")
    private String update_en; // 更新内容/备注

    @Column(name = "update_time", columnDefinition = "datetime(0)")
    private LocalDateTime updateTime; // 更新时间
}