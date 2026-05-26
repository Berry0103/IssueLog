package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pm_dictype")
public class Dictype {

    @Id
    @Column(name = "dictype_index", nullable = false)
    private String dictypeIndex; // 字典类型唯一标识

    @Column(name = "dictype_name", nullable = false, length = 64)
    private String dictypeName; // 字典类型名称 (如: "Project Type", "Issue Status")

    @Column(name = "dictype_desc")
    private String dictypeDesc; // 字典类型描述
}