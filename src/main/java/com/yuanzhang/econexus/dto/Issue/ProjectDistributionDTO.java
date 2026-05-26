package com.yuanzhang.econexus.dto.Issue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事务-项目分布DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDistributionDTO {
    // 项目名称
    private String projectName;
    // 该项目下事务数量
    private Integer count;
}
