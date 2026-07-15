package com.zhangxin.service;

import com.zhangxin.entity.Category;
import com.zhangxin.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务：负责系统分类和用户自定义分类的查询、去重与维护。
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public Category findById(Integer id) {
        return categoryMapper.findById(id);
    }

    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    public List<Category> findByType(String type) {
        return categoryMapper.findByType(type);
    }

    /**
     * 查询当前用户可用分类。数据库中可能同时存在系统分类、用户分类或演示数据，
     * 这里按“类型 + 名称”去重，避免前端下拉框重复显示。
     */
    public List<Category> findUserCategories(Integer userId) {
        List<Category> source = categoryMapper.findSystemAndUserCategories(userId);
        Map<String, Category> deduped = new LinkedHashMap<>();

        for (Category category : source) {
            String key = buildDedupKey(category);
            Category existing = deduped.get(key);

            if (existing == null || shouldPrefer(category, existing)) {
                deduped.put(key, category);
            }
        }

        return new ArrayList<>(deduped.values());
    }

    @Transactional
    public Category add(Category category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(99);
        }

        if (categoryMapper.checkNameExists(category.getName(), category.getUserId()) > 0) {
            throw new RuntimeException("分类名称已存在");
        }

        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public void update(Category category) {
        Category existing = categoryMapper.findById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }

        if (!existing.getName().equals(category.getName())
                && categoryMapper.checkNameExists(category.getName(), category.getUserId()) > 0) {
            throw new RuntimeException("分类名称已存在");
        }

        categoryMapper.update(category);
    }

    @Transactional
    public void delete(Integer id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        if (categoryMapper.countBillsByCategoryId(id) > 0) {
            throw new RuntimeException("该分类已被账单使用，请先修改相关账单后再删除");
        }

        categoryMapper.delete(id);
    }

    private String buildDedupKey(Category category) {
        String type = category.getType() == null ? "" : category.getType().trim().toLowerCase();
        String name = category.getName() == null ? "" : category.getName().trim().toLowerCase();
        return type + "::" + name;
    }

    private boolean shouldPrefer(Category candidate, Category existing) {
        if (existing.getUserId() == null && candidate.getUserId() != null) {
            return true;
        }

        Integer existingSort = existing.getSortOrder() == null ? Integer.MAX_VALUE : existing.getSortOrder();
        Integer candidateSort = candidate.getSortOrder() == null ? Integer.MAX_VALUE : candidate.getSortOrder();
        return candidateSort < existingSort;
    }
}
