package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2023/2/3 21:20
 */
@Service
public class OrdersImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户id，查询当前用户的购物车数据
        Long userId = BaseContext.getThread();
        LambdaQueryWrapper<ShoppingCart> cartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(cartLambdaQueryWrapper);
        if (shoppingCarts==null||shoppingCarts.size()==0){
            throw new CustomException("当前购物车为空，无法下单");
        }
        //根据当前登录的用户id，查询用户数据，用于获取用户名字插入到后面的order表中
        User user = userService.getById(userId);
        //根据地址id，查询地址数据
        LambdaQueryWrapper<AddressBook> addressLambdaQueryWrapper=new LambdaQueryWrapper<>();
        addressLambdaQueryWrapper.eq(AddressBook::getId,orders.getAddressBookId());
        AddressBook addressBook = addressBookService.getOne(addressLambdaQueryWrapper);
        if(addressBook==null){
            throw new CustomException("当前用户地址为空,无法配送");
        }
        //生成当前订单id
        long orderId = IdWorker.getId();
        //原子整型，防止多线程不安全性，初始值设置为0
        AtomicInteger amount = new AtomicInteger(0);
        //组装订单明细数据，批量保存订单数据
        List<OrderDetail> orderDetailList = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            //总金额数=单个菜品金额数×单品的数量。addAndGet:遍历一次叠加一次
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        //组装订单数据，批量保存订单数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());//设置用户名
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向orders表中插入一条订单信息
        //ordersService.save(orders);
        this.save(orders);
        //向orderDetail表中插入多条信息
        orderDetailService.saveBatch(orderDetailList);
        //删除当前用户的购物车列表数据
        shoppingCartService.remove(cartLambdaQueryWrapper);
    }
}
