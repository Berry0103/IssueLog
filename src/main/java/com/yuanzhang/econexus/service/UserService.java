package com.yuanzhang.econexus.service;

import com.yuanzhang.econexus.model.*;
import com.yuanzhang.econexus.repository.DepartmentRepository;
import com.yuanzhang.econexus.repository.DictionaryRepository;
import com.yuanzhang.econexus.repository.RoleRepository;
import com.yuanzhang.econexus.repository.UserRepository;
import com.yuanzhang.econexus.util.ExmailSendUtil;
import com.yuanzhang.econexus.util.JwtTokenUtil;
import com.yuanzhang.econexus.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final DictionaryRepository dictionaryRepository;
    private final ExmailSendUtil mailUtil;
    private final PasswordUtil passwordUtil;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${app.reset-password.front-end-url}")
    private String frontEndResetUrl;
    @Value("${server.url}")
    private String homepage;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createUser(User user) {
        // 在保存前，确保所有关联的实体都被正确加载或设置
        // 这里假设前端传来的是ID，我们需要将其转换为实体引用
        if (user.getDepartment() != null && user.getDepartment().getDeptIndex() != null) {
            Optional<Department> deptOpt = departmentRepository.findById(user.getDepartment().getDeptIndex());
            deptOpt.ifPresent(user::setDepartment);
        }
        if (user.getRoler() != null && user.getRoler().getRolerIndex() != null) {
            Optional<Role> roleOpt = roleRepository.findById(user.getRoler().getRolerIndex());
            roleOpt.ifPresent(user::setRoler);
        }
        if (user.getUserState() != null && user.getUserState().getDicIndex() != null) {
            Optional<Dictionary> dicOpt = dictionaryRepository.findById(user.getUserState().getDicIndex());
            dicOpt.ifPresent(user::setUserState);
        }

        String psd = passwordUtil.generate12BitPassword();
        String psd_encode = passwordEncoder.encode(psd);
        user.setPassword(psd_encode);
        user.setLanguage("zh");

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String[] to = new String[1];
            to[0] = user.getEmail();

            String subject = "【账号创建】您的MES账号创建成功";
//            String content = "您的MES账号已创建！<br><br>";
//            content = content + "用户名:" + user.getUsername() + "<br><br>";
//            content = content + "初始密码:" + psd + "<br><br>";
//            content = content + "请登录MES系统查看！" + "<br>";
//            content = content + "http://mes.yuanzhangtechs.com";
            String content = "<!DOCTYPE html>" +
                    "<html lang=\"zh-CN\">" +
                    "<head>" +
                    "    <meta charset=\"UTF-8\">" +
                    "    <title>MES账号创建提醒</title>" +
                    "</head>" +
                    "<body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                    "        <h2 style=\"color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;\">MES账号创建成功</h2>" +
                    "        <p>尊敬的用户：</p>" +
                    "        <p>您的MES账号已创建！</p>" +
                    // 账号信息模块（突出显示）
                    "        <div style=\"margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #4a90e2;\">" +
                    "            <p style=\"margin: 8px 0;\"><strong>用户名:</strong> " + user.getUsername() + "</p>" +
                    "            <p style=\"margin: 8px 0; font-size: 16px; font-weight: bold; color: #4a90e2;\"><strong>初始密码:</strong> " + psd + "</p>" +
                    "        </div>" +
                    // 安全提醒
                    "        <p><strong>安全提醒：</strong>请首次登录后立即修改初始密码，确保账号安全。</p>" +
                    // MES系统查看提示（和要求完全一致）
                    "        <hr style=\"border: none; border-top: 1px solid #eee; margin: 25px 0;\">" +
                    "        <p style=\"color: #333; font-weight: 500;\">请登录MES系统查看！</p>" +
                    "        <p style=\"margin: 10px 0;\">" +
                    "            <a href=\"" + homepage + "\" " +
                    "               style=\"color: #4a90e2; text-decoration: none;\">" + homepage +
                    "            </a>" +
                    "        </p>" +
                    // 底部系统提示
                    "        <p style=\"color: #999; margin-top: 30px; font-size: 12px;\">" +
                    "            此邮件为系统自动发送，请勿回复。" +
                    "        </p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
            mailUtil.sendHtmlMail(to, subject, content);
        }

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getUserIndex())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getFirstname() != null && !user.getFirstname().isEmpty()) {
            existingUser.setFirstname(user.getFirstname());
        }
        if (user.getLastname() != null && !user.getLastname().isEmpty()) {
            existingUser.setLastname(user.getLastname());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getLanguage() != null && !user.getLanguage().isEmpty()) {
            existingUser.setLanguage(user.getLanguage());
        }
        if (user.getLastLogin() != null && !Objects.nonNull(user.getLastLogin())) {
            existingUser.setLastLogin(user.getLastLogin());
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public User getUserById(String userindex) {
        return userRepository.findById(userindex)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public boolean sendPasswordResetLink(String userId, String email) {
        try {
            // 1. 根据用户ID查询用户名（实际项目中从数据库查询）
            User user = getUserById(userId);
            if (user.getUsername() == null) {
                return false;
            }

            if ( user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().equals(email)) {
                return false;
            }

            // 2. 生成重置令牌
            String resetToken = jwtTokenUtil.generateResetToken(userId);

            // 3. 构建重置链接（前端页面 + 令牌参数）
            String resetLink = frontEndResetUrl + "?token=" + resetToken;

            // 4. 发送重置邮件
            String[] to = new String[1];
            to[0] = email;
            String subject = "【密码重置】您的MES账号密码重置链接";
            String content = "<!DOCTYPE html>" +
                    "<html lang=\"zh-CN\">" +
                    "<head>" +
                    "    <meta charset=\"UTF-8\">" +
                    "    <title>密码重置</title>" +
                    "</head>" +
                    "<body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                    "        <h2 style=\"color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;\">密码重置请求</h2>" +
                    "        <p>尊敬的 " + user.getUsername() + "：</p>" +
                    "        <p>您请求重置账号密码，请点击下方链接完成重置：</p>" +
                    "        <p style=\"margin: 20px 0;\">" +
                    "            <a href=\"" + resetLink + "\" " +
                    "               style=\"background-color: #4a90e2; color: white; padding: 10px 20px; " +
                    "                      text-decoration: none; border-radius: 5px;\">重置密码</a>" +
                    "        </p>" +
                    "        <p><strong>注意：</strong>该链接有效期为30分钟，过期后请重新申请。</p>" +
                    "        <p>如果不是您本人操作，请忽略此邮件，您的账号安全不会受到影响。</p>" +
                    "        <p style=\"color: #999; margin-top: 30px; font-size: 12px;\">" +
                    "            此邮件为系统自动发送，请勿回复。" +
                    "        </p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
            mailUtil.sendHtmlMail(to, subject, content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 验证重置令牌并重置密码
     */
    public boolean resetPassword(String token,String newPassword) {
        // 1. 验证令牌有效性
        if (!jwtTokenUtil.isTokenValid(token)) {
            return false;
        }

        // 2. 解析令牌获取用户ID
        String userId = jwtTokenUtil.extractUserIdFromToken(token);
        if (userId == null) {
            return false;
        }

        // 3. 重置密码
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String psd_encode = passwordEncoder.encode(newPassword);
        existingUser.setPassword(psd_encode);
        userRepository.save(existingUser);
        String[] to = new String[1];
        to[0] = existingUser.getEmail();
        String subject = "【密码重置成功】您的MES账号密码已更新";
        String content = "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <title>密码重置成功</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <h2 style=\"color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;\">密码重置成功</h2>" +
                "        <p>尊敬的 " + existingUser.getUsername() + "：</p>" +
                "        <p>您的账号密码已成功重置</p>" +
                "        <p><strong>安全提醒：</strong></p>" +
                "        <ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "            <li>请使用新密码登录</li>" +
                "            <li>请勿将密码告知他人，确保账号安全</li>" +
                "            <li>如果不是您本人操作，请立即联系管理员</li>" +
                "        </ul>" +
                "        <hr style=\"border: none; border-top: 1px solid #eee; margin: 25px 0;\">" +
                "        <p style=\"color: #333; font-weight: 500;\">请登录MES系统查看！</p>" +
                "        <p style=\"margin: 10px 0;\">" +
                "            <a href=\"" + homepage + "\" " +
                "               style=\"color: #4a90e2; text-decoration: none;\">" + homepage +
                "            </a>" +
                "        </p>" +
                "        <p style=\"color: #999; margin-top: 30px; font-size: 12px;\">" +
                "            此邮件为系统自动发送，请勿回复。" +
                "        </p>" +
                "    </div>" +
                "</body>" +
                "</html>";
        mailUtil.sendHtmlMail(to, subject, content);
        System.out.println("用户 " + userId + " 重置密码为：" + newPassword);

        return true;
    }
}
