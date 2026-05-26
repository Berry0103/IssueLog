package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pm_dictionary")
public class Dictionary {

    @Id
    @Column(name = "dic_index", nullable = false)
    private String dicIndex; // 字典项唯一标识

    @Column(name = "dic_name", nullable = false, length = 64)
    private String dicName; // 字典项显示名称 (如: "处理中")

    @Column(name = "dic_num", length = 10)
    private String dicNum; // 字典项排序号

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictype_index", referencedColumnName = "dictype_index")
    private Dictype dictype; // 所属字典类型

    @Column(name = "description")
    private String description; // 描述

    @Column(name = "dic_color", length = 20)
    private String dicColor; // 显示颜色 (如: "bg-primary")
}