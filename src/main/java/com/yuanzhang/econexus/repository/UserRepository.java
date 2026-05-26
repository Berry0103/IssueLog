package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roler LEFT JOIN FETCH u.department LEFT JOIN FETCH u.userState LEFT JOIN FETCH u.userState.dictype WHERE 1=1")
    @Override
    List<User> findAll();

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 根据用户名查询用户（用于登录验证）
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roler LEFT JOIN FETCH u.department LEFT JOIN FETCH u.userState LEFT JOIN FETCH u.userState.dictype WHERE u.userIndex = :userIndex")
    @Override
    Optional<User> findById(@Param("userIndex") String userIndex);

    // 可以根据需要添加自定义查询，例如根据部门查询用户
    List<User> findByDepartment_DeptIndex(String deptIndex);

    /**
     * 根据用户名查询用户，并通过fetch join加载关联的角色和部门信息
     * @param userName 用户名
     * @return 包含角色和部门信息的用户实体
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roler LEFT JOIN FETCH u.department LEFT JOIN FETCH u.userState LEFT JOIN FETCH u.userState.dictype WHERE u.username = :userName")
    Optional<User> findByUserNameWithDetails(@Param("userName") String userName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roler LEFT JOIN FETCH u.department LEFT JOIN FETCH u.userState LEFT JOIN FETCH u.userState.dictype WHERE u.userState.dicIndex <> :dicIndex")
    List<User> findByUserStateDicIndexNot(@Param("dicIndex") String dicIndex);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.username = :username")
    void updateLastLoginByUserIndex(@Param("lastLogin")LocalDateTime lastLogin,
                                    @Param("username") String username);

    List<User> findUsersByEmail(String email);
}
