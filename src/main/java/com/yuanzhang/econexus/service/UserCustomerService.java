package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.model.UserCustomer;
import com.yuanzhang.econexus.repository.UserCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCustomerService {

    private final UserCustomerRepository userCustomerRepository;

    /**
     * 绑定用户与客户（确保唯一关联）
     */
    public UserCustomer bindUserCustomer(Long userId, Long customerId) {
        // 检查是否已存在关联
        if (userCustomerRepository.existsByUserIdAndCustomerId(userId, customerId)) {
            throw new IllegalArgumentException("用户与客户已存在关联");
        }
        UserCustomer userCustomer = new UserCustomer();
        userCustomer.setUserId(userId);
        userCustomer.setCustomerId(customerId);
        return userCustomerRepository.save(userCustomer);
    }

    /**
     * 解绑用户与客户
     */
    public void unbindUserCustomer(Long userId, Long customerId) {
        userCustomerRepository.deleteByUserIdAndCustomerId(userId, customerId);
    }

    /**
     * 根据用户ID查询关联的客户ID列表
     */
    public List<Long> getCustomerIdsByUserId(Long userId) {
        return userCustomerRepository.findByUserId(userId).stream()
                .map(UserCustomer::getCustomerId)
                .toList();
    }

    /**
     * 根据客户ID查询关联的用户ID列表
     */
    public List<Long> getUserIdsByCustomerId(Long customerId) {
        return userCustomerRepository.findByCustomerId(customerId).stream()
                .map(UserCustomer::getUserId)
                .toList();
    }

    /**
     * 检查用户是否属于某个客户
     */
    public boolean isUserInCustomer(Long userId, Long customerId) {
        return userCustomerRepository.existsByUserIdAndCustomerId(userId, customerId);
    }
}