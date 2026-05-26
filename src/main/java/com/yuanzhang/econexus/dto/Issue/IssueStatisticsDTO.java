package com.yuanzhang.econexus.dto.Issue;

import lombok.Data;

/**
 * 事务统计数据DTO
 */
@Data
public class IssueStatisticsDTO {
    // 总事务数
    private Integer total = 0;
    // 开启事务数
    private Integer open = 0;
    // 进行中事务数
    private Integer progress = 0;
    // 超期事务数
    private Integer overdue = 0;
}
