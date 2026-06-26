package com.bookmanager.service;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.dto.LoginDTO;
import com.bookmanager.dto.RegisterDTO;
import com.bookmanager.entity.User;
import com.bookmanager.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public void register(RegisterDTO registerDTO) {
        if (userMapper.findByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
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
