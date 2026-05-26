package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.dto.Issue.MesIssueDTO;
import com.yuanzhang.econexus.dto.Issue.MesIssueUpdateDTO;
import com.yuanzhang.econexus.model.MesIssue;
import com.yuanzhang.econexus.model.MesIssueUpdate;
import com.yuanzhang.econexus.service.MesIssueService;
import com.yuanzhang.econexus.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
public class MesIssueController {

    @Autowired
    private MesIssueService issueService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<MesIssueDTO>> getIssues(@RequestParam Map<String, Object> params) {
        // 1. 分页参数处理
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) - 1 : 0;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;

        // 2. 排序参数处理（新增核心逻辑）
        // 2.1 从请求参数中获取前端传递的排序字段和方向
        String sortField = params.getOrDefault("sortField", "startTime").toString(); // 默认排序字段：startTime
        String sortDirection = params.getOrDefault("sortDirection", "DESC").toString().toUpperCase(); // 默认方向：降序

        sortField = switch (sortField) {
            case "projectName" -> "project.projectName";
            case "actionOwnerName" -> "actionOwner.lastname";
            case "issueOwnerName" -> "issueOwner.lastname";
            default -> "startTime";
        };
        // 2.2 验证排序方向合法性（只能是 ASC 或 DESC）
        Sort.Direction direction = Sort.Direction.DESC; // 默认降序
        if ("ASC".equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        }

        // 2.3 构建动态排序规则
        Sort sort = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MesIssue> issuePage = issueService.findIssuesByPage(params, pageable);

        // 将 Page<MesIssue> 转换为 Page<MesIssueDTO>
//        Page<MesIssueDTO> dtoPage = (Page<MesIssueDTO>) issuePage.map(MesIssueDTO::fromEntity);
        Page<MesIssueDTO> dtoPage = (Page<MesIssueDTO>) issuePage.map(issue -> {
            List<MesIssueUpdate> updateList = issueService.getIssueUpdates(issue.getIssueIndex());
            return issueService.convertToDTOWithUpdates(issue, updateList);
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesIssueDTO> getIssueById(@PathVariable("id") String id) {
        MesIssueDTO issueInfo;
        MesIssue issue = issueService.getIssueById(id);
        List<MesIssueUpdate> updatelist = issueService.getIssueUpdates(id);

        return issue != null ? ResponseEntity.ok(issueService.convertToDTOWithUpdates(issue, updatelist)) : ResponseEntity.notFound().build();
    }

    // 创建
    @PostMapping("/create")
    public ResponseEntity<MesIssue> createIssue(@RequestBody MesIssue issue) {
        MesIssue createdIssue = issueService.createIssue(issue);
        return ResponseEntity.ok(createdIssue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesIssue> updateIssue(@PathVariable("id") String id, @RequestBody MesIssue issue) {
        MesIssue updatedIssue = issueService.updateIssue(id, issue);
        return ResponseEntity.ok(updatedIssue);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MesIssue> updateIssueStatus(@PathVariable("id") String id, @RequestBody MesIssueUpdate update) {
        MesIssue updatedIssue = issueService.updateIssueStatus(id, update);
        return ResponseEntity.ok(updatedIssue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable("id") String id) {
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<MesIssueUpdate>> getIssueUpdates(@PathVariable("id") String id) {
        List<MesIssueUpdate> updates = issueService.getIssueUpdates(id);
        return ResponseEntity.ok(updates);
    }

    @PostMapping("/update")
    public ResponseEntity<MesIssueUpdate> addIssueUpdate(@RequestBody MesIssueUpdateDTO dto) throws MessagingException {
        // 从嵌套的issue对象中获取
        if (dto.getIssueUpdate() == null || dto.getIssueUpdate().getIssue() == null
                || dto.getIssueUpdate().getIssue().getIssueIndex() == null) {
            return ResponseEntity.badRequest().body(null); // 事务ID不能为空
        }

        if (dto.getIssueUpdate().getUpdateOwner() == null || dto.getIssueUpdate().getUpdateOwner().getUserIndex() == null) {
            return ResponseEntity.badRequest().body(null); // 用户ID不能为空
        }

        if (dto.getIssueStatusIndex() == null || dto.getActionOwnerIndex() == null || dto.getIssueOwnerIndex() == null) {
            return ResponseEntity.badRequest().body(null); // 信息不能为空
        }

        MesIssueUpdate savedUpdate = issueService.saveIssueUpdate(dto);
        return ResponseEntity.ok(savedUpdate);
    }

    @GetMapping("/generate")
    public void generateIssues() {
        issueService.genrateIssues();
    }

    @GetMapping("/generateUpdates")
    public void generateUpdates() {
        issueService.genrateUpdates();
    }
}