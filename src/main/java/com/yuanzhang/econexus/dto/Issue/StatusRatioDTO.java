package com.yuanzhang.econexus.dto.Issue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事务-状态占比DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusRatioDTO {
    // 状态名称（已完成/进行中/超期/待处理）
    private String statusName;
    // 该状态下事务数量
    private Integer count;
}
