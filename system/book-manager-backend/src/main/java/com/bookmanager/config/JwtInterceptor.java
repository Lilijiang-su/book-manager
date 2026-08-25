package com.bookmanager.config;

import com.bookmanager.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 仅管理员可访问的路径前缀（管理操作）。
     * 普通用户访问这些路径将被拒绝。
     */
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/user/list",
            "/api/user/page",
            "/api/user/status",
            "/api/user/update",
            "/api/user/delete",
            "/api/user/batch-delete",
            "/api/book/add",
            "/api/book/update",
            "/api/book/status",
            "/api/book/delete",
            "/api/book/batch-delete",
            "/api/book/batch-category",
            "/api/category/add",
            "/api/category/update",
            "/api/category/status",
            "/api/category/sort",
            "/api/category/delete",
            "/api/category/batch-delete",
            "/api/audit/list",
            "/api/audit/page",
            "/api/fine-rule"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // GET /api/fine-rule 为公开只读接口，无需登录
        if ("/api/fine-rule".equals(request.getRequestURI())
                && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录或token已过期");
        }
        token = token.substring(7);
        jwtUtil.verifyToken(token);
        // 注入操作用户信息到request，供Service层使用
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));

        // 管理路径角色校验：仅 admin 可访问
        String role = jwtUtil.getRole(token);
        if (isAdminPath(request) && !"admin".equals(role)) {
            throw new AdminAccessDeniedException("无权限执行该操作");
        }
        return true;
    }

    private boolean isAdminPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // /api/fine-rule：GET 登录即可（读规则），PUT 需 admin（修改规则）
        if ("/api/fine-rule".equals(path)) {
            return "PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)
                    || "DELETE".equalsIgnoreCase(method);
        }
        // 精确匹配管理操作前缀
        for (String adminPath : ADMIN_PATHS) {
            if (path.equals(adminPath) || path.startsWith(adminPath + "/")) {
                return true;
            }
        }
        // 路径变量形式的删除操作：/api/user/{id}、/api/book/{id}、/api/category/{id}
        // 仅当 method 为 DELETE 时视为管理操作（DELETE 语义上属于管理写操作）
        if ("DELETE".equalsIgnoreCase(method)) {
            if (path.matches("^/api/(user|book|category)/\\d+$")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 管理员访问被拒异常。
     */
    public static class AdminAccessDeniedException extends RuntimeException {
        public AdminAccessDeniedException(String message) {
            super(message);
        }
    }
}
