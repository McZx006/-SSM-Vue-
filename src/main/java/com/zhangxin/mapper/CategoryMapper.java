package com.zhangxin.mapper;

import com.zhangxin.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper。
 */
public interface CategoryMapper {

    Category findById(@Param("id") Integer id);

    List<Category> findAll();

    List<Category> findByType(@Param("type") String type);

    List<Category> findByUserId(@Param("userId") Integer userId);

    /**
     * 查询系统预设分类和当前用户自定义分类。
     */
    List<Category> findSystemAndUserCategories(@Param("userId") Integer userId);

    int insert(Category category);

    int update(Category category);

    int delete(@Param("id") Integer id);

    int checkNameExists(@Param("name") String name, @Param("userId") Integer userId);

    int countBillsByCategoryId(@Param("categoryId") Integer categoryId);
}
