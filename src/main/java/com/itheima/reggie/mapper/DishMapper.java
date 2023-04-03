package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品：先是用来在删除种类的时候是否关联了菜品
 *
 * @author Chandler*/
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
