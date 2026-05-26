package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pm_user")
public class User implements UserDetails {

    @Id
    @UuidGenerator
    @Column(name = "user_index", nullable = false)
    private String userIndex; // 用户唯一标识

    @Column(name = "user_name", nullable = false, length = 64)
    private String username; // 用户名 (登录用)

    @Column(name = "password", nullable = false, length = 128)
    private String password; // 密码

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department", referencedColumnName = "dept_index")
    private Department department; // 所属部门

    @Column(name = "e_mail", length = 128)
    private String email; // 邮箱

    @Column(name = "firstname", length = 32)
    private String firstname; // 名

    @Column(name = "lastname", length = 32)
    private String lastname; // 姓

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roler_index", referencedColumnName = "roler_index")
    private Role roler; // 角色

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_state", referencedColumnName = "dic_index")
    private Dictionary userState; // 用户状态

    @Column(name = "language", length = 6)
    private String language; // 语言

    @Column(name = "last_login", columnDefinition = "datetime(0)")
    private LocalDateTime lastLogin; // 最后登录时间

    @Column(name = "qywx_id", length = 16)
    private String qywxId; // 企业微信

    // 计算并返回用户的全名
    @Transient
    public String getFullname() {
        return lastname + firstname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
