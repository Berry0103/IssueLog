package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.dto.UserDTO;
import com.yuanzhang.econexus.model.Department;
import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
public class DepartmentController {
    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/deptlist")
    public ResponseEntity<List<Department>> getDeptList() {
        List<Department> depts = departmentRepository.findAll();
        if (depts == null){
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(depts.stream()
                .collect(Collectors.toList()));
    }
}
