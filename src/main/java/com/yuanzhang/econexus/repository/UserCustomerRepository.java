package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.UserCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCustomerRepository extends JpaRepository<UserCustomer, Long> {
    List<UserCustomer> findByUserId(Long userId);
    List<UserCustomer> findByCustomerId(Long customerId);
    void deleteByUserIdAndCustomerId(Long userId, Long customerId);
    boolean existsByUserIdAndCustomerId(Long userId, Long customerId);
}