package com.yuanzhang.econexus.serviceImpl;

import com.yuanzhang.econexus.dto.Issue.*;
import com.yuanzhang.econexus.model.*;
import com.yuanzhang.econexus.model.Dictionary;
import com.yuanzhang.econexus.repository.*;
import com.yuanzhang.econexus.service.MesIssueService;
import com.yuanzhang.econexus.service.UserService;
import com.yuanzhang.econexus.util.BaiduTranslateUtil;
import com.yuanzhang.econexus.util.ExmailSendUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MesIssueServiceImpl implements MesIssueService {
    @Value("${server.url}")
    private String homepage;

    @Autowired
    private MesIssueRepository issueRepository;

    @Autowired
    private MesIssueUpdateRepository issueUpdateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExmailSendUtil mailUtil;

    @Autowired
    private BaiduTranslateUtil baiduTranslateUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Cacheable(value = "issuesCache", keyGenerator = "issuesKeyGenerator")
    @Transactional(readOnly = true)
    @Override
    public Page<MesIssue> findIssuesByPage(Map<String, Object> params, Pageable pageable) {
        Specification<MesIssue> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 项目筛选 (通过项目ID)
            if (params.get("projectIndex") != null && !params.get("projectIndex").toString().isEmpty()) {
                Join<MesIssue, Project> projectJoin = root.join("project", JoinType.LEFT);
                predicates.add(cb.equal(projectJoin.get("projectIndex"), params.get("projectIndex")));
            }

            // 状态筛选 (通过字典项ID)
            if (params.get("issueStatus") != null && !params.get("issueStatus").toString().isEmpty()) {
                Join<MesIssue, Dictionary> statusJoin = root.join("issueStatus", JoinType.LEFT);
                predicates.add(cb.equal(statusJoin.get("dicIndex"), params.get("issueStatus")));
            }

            // 处理人筛选 (通过用户ID)
            if (params.get("actionOwner") != null && !params.get("actionOwner").toString().isEmpty()) {
                Join<MesIssue, User> actionOwnerJoin = root.join("actionOwner", JoinType.LEFT);
                predicates.add(cb.equal(actionOwnerJoin.get("userIndex"), params.get("actionOwner")));
            }

            // 开始时间范围筛选
            if (params.get("startTime") != null) {
                try {
                    String startTimeStr = params.get("startTime").toString();
                    LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
                } catch (DateTimeParseException e) {
                    System.err.println("警告：开始时间参数格式不正确，已忽略。");
                }
            }

            // 截止时间范围筛选
            if (params.get("deadline") != null) {
                try {
                    String deadlineStr = params.get("deadline").toString();
                    LocalDateTime deadline = LocalDateTime.parse(deadlineStr, DATE_TIME_FORMATTER);
                    predicates.add(cb.lessThanOrEqualTo(root.get("deadline"), deadline));
                } catch (DateTimeParseException e) {
                    System.err.println("警告：截止时间参数格式不正确，已忽略。");
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return issueRepository.findAll(spec, pageable);
    }

    @Cacheable(value = "issuesCache", keyGenerator = "issuesKeyGenerator")
    @Transactional(readOnly = true)
    @Override
    public MesIssue getIssueById(String issueIndex) {
        return issueRepository.findById(issueIndex).orElse(null);
    }

    @CacheEvict(value = "issuesCache", allEntries = true)
    @Override
    @Transactional
    public MesIssue createIssue(MesIssue mesIssue) {
        String[] to = new String[3];
        // 在保存前，确保所有关联的实体都被正确加载或设置
        // 这里假设前端传来的是ID，我们需要将其转换为实体引用
        if (mesIssue.getActionOwner() != null && mesIssue.getActionOwner().getUserIndex() != null) {
            Optional<User> userOpt = userRepository.findById(mesIssue.getActionOwner().getUserIndex());
            userOpt.ifPresent(mesIssue::setActionOwner);
            to[0] = userOpt.map(User::getEmail).orElse("");
        }
        if (mesIssue.getIssueOwner() != null && mesIssue.getIssueOwner().getUserIndex() != null) {
            Optional<User> userOpt1 = userRepository.findById(mesIssue.getIssueOwner().getUserIndex());
            userOpt1.ifPresent(mesIssue::setIssueOwner);
            to[1] = userOpt1.map(User::getEmail).orElse("");
        }
        if (mesIssue.getIssueReporter() != null && mesIssue.getIssueReporter().getUserIndex() != null) {
            Optional<User> userOpt2 = userRepository.findById(mesIssue.getIssueReporter().getUserIndex());
            userOpt2.ifPresent(mesIssue::setIssueReporter);
        }
        if (mesIssue.getProject() != null && mesIssue.getProject().getProjectIndex() != null) {
            Optional<Project> project = projectRepository.findById(mesIssue.getProject().getProjectIndex());
            project.ifPresent(mesIssue::setProject);
        }
        if (mesIssue.getIssueStatus() != null && mesIssue.getIssueStatus().getDicIndex() != null) {
            Optional<Dictionary> status = dictionaryRepository.findById(mesIssue.getIssueStatus().getDicIndex());
            status.ifPresent(mesIssue::setIssueStatus);
        }
        if (mesIssue.getPriority() != null && mesIssue.getPriority().getDicIndex() != null) {
            Optional<Dictionary> priority = dictionaryRepository.findById(mesIssue.getPriority().getDicIndex());
            priority.ifPresent(mesIssue::setPriority);
        }

        mesIssue.setStartTime(LocalDateTime.now());

        User reporter = mesIssue.getIssueReporter();
        to[2] = reporter.getEmail();

        String project = mesIssue.getProject().getProjectName();
        String subject = "【待解决事件提醒】" + project;
//        String content = "待解决事件提醒！<br><br>";
//        content = content + "事件描述:" + mesIssue.getDescription() + "<br><br>";
//        content = content + "请登录MES系统查看！" + "<br>";
//        content = content + "http://mes.yuanzhangtechs.com";
        String content = "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <title>待解决事件提醒</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <h2 style=\"color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;\">待解决事件提醒</h2>" +
                "        <p>您有新的待解决事件！</p>" +
                // 事件描述核心内容
                "        <p style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                "            <strong>事件描述:</strong> " + mesIssue.getDescription() +
                "        </p>" +
                // MES系统查看提示（和要求完全一致）
                "        <p style=\"color: #333; font-weight: 500;\">请登录MES系统查看！</p>" +
                "        <p style=\"margin: 10px 0;\">" +
                "            <a href=\"" + homepage + "\" " +
                "               style=\"color: #4a90e2; text-decoration: none;\">" + homepage +
                "            </a>" +
                "        </p>" +
                // 底部系统提示
                "        <p style=\"color: #999; margin-top: 30px; font-size: 12px;\">" +
                "            此邮件为系统自动发送，请勿回复。" +
                "        </p>" +
                "    </div>" +
                "</body>" +
                "</html>";
        mailUtil.sendHtmlMail(to, subject, content);
        return issueRepository.save(mesIssue);
    }

    @CacheEvict(value = "issuesCache", allEntries = true)
    @Override
    @Transactional
    public MesIssue updateIssue(String issueIndex, MesIssue mesIssue) {
        MesIssue existingIssue = getIssueById(issueIndex);
        if (existingIssue == null) {
            throw new RuntimeException("事务不存在");
        }
        // 有选择地更新字段，并处理关联
        // ...
        existingIssue.setDescription(mesIssue.getDescription());
        existingIssue.setDeadline(mesIssue.getDeadline());

        // 更新关联
        if (mesIssue.getProject() != null && mesIssue.getProject().getProjectIndex() != null) {
            projectRepository.findById(mesIssue.getProject().getProjectIndex()).ifPresent(existingIssue::setProject);
        }
        if (mesIssue.getIssueStatus() != null && mesIssue.getIssueStatus().getDicIndex() != null) {
            dictionaryRepository.findById(mesIssue.getIssueStatus().getDicIndex()).ifPresent(mesIssue::setIssueStatus);
        }
        if (mesIssue.getPriority() != null && mesIssue.getPriority().getDicIndex() != null) {
            dictionaryRepository.findById(mesIssue.getPriority().getDicIndex()).ifPresent(mesIssue::setPriority);
        }
        if (mesIssue.getActionOwner() != null && mesIssue.getActionOwner().getUserIndex() != null) {
            userRepository.findById(mesIssue.getActionOwner().getUserIndex()).ifPresent(existingIssue::setActionOwner);
        }
        if (mesIssue.getIssueOwner() != null && mesIssue.getIssueOwner().getUserIndex() != null) {
            userRepository.findById(mesIssue.getIssueOwner().getUserIndex()).ifPresent(existingIssue::setIssueOwner);
        }

        return issueRepository.save(existingIssue);
    }

    @CacheEvict(value = "issuesCache", allEntries = true)
    @Override
    @Transactional
    public MesIssue updateIssueStatus(String issueIndex, MesIssueUpdate update) {
        MesIssue issue = getIssueById(issueIndex);
        if (issue == null) {
            throw new RuntimeException("事务不存在");
        }

        // 1. 更新事务状态 (假设update的statusUpdate字段传递了新状态的字典ID)
        if (update.getStatusUpdate() != null && !update.getStatusUpdate().isEmpty()) {
            dictionaryRepository.findById(update.getStatusUpdate()).ifPresent(issue::setIssueStatus);
        }

        issueRepository.save(issue);

        // 2. 创建一条新的更新记录
        update.setIssue(issue);
        // 设置更新人
        if (update.getUpdateOwner() != null && update.getUpdateOwner().getUserIndex() != null) {
            userRepository.findById(update.getUpdateOwner().getUserIndex()).ifPresent(update::setUpdateOwner);
        }
        update.setUpdateTime(LocalDateTime.now());
        issueUpdateRepository.save(update);

        return issue;
    }

    @CacheEvict(value = "issuesCache", allEntries = true)
    @Override
    @Transactional
    public void deleteIssue(String issueIndex) {
        issueRepository.deleteById(issueIndex);
    }

    @Cacheable(value = "issuesCache", keyGenerator = "issuesKeyGenerator")
    @Transactional(readOnly = true)
    @Override
    public List<MesIssueUpdate> getIssueUpdates(String issueIndex) {
        return issueUpdateRepository.findByIssueOrderByUpdateTimeDesc(issueIndex);
    }

    @CacheEvict(value = "issuesCache", allEntries = true)
    @Override
    @Transactional
    public MesIssueUpdate saveIssueUpdate(MesIssueUpdateDTO dto) throws MessagingException {
        String issueIndex = null;
        String updateOwnerIndex = null;
        String[] to = new String[3];

        issueIndex = dto.getIssueUpdate().getIssue().getIssueIndex();
        MesIssue issue = getIssueById(issueIndex); // 从数据库查询MesIssue
        if (issue == null) {
            throw new RuntimeException("事务不存在");
        }

        if (dto.getIssueOwnerIndex() != null && !dto.getIssueOwnerIndex().isEmpty()) {
            Optional<User> issueOwner = userRepository.findById(dto.getIssueOwnerIndex());
            issueOwner.ifPresent(issue::setIssueOwner);
            to[0] = issueOwner.map(User::getEmail).orElse("");
        }
        if (dto.getActionOwnerIndex() != null && !dto.getActionOwnerIndex().isEmpty()) {
            Optional<User> actionOwner = userRepository.findById(dto.getIssueOwnerIndex());
            actionOwner.ifPresent(issue::setActionOwner);
            to[1] = actionOwner.map(User::getEmail).orElse("");
        }
        // 处理更新人关联（如果需要）
        if (dto.getIssueStatusIndex() != null && !dto.getIssueStatusIndex().isEmpty()) {
            dictionaryRepository.findById(dto.getIssueStatusIndex()).ifPresent(issue::setIssueStatus);
        }
        issueRepository.save(issue);

        MesIssueUpdate issueUpdate = dto.getIssueUpdate();
        issueUpdate.setIssue(issue);
        updateOwnerIndex = issueUpdate.getUpdateOwner().getUserIndex();
        User user = userService.getUserById(updateOwnerIndex);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        issueUpdate.setUpdateOwner(user);
        issueUpdate.setUpdateTime(LocalDateTime.now());

        MesIssueUpdate newUpdate = issueUpdateRepository.save(issueUpdate);

        User reporter = issue.getIssueReporter();
        to[2] = reporter.getEmail();
        String project = issue.getProject().getProjectName();
        String subject = "【事件更新提醒】" + project;
//        String content = "您的事件有更新！<br><br>";
//        content = content + "事件描述:" + issue.getDescription() + "<br><br>";
//        content = content + "更新人员:" + user.getFullname() + "<br><br>";
//        content = content + "更新状态:" + issue.getIssueStatus().getDicName() + "<br><br>";
//        content = content + "更新日志:" + issueUpdate.getStatusUpdate() + "<br><br>";
//        content = content + "请登录MES系统查看！" + "<br>";
//        content = content + "http://mes.yuanzhangtechs.com";
        String content = "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <title>事件更新提醒</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <h2 style=\"color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;\">事件更新提醒</h2>" +
                "        <p>您的事件有更新！</p>" +
                // 事件描述核心内容
                "        <p style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                "            <strong>事件描述:</strong> " + issue.getDescription() +
                "        </p>" +
                // 事件描述核心内容
                "        <p style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                "            <strong>更新人员:</strong> " + user.getFullname() +
                "        </p>" +
                // 事件描述核心内容
                "        <p style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                "            <strong>更新日志:</strong> " + issueUpdate.getStatusUpdate() +
                "        </p>" +
                // 事件描述核心内容
                "        <p style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                "            <strong>更新状态:</strong> " + issue.getIssueStatus().getDicName() +
                "        </p>" +
                // MES系统查看提示（和要求完全一致）
                "        <p style=\"color: #333; font-weight: 500;\">请登录MES系统查看！</p>" +
                "        <p style=\"margin: 10px 0;\">" +
                "            <a href=\"" + homepage + "\" " +
                "               style=\"color: #4a90e2; text-decoration: none;\">" + homepage +
                "            </a>" +
                "        </p>" +
                // 底部系统提示
                "        <p style=\"color: #999; margin-top: 30px; font-size: 12px;\">" +
                "            此邮件为系统自动发送，请勿回复。" +
                "        </p>" +
                "    </div>" +
                "</body>" +
                "</html>";
        mailUtil.sendHtmlMail(to, subject, content);

        return newUpdate;
    }

    /**
     * 获取用户关联事务统计数据
     *
     * @param userId 当前登录用户ID
     * @return 统计结果
     */
    public IssueStatisticsDTO getIssueStatistics(String userId) {
        IssueStatisticsDTO statistics = new IssueStatisticsDTO();

        // 1. 查询该用户关联的所有事务（负责人/执行人=userId，根据业务调整）
        List<MesIssue> issues = issueRepository.findByUserIndexInAnyOwnerFieldOrderByDeadlineDesc(userId);
        if (issues.isEmpty()) {
            return statistics;
        }

        // 2. 统计总数量
        statistics.setTotal(issues.size());

        // 3. 按状态分组统计
        Map<String, Long> statusCountMap = issues.stream()
                .map(issue -> {
                    if (issue.getIssueStatus() == null) {
                        return "未知状态";
                    }
                    return issue.getIssueStatus().getDicName();
                })
                .collect(Collectors.groupingBy(statusName -> statusName, Collectors.counting()));

        // 4. 映射状态统计（注意状态值与前端保持一致）
        statistics.setOpen(statusCountMap.getOrDefault("OPEN", 0L).intValue());
        statistics.setProgress(statusCountMap.getOrDefault("IN PROGRESS", 0L).intValue());
        long overdueCount = issues.stream()
                .filter(issue -> {
                    if (issue.getDeadline() == null) {
                        return false;
                    }
                    LocalDate deadline = issue.getDeadline().toLocalDate();
                    LocalDate now = LocalDate.now();
                    return deadline.isBefore(now);
                })
                .count();
        statistics.setOverdue((int) overdueCount);
        return statistics;
    }

    /**
     * 获取用户关联事务的项目分布数据
     *
     * @param userId 当前登录用户ID
     * @return 项目分布列表
     */
    public List<ProjectDistributionDTO> getProjectDistribution(String userId) {
        // 1. 查询用户关联事务
        List<MesIssue> issues = issueRepository.findByUserIndexInAnyOwnerFieldOrderByDeadlineDesc(userId);
        if (issues.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 按项目名称分组统计数量
        Map<String, Long> projectCountMap = issues.stream()
                .filter(issue -> StringUtils.hasText(issue.getProject().getProjectName()))
                .map(issue -> {
                    if (issue.getProject() == null) {
                        return "未知项目";
                    }
                    return issue.getProject().getProjectName();
                })// 过滤空项目名
                .collect(Collectors.groupingBy(projectName -> projectName, Collectors.counting()));

        // 3. 转换为DTO
        return projectCountMap.entrySet().stream()
                .map(entry -> new ProjectDistributionDTO(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户关联事务的状态占比数据
     *
     * @param userId 当前登录用户ID
     * @return 状态占比列表
     */
    public List<StatusRatioDTO> getStatusRatio(String userId) {
        // 1. 查询用户关联事务
        List<MesIssue> issues = issueRepository.findByUserIndexInAnyOwnerFieldOrderByDeadlineDesc(userId);
        if (issues.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 按状态分组统计数量
        Map<String, Long> statusCountMap = issues.stream()
                .map(issue -> {
                    if (issue.getIssueStatus() == null) {
                        return "未知状态";
                    }
                    return issue.getIssueStatus().getDicName();
                })// 过滤空项目名
                .collect(Collectors.groupingBy(issueStatus -> issueStatus, Collectors.counting()));

        // 3. 转换为DTO（状态名与前端保持一致）
        return statusCountMap.entrySet().stream()
                .map(entry -> new StatusRatioDTO(getStatusDisplayName(entry.getKey()), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户近期事务（按截止日期升序，返回前N条）
     *
     * @param userId 当前登录用户ID
     * @param size   返回条数
     * @return 近期事务列表
     */
    public List<MesIssueDTO> getRecentIssues(String userId, Integer size) {
        // 1. 查询用户关联事务，按截止日期升序排序
        List<MesIssue> issues = issueRepository.findByUserIndexInAnyOwnerFieldOrderByDeadlineDesc(userId);
        if (issues.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 截取前size条
        int limit = Math.min(size, issues.size());
        List<MesIssue> recentIssues = issues.subList(0, limit);

        // 3. 转换为DTO
        return recentIssues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户事务排程
     *
     * @param userId 当前登录用户ID
     * @return 近期事务列表
     */
    public List<MesIssueDTO> getIssuesGantt(String userId) {
        // 1. 查询用户关联事务，按截止日期升序排序
        List<MesIssue> issues = issueRepository.findByUserIndexInActionOwnerFieldOrderByDeadlineAsc(userId);
        if (issues.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 转换为DTO
        return issues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ------------------- 私有工具方法 -------------------

    /**
     * 状态值转显示名（与前端statusMap匹配）
     */
    private String getStatusDisplayName(String status) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("CLOSED", "已关闭");
        statusMap.put("OPEN", "开启");
        statusMap.put("IN PROGRESS", "进行中");
        statusMap.put("VALIDATION", "待验证");
        statusMap.put("PENDING", "待提交");
        return statusMap.getOrDefault(status, status);
    }

    /**
     * 实体转DTO
     */
    @Cacheable(value = "issuesCache", keyGenerator = "issuesKeyGenerator")
    @Transactional(readOnly = true) // 事务内初始化+缓存
    public MesIssueDTO convertToDTO(MesIssue issue) {
        return MesIssueDTO.fromEntity(issue, new ArrayList<>());
    }

    @Cacheable(value = "issuesCache", keyGenerator = "issuesKeyGenerator")
    @Transactional(readOnly = true) // 事务内初始化+缓存
    public MesIssueDTO convertToDTOWithUpdates(MesIssue issue,List<MesIssueUpdate> updateList) {
        return MesIssueDTO.fromEntity(issue, updateList);
    }

    public void genrateIssues() {
        List<MesIssue> allissues = issueRepository.findAllIssues();
        allissues.stream()
                .filter(issue -> (Objects.isNull(issue.getDescription_cn()) || issue.getDescription_cn().trim().isEmpty())
                        || (Objects.isNull(issue.getDescription_en()) || issue.getDescription_en().trim().isEmpty())
                )
                // 过滤出description_cn为空/空白的记录
                .forEach(issue -> {
                    // 先获取原始description（避免重复调用get方法）
                    String originalDesc = issue.getDescription();
                    String translatedDesc_cn = Objects.nonNull(originalDesc) ? baiduTranslateUtil.translate(originalDesc,"auto","zh") : "";
                    String translatedDesc_en = Objects.nonNull(originalDesc) ? baiduTranslateUtil.translate(originalDesc,"auto","en") : "";

                    // 填充description_cn（为空则赋值）
                    if (Objects.isNull(issue.getDescription_cn()) || issue.getDescription_cn().trim().isEmpty()) {
                        issue.setDescription_cn(translatedDesc_cn);
                    }

                    // 填充description_en（为空则赋值）
                    if (Objects.isNull(issue.getDescription_en()) || issue.getDescription_en().trim().isEmpty()) {
                        issue.setDescription_en(translatedDesc_en);
                    }
                    issueRepository.save(issue);
                });
    }

    public void genrateUpdates() {
        List<MesIssueUpdate> allupdates = issueUpdateRepository.findAllUpdates();
        allupdates.stream()
                .filter(update -> (Objects.isNull(update.getUpdate_cn()) || update.getUpdate_cn().trim().isEmpty())
                        || (Objects.isNull(update.getUpdate_en()) || update.getUpdate_en().trim().isEmpty())
                        || (Objects.isNull(update.getStatusUpdate()) || update.getStatusUpdate().trim().isEmpty())
                )
                // 过滤出description_cn为空/空白的记录
                .forEach(update -> {
                    // 先获取原始description（避免重复调用get方法）
                    String originalDesc = update.getStatusUpdate();
                    String translatedDesc_cn = Objects.nonNull(originalDesc) ? baiduTranslateUtil.translate(originalDesc,"auto","zh") : "";
                    String translatedDesc_en = Objects.nonNull(originalDesc) ? baiduTranslateUtil.translate(originalDesc,"auto","en") : "";

                    // 填充description_cn（为空则赋值）
                    if (Objects.isNull(update.getUpdate_cn()) || update.getUpdate_cn().trim().isEmpty()) {
                        update.setUpdate_cn(translatedDesc_cn);
                    }

                    // 填充description_en（为空则赋值）
                    if (Objects.isNull(update.getUpdate_en()) || update.getUpdate_en().trim().isEmpty()) {
                        update.setUpdate_en(translatedDesc_en);
                    }
                    issueUpdateRepository.save(update);
                });
    }
}