package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.model.Customer;
import com.yuanzhang.econexus.model.UserCustomer;
import com.yuanzhang.econexus.service.CustomerService;
import com.yuanzhang.econexus.service.UserCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final UserCustomerService userCustomerService;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer, Authentication authentication) {
        // 设置创建人ID（从当前登录用户获取）
        Customer currentUser = (Customer) authentication.getPrincipal();
        return customerService.createCustomer(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer,
            Authentication authentication) {
        Customer currentUser = (Customer) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(customerService.updateCustomer(id, customer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Customer> searchCustomers(@RequestParam String keyword) {
        return customerService.searchCustomers(keyword);
    }

    // 用户-客户关联接口
    @PostMapping("/{customerId}/users/{userId}")
    public UserCustomer bindUserCustomer(@PathVariable Long customerId, @PathVariable Long userId) {
        return userCustomerService.bindUserCustomer(userId, customerId);
    }

    @DeleteMapping("/{customerId}/users/{userId}")
    public ResponseEntity<Void> unbindUserCustomer(@PathVariable Long customerId, @PathVariable Long userId) {
        userCustomerService.unbindUserCustomer(userId, customerId);
        return ResponseEntity.noContent().build();
    }
}