package com.yuanzhang.econexus.dto;

import lombok.Data;

// 字典DTO（组合类型和值）
@Data
public class DicDTO {
    private String typeCode; // 类型编码（如gender）
    private String dicCode; // 字典值
    private String dicLabel; // 标签
}