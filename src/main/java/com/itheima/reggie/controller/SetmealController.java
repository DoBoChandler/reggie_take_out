package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2023/1/26 18:54
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐同时保存套餐和菜品的关系
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        log.info("套餐信息"+setmealDto);
        return R.success("添加套餐成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> pageDto=new Page<>(page,pageSize);
        //添加查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByAsc(Setmeal::getUpdateTime);
        //把分页信息和条件封装进去
        setmealService.page(pageInfo,queryWrapper);
        //拷贝数据
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        //获取记录
        List<Setmeal> records = pageInfo.getRecords();
        //添加上分类名字，获得新的记录
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //把原来的记录拷贝到dto上(此时是没有分类名字的)
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        //新的分页对象设置上新的记录
        pageDto.setRecords(list);
        return R.success(pageDto);
    }

    /**
     * 批量修改起售状态
     * @param st
     * @param ids
     * @return
     */
    @PostMapping("/status/{st}")
    public R<String> status(@PathVariable int st,@RequestParam List<Long> ids){
        log.info("ids"+ids);
        //遍历集合，添加条件在重新组成集合，之后作为参数添加到修改中
        List<Setmeal> list = ids.stream().map((item) -> {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(item);
            setmeal.setStatus(st);
            return setmeal;
        }).collect(Collectors.toList());
        setmealService.updateBatchById(list);
        return R.success("修改成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 显示套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //添加条件
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //两个请求参数
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.orderByAsc(Setmeal::getUpdateTime);
        //查询
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return R.success(list);
    }
}
