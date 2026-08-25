package com.bookmanager.controller;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.common.PageResult;
import com.bookmanager.common.Result;
import com.bookmanager.dto.LoginDTO;
import com.bookmanager.dto.RegisterDTO;
import com.bookmanager.entity.User;
import com.bookmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.getUserId(token);
        return Result.success(userService.findById(userId));
    }

    @GetMapping("/list")
    public Result<?> list() {
        List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<User> records = userService.findPage(page, pageSize);
        records.forEach(u -> u.setPassword(null));
        long total = userService.count();
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }

    @PostMapping("/avatar")
    public Result<?> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        try {
            String token = request.getHeader("Authorization").substring(7);
            Integer userId = jwtUtil.getUserId(token);
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;
            file.transferTo(new File(uploadDir + filename));
            String avatarUrl = "/avatars/" + filename;
            userService.updateAvatar(userId, avatarUrl);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<?> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        userService.batchDelete(body.get("ids"));
        return Result.success("批量删除成功");
    }
}
