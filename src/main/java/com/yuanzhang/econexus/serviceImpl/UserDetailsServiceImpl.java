package com.yuanzhang.econexus.serviceImpl;

import com.yuanzhang.econexus.model.Role;
import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据用户名查询用户，同时加载关联的角色信息
        Optional<User> userOptional = userRepository.findByUserNameWithDetails(username);

        // 2. 检查用户是否存在
        User pmUser = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("用户不存在: " + username)
        );

        // 3. 检查用户状态是否为激活
        // 假设 "active" 代表用户处于激活状态
        if ("LEAVE".equalsIgnoreCase(pmUser.getUserState().getDicName())) {
            throw new UsernameNotFoundException("用户已被禁用或锁定: " + username);
            // 更精确的异常可以使用:
            // throw new DisabledException("用户已被禁用");
            // throw new LockedException("用户已被锁定");
        }

        // 4. 将用户的角色转换为 Spring Security 所需的 GrantedAuthority 列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role roler = pmUser.getRoler();
        if (roler != null) {
            // 通常，角色名称需要以 "ROLE_" 开头
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roler.getRolerName().toUpperCase()));
        }
        // 如果一个用户可以有多个角色，这里需要遍历角色列表

        // 5. 构建并返回 UserDetails 对象
        // User 是 Spring Security 提供的一个 UserDetails 实现类
        return new org.springframework.security.core.userdetails.User(
                pmUser.getUsername(),          // 用户名
                pmUser.getPassword(),          // 数据库中存储的加密后的密码
                true,                          // 账户是否未过期
                true,                          // 凭证是否未过期
                true,                          // 账户是否未锁定
                !"LEAVE".equalsIgnoreCase(pmUser.getUserState().getDicName()),    // 账户是否启用
                authorities                    // 权限列表
        );
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }
}
