package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Table(name = "pm_project")
public class Project {

    @Id
    @UuidGenerator
    @Column(name = "project_index", nullable = false)
    private String projectIndex; // 项目唯一标识

    @Column(name = "project_name", nullable = false, length = 128)
    private String projectName; // 项目名称

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_type", referencedColumnName = "dic_index")
    private Dictionary projectType; // 项目类型 (关联字典表)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "power", referencedColumnName = "dic_index")
    private Dictionary power; // 功率

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_index", referencedColumnName = "customer_index")
    private Customer customer; // 客户标识

    @Column(name = "description")
    private String description; // 描述
}