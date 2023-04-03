package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/26 17:05
 */
public interface SetmealService extends IService<Setmeal> {

    void saveWithDish(SetmealDto setmealDto);

    void deleteWithDish(List<Long> ids);
}
