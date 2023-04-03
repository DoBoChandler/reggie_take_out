package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/23 5:00
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        //创建分页对象
        Page<Category> pageInfo=new Page<>(page,pageSize);
        //创建条件查询对象
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //加入分页条件(排序)
        queryWrapper.orderByAsc(Category::getSort);
        //调用mp中的分页查询放大
        Page pages = categoryService.page(pageInfo, queryWrapper);
        return R.success(pages);
    }
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("添加成功");
    }
    @DeleteMapping
    public R<String> remove(Long id){
        categoryService.remove(id);
        return R.success("删除成功");
    }
    @PutMapping
    public R<String> updateById(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
