package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    @EntityGraph(attributePaths = {"projectType", "power", "customer"})
    @Override
    List<Project> findAll();
}