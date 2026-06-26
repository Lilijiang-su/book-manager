package com.bookmanager.service;

import com.bookmanager.entity.Category;
import com.bookmanager.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AuditLogService auditLogService;

    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    public List<Category> findTree() {
        List<Category> all = categoryMapper.findAll();
        List<Category> roots = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
                buildChildren(c, all);
            }
        }
        return roots;
    }

    private void buildChildren(Category parent, List<Category> all) {
        List<Category> children = new ArrayList<>();
        for (Category c : all) {
            if (parent.getId().equals(c.getParentId())) {
                children.add(c);
                buildChildren(c, all);
            }
        }
        if (!children.isEmpty()) {
            parent.setChildren(children);
        }
    }

    public List<Category> findEnabled() {
        List<Category> all = categoryMapper.findAll();
        List<Category> result = new ArrayList<>();
        for (Category c : all) {
            if (c.getStatus() == 1) {
                result.add(c);
            }
        }
        return result;
    }

    public void insert(Category category) {
        if (category.getSortOrder() == null) category.setSortOrder(0);
        if (category.getStatus() == null) category.setStatus(1);
        categoryMapper.insert(category);
        auditLog(category.getId(), "CREATE", "CATEGORY", "添加分类「" + category.getName() + "」");
    }

    public void update(Category category) {
        categoryMapper.update(category);
        auditLog(category.getId(), "UPDATE", "CATEGORY", "更新分类「" + category.getName() + "」");
    }

    public void updateSortOrder(Integer id, Integer sortOrder) {
        categoryMapper.updateSortOrder(id, sortOrder);
    }

    public void updateStatus(Integer id, Integer status) {
        categoryMapper.updateStatus(id, status);
        Category c = categoryMapper.findById(id);
        auditLog(id, "UPDATE", "CATEGORY", (status == 1 ? "启用" : "禁用") + "分类");
    }

    public void deleteById(Integer id) {
        Category c = categoryMapper.findById(id);
        categoryMapper.deleteById(id);
        if (c != null) auditLog(id, "DELETE", "CATEGORY", "删除分类「" + c.getName() + "」");
    }

    public void batchDelete(List<Integer> ids) {
        categoryMapper.batchDelete(ids);
        auditLog(null, "DELETE", "CATEGORY", "批量删除 " + ids.size() + " 个分类");
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
