package com.bookmanager.service;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.dto.LoginDTO;
import com.bookmanager.dto.RegisterDTO;
import com.bookmanager.entity.User;
import com.bookmanager.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogService auditLogService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!checkPassword(loginDTO.getPassword(), user.getPassword(), loginDTO.getUsername())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 密码校验：优先 BCrypt，若数据库仍为明文则兼容（并自动升级为 BCrypt）。
     */
    private boolean checkPassword(String rawPassword, String storedPassword, String username) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        // 兼容历史明文数据
        if (rawPassword.equals(storedPassword)) {
            upgradePasswordToBcrypt(username, rawPassword);
            return true;
        }
        return false;
    }

    /**
     * 将明文密码升级为 BCrypt 哈希（在登录时自动完成）。
     */
    private void upgradePasswordToBcrypt(String username, String rawPassword) {
        try {
            User u = userMapper.findByUsername(username);
            if (u != null) {
                userMapper.updatePassword(u.getId(), passwordEncoder.encode(rawPassword));
            }
        } catch (Exception ignored) {}
    }

    private String currentRequestRole() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest().getAttribute("role") != null) {
                return attrs.getRequest().getAttribute("role").toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Integer currentRequestUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest().getAttribute("userId") != null) {
                return (Integer) attrs.getRequest().getAttribute("userId");
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void register(RegisterDTO registerDTO) {
        if (userMapper.findByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setName(registerDTO.getName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);
    }

    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public List<User> findPage(int page, int pageSize) {
        return userMapper.findPage((page - 1) * pageSize, pageSize);
    }

    public long count() {
        return userMapper.count();
    }

    public void update(User user) {
        // 双保险：即使绕过拦截器，Service 层也校验权限
        String currentRole = currentRequestRole();
        Integer currentUserId = currentRequestUserId();
        // 非管理员只能修改自己，且不能改角色
        if (!"admin".equals(currentRole)) {
            if (user.getId() == null || !user.getId().equals(currentUserId)) {
                throw new RuntimeException("无权修改其他用户信息");
            }
            user.setRole(null); // 不允许普通用户提升角色
        }
        // 管理员不能把自己的角色降级/改掉（防止锁死系统）
        if (user.getId() != null && user.getId().equals(currentUserId) && user.getRole() != null
                && !"admin".equals(user.getRole()) && "admin".equals(currentRole)) {
            throw new RuntimeException("不能修改自己的管理员角色");
        }
        userMapper.update(user);
        auditLog(user.getId(), "UPDATE", "USER", "更新用户信息");
    }

    public void updateStatus(Integer id, Integer status) {
        userMapper.updateStatus(id, status);
        User u = userMapper.findById(id);
        auditLog(id, "UPDATE", "USER", (status == 1 ? "启用" : "禁用") + "用户「" + (u != null ? u.getUsername() : "") + "」");
    }

    public void updateAvatar(Integer id, String avatar) {
        userMapper.updateAvatar(id, avatar);
    }

    public void deleteById(Integer id) {
        User u = userMapper.findById(id);
        userMapper.deleteById(id);
        if (u != null) auditLog(id, "DELETE", "USER", "删除用户「" + u.getUsername() + "」");
    }

    public void batchDelete(List<Integer> ids) {
        userMapper.batchDelete(ids);
        auditLog(null, "DELETE", "USER", "批量删除 " + ids.size() + " 个用户");
    }

    private void auditLog(Integer targetId, String action, String targetType, String detail) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                Object userId = attrs.getRequest().getAttribute("userId");
                Object username = attrs.getRequest().getAttribute("username");
                String ip = attrs.getRequest().getRemoteAddr();
                if (username != null) {
                    auditLogService.save(
                        userId != null ? (Integer) userId : 0,
                        username.toString(), action, targetType, targetId, detail, ip
                    );
                }
            }
        } catch (Exception ignored) {}
    }
}
