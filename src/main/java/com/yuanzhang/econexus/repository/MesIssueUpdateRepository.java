package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.MesIssue;
import com.yuanzhang.econexus.model.MesIssueUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesIssueUpdateRepository extends JpaRepository<MesIssueUpdate, String> {
    // 根据事务ID查询所有更新记录，并按更新时间降序排列
    @Query("SELECT i FROM MesIssueUpdate i JOIN FETCH i.issue JOIN FETCH i.updateOwner WHERE i.issue.issueIndex = :issueIndex")
    List<MesIssueUpdate> findByIssueOrderByUpdateTimeDesc(@Param("issueIndex") String issueIndex);

    @Query("SELECT i FROM MesIssueUpdate i WHERE 1=1")
    List<MesIssueUpdate> findAllUpdates();
}