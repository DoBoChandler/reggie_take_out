package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2023/1/18 20:42
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        /*Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/
        Set keys = redisTemplate.keys("dish_" + dishDto.getId() + "_1");
        redisTemplate.delete(keys);
        return R.success("添加菜品成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //添加分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //创建一个dishDto的分页构造器，因为dish上面没有商品类别的字段，所以我们要使用dto，把上面的数据通过dto封装完成之后，再放入分页构造器中完成使用dto的分页
        Page<DishDto> dishDtoPage=new Page<>();
        //添加查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加name的模糊查询
        lambdaQueryWrapper.like(name!=null,Dish::getName,name);
        //按照创建时间进行排序
        lambdaQueryWrapper.orderByAsc(Dish::getUpdateTime);
        //进行相应的分页条件查询
        Page<Dish> dishPage = dishService.page(pageInfo, lambdaQueryWrapper);
        //将dish的数据拷贝到dishDto
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //获取原来dishPage的records(因为后面需要添加到categoryName再重新封装到dishDto的records)
        List<Dish> records = dishPage.getRecords();
        //使用Lambda表达式进行遍历、添加字段、在重新封装到list集合中
        List<DishDto> list=records.stream().map(( item )->{
            DishDto dishDto=new DishDto();
            Long categoryId = item.getCategoryId();
            //根据id查询，再通过封装类获取name
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            BeanUtils.copyProperties(item,dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
    @RequestMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品，并修改对应的口味
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        Set keys = redisTemplate.keys("dish_" + dishDto.getId() + "_1");
        redisTemplate.delete(keys);
        return R.success("添加菜品成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteByIds(String ids){
        //通过字符串的形式，接收前端的批量字符串请求，通过把逗号删除获得每一段id
        String[] split = ids.split(",");
        //使用Arrays的stream流把每一个id截取掉前后空格，再放入到list集合当中，获取到long行的id集合
        List<Long> lists = Arrays.stream(split).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        //通过mp中的批量删除的方法把获取到的id以集合的形式作为参数进行删除
        dishService.removeByIds(lists);
        log.info("删除的批量id："+ids);
        return R.success("删除成功");
    }

    /**
     * 批量修改状态
     * @param st
     * @param ids
     * @return
     */
    @PostMapping("/status/{st}")
    public R<String> setStatus(@PathVariable int st,String ids){
        //获取url上的ids，截取掉逗号，放入数组中
        String[] split = ids.split(",");
        //stream流把所有的id重新放到数组中
        List<Long> idList = Arrays.stream(split).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        //遍历集合，new一个dish对象，设置上每一个id，并设置上url上传来的状态码，重新放到一个集合中
        List<Dish> dishList = idList.stream().map((item) -> {
            Dish dish = new Dish();
            dish.setId(item);
            dish.setStatus(st);
            return dish;
        }).collect(Collectors.toList());
        //在mp中的批量修改当法中，通过api可知，我们需要的是一个实体类的集合参数，所以我们要通过上面的内容来获取到这个集合参数
        dishService.updateBatchById(dishList);
        return R.success("修改状态成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //添加查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //只查询已经起售的菜品，值为1是起售
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        //按照sort进行排序
        lambdaQueryWrapper.orderByAsc(Dish::getSort);
        //根据id查询，返回list集合
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList =null;
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
         dishDtoList=(List<DishDto>) redisTemplate.opsForValue().get(key);
         if(dishDtoList!=null){
             return R.success(dishDtoList);
         }
        //添加查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //只查询已经起售的菜品，值为1是起售
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        //按照sort进行排序
        lambdaQueryWrapper.orderByAsc(Dish::getSort);
        //根据id查询，返回list集合
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        dishDtoList=list.stream().map((item) -> {
            //当前前端的页面菜品选择上是缺少口味信息的，所以我们需要使用dishDto
            DishDto dishDto = new DishDto();
            //将原来的信息拷贝到dto对象上
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            //通过菜品查找到当前的分类对象
            Category category = categoryService.getById(categoryId);
            if (null != category) {
                String categoryName = category.getName();
                //设置分类名称
                dishDto.setCategoryName(categoryName);
            }
            Long dishId = item.getId();
            //查询出dishId，通过这个id在dishFlavor表上查找出相应的口味信息
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
            //把口味信息添加到dto对象中
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
