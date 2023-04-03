package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2023/2/2 21:32
 */
@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //先查看当时是哪个用户的购物车
        Long userId = BaseContext.getThread();
        shoppingCart.setUserId(userId);
        //用于判断是否为dishid
        Long dishId = shoppingCart.getDishId();
        //用于判断是否为套餐id
        Long setmealId = shoppingCart.getSetmealId();
        //判断当前添加的菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(null !=dishId){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        //获取购物车对象
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
        if(null!=cartServiceOne){
            //不为空说明购物车中已经存在了当前选择的菜品或者套餐
            Integer number = cartServiceOne.getNumber();
            //此时我们获取数量字段，只需要把数量字段加一就可以了(默认是0)
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //为空说明购物车中没有,直接手动添加1，在进行保存操作
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //把这个独享直接付给根据条件查询的对象，用于之后的返回
            cartServiceOne=shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 减少商品数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> delete(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getThread();
        shoppingCart.setUserId(userId);
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        if(null != dishId){
            wrapper.eq(ShoppingCart::getDishId,dishId);
        }
        if(null != setmealId){
            wrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(wrapper);
        //上面和添加时的思想是一样的
        //这里需要判断购物车中菜品或者套餐的数量，数量大于1就该数字，小于1就直接删除
        Integer number = cartServiceOne.getNumber();
        if(number>1){
            cartServiceOne.setNumber(number-1);
            cartServiceOne.setCreateTime(LocalDateTime.now());
            //大于1就修改数字
            shoppingCartService.updateById(cartServiceOne);

        }else {
            //小于1就直接删除
            shoppingCartService.remove(wrapper);
        }
        return R.success("删除成功");
    }
    /**
     * 查看购物车列表
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车列表信息");
        //添加查询条件
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //根据userId查询购物车的信息：select * from shopping_cart where userid= ?
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getThread());
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //返回查询出来的列表
        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //通过userId删除---select * from shopping_cart where UserId=?
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getThread());
        //根据用户id删除而不是根据购物车id删除，所以不能用removById
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功");
    }
}
