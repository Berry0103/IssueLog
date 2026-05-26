package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.dto.Issue.*;
import com.yuanzhang.econexus.model.MesIssue;
import com.yuanzhang.econexus.model.MesIssueUpdate;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MesIssueService {
    /**
     * 分页查询事务列表，支持多条件筛选
     * @param params 筛选条件 (projectIndex, issueStatus, actionOwner, startTime, deadline, etc.)
     * @param pageable 分页参数
     * @return 分页事务列表
     */
    Page<MesIssue> findIssuesByPage(Map<String, Object> params, Pageable pageable);

    /**
     * 根据ID获取事务详情
     * @param issueIndex 事务ID
     * @return 事务对象
     */
    MesIssue getIssueById(String issueIndex);

    /**
     * 创建新事务
     * @param mesIssue 事务对象
     * @return 创建成功的事务对象
     */
    MesIssue createIssue(MesIssue mesIssue);

    /**
     * 更新事务信息
     * @param issueIndex 事务ID
     * @param mesIssue 新的事务信息
     * @return 更新后的事务对象
     */
    MesIssue updateIssue(String issueIndex, MesIssue mesIssue);

    /**
     * 更新事务状态，并添加一条更新记录
     * @param issueIndex 事务ID
     * @param update 包含更新人、更新内容等信息的对象
     * @return 更新后的事务对象
     */
    MesIssue updateIssueStatus(String issueIndex, MesIssueUpdate update);

    /**
     * 根据ID删除事务
     * @param issueIndex 事务ID
     */
    void deleteIssue(String issueIndex);

    /**
     * 获取某个事务的所有更新记录
     * @param issueIndex 事务ID
     * @return 更新记录列表
     */
    List<MesIssueUpdate> getIssueUpdates(String issueIndex);

    MesIssueUpdate saveIssueUpdate(MesIssueUpdateDTO update) throws MessagingException;

    IssueStatisticsDTO getIssueStatistics(String userId);

    List<ProjectDistributionDTO> getProjectDistribution(String userId);

    List<StatusRatioDTO> getStatusRatio(String userId);

    List<MesIssueDTO> getRecentIssues(String userId, Integer size);

    List<MesIssueDTO> getIssuesGantt(String userId);

    void genrateIssues();

    void genrateUpdates();

    MesIssueDTO convertToDTO(MesIssue issue);

    MesIssueDTO convertToDTOWithUpdates(MesIssue issue,List<MesIssueUpdate> updateList);
}