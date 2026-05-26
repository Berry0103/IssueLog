package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.MesIssue;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MesIssueRepository extends JpaRepository<MesIssue, String>, JpaSpecificationExecutor<MesIssue> {
    @EntityGraph(attributePaths = {"project", "issueStatus", "priority", "actionOwner", "issueOwner", "issueReporter"})
    @Override
    Page<MesIssue> findAll(Specification<MesIssue> spec, Pageable pageable);

    @Query("SELECT i FROM MesIssue i " +
            "JOIN FETCH i.project " +
            "JOIN FETCH i.issueStatus " +
            "JOIN FETCH i.priority " +
            "LEFT JOIN FETCH i.actionOwner " +
            "LEFT JOIN FETCH i.issueOwner " +
            "LEFT JOIN FETCH i.issueReporter " +
            "WHERE i.issueStatus.dicIndex <> '0d652c28-8172-48d9-9bbf-586fe526b620' AND (i.issueOwner.userIndex = :userIndex OR i.actionOwner.userIndex = :userIndex OR i.issueReporter.userIndex = :userIndex) ORDER BY i.deadline DESC")
    List<MesIssue> findByUserIndexInAnyOwnerFieldOrderByDeadlineDesc(@Param("userIndex") String userIndex);

    @Query("SELECT i FROM MesIssue i " +
            "JOIN FETCH i.project " +
            "JOIN FETCH i.issueStatus " +
            "JOIN FETCH i.priority " +
            "LEFT JOIN FETCH i.actionOwner " +
            "LEFT JOIN FETCH i.issueOwner " +
            "LEFT JOIN FETCH i.issueReporter " +
            "WHERE i.issueStatus.dicIndex <> '0d652c28-8172-48d9-9bbf-586fe526b620' AND i.actionOwner.userIndex = :userIndex ORDER BY i.deadline ASC")
    List<MesIssue> findByUserIndexInActionOwnerFieldOrderByDeadlineAsc(@Param("userIndex") String userIndex);

    @Query("SELECT i FROM MesIssue i WHERE 1=1")
    List<MesIssue> findAllIssues();

    @EntityGraph(attributePaths = {"project", "issueStatus", "priority", "actionOwner", "issueOwner", "issueReporter"})
    @Override
    Optional<MesIssue> findById(@Param("issueIndex") String issueIndex);

}