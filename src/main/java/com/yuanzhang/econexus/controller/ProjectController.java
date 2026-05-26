package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.dto.ProjectDTO;
import com.yuanzhang.econexus.dto.UserDTO;
import com.yuanzhang.econexus.model.Project;
import com.yuanzhang.econexus.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 获取所有项目列表
     * @return 项目列表
     */
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.findAll();
        if (projects == null){
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(projects.stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList()));
    }


}