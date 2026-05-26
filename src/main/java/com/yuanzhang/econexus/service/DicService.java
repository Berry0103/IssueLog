package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.dto.DicDTO;
import com.yuanzhang.econexus.mapper.DicMapper;
import com.yuanzhang.econexus.model.Dictionary;
import com.yuanzhang.econexus.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DicService {
    @Autowired
    private DicMapper dicMapper;

    @Autowired
    private CacheManager cacheManager; // 缓存管理器（如Caffeine/Redis）

    // 缓存key前缀
    private static final String CACHE_KEY_PREFIX = "dic:";

    @Autowired
    private DictionaryRepository dictionaryRepository;

    // 获取指定类型的所有字典（带缓存）
    public List<Dictionary> getDicByType(String typeName) {
        String cacheKey = CACHE_KEY_PREFIX + typeName;
        Cache cache = cacheManager.getCache("dicCache");

        List<Dictionary> dicMap = cache.get(cacheKey, List.class);
        if (dicMap != null) {
            return dicMap;
        }
        dicMap = dictionaryRepository.findByDictypeIndex(typeName);
        // 存入缓存（设置过期时间，如1小时）
        cache.put(cacheKey, dicMap);
        return dicMap;
    }

    // 字典更新后清空缓存（如新增/修改字典时调用）
    public void refreshDicCache(String typeCode) {
        String cacheKey = CACHE_KEY_PREFIX + typeCode;
        cacheManager.getCache("dicCache").evict(cacheKey);
    }
}

