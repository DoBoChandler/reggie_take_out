package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/23 04:57
 */
public interface CategoryService extends IService<Category> {
    /**
     * 重写一下删除方法，之后用这个自己定义的
     * */
    public void remove(Long id);
}
