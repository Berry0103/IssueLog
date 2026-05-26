package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRolerName(String rolerName);
}
    