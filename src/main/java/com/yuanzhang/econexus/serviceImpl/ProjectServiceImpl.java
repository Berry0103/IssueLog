package com.yuanzhang.econexus.serviceImpl;

import com.yuanzhang.econexus.model.Project;
import com.yuanzhang.econexus.repository.ProjectRepository;
import com.yuanzhang.econexus.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public List<Project> findAll() {
        // JpaRepository 自带的 findAll() 方法
        return projectRepository.findAll();
    }

    // ... 其他方法的实现
}