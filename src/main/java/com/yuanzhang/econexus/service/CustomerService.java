package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.model.Customer;
import com.yuanzhang.econexus.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(Customer customer) {
        // 验证客户代码唯一性
        if (customerRepository.findByCustomerCode(customer.getCustomerCode()).isPresent()) {
            throw new IllegalArgumentException("客户代码已存在");
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customer) {
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setCustomerName(customer.getCustomerName());
                    existing.setAreaCode(customer.getAreaCode());
                    return customerRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.findByCustomerNameContaining(keyword);
    }
}