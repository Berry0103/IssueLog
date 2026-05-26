package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.model.Department;
import com.yuanzhang.econexus.model.Dictionary;
import com.yuanzhang.econexus.model.Project;
import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.DepartmentRepository;
import com.yuanzhang.econexus.repository.DictionaryRepository;
import com.yuanzhang.econexus.repository.ProjectRepository;
import com.yuanzhang.econexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommonDataService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private CacheManager cacheManager; // 缓存管理器（如Caffeine/Redis）

    // 缓存key前缀
    private static final String CACHE_KEY_PREFIX = "dic:";

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Dictionary> getDictionariesByTypeName(String typeName) {
//        return dictionaryRepository.findByDictype_DictypeNameOrderByDicNumAsc(typeName);
        String cacheKey = CACHE_KEY_PREFIX + typeName;
        Cache cache = cacheManager.getCache("dicCache");

        List<Dictionary> dicMap = cache.get(cacheKey, List.class);
        if (dicMap != null && dicMap.size() > 0) {
            return dicMap;
        }
        dicMap = dictionaryRepository.findByDictypeIndex(typeName);
        // 存入缓存（设置过期时间，如1小时）
        cache.put(cacheKey, dicMap);
        return dicMap;
    }
}