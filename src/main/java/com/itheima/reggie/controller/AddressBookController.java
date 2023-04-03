package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 地址簿管理
 * @author Chandler
 * @version 2021.2
 * @date 2023/1/31 16:16
 */
@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        //通过当前线程获取到当前登录用户的id，并添加到输入的信息中
        addressBook.setUserId(BaseContext.getThread());
        log.info("addressBook"+addressBook);
        //使用save方法进行保存
        addressBookService.save(addressBook);
        //返回添加成功的地址对象
        return R.success(addressBook);
    }

    /**
     * 查询当前用户的地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        //获取当前用户id，才能获取到当前的创建用户，创建用户在数据库中为不能空
        addressBook.setUserId(BaseContext.getThread());

        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        //查询
        wrapper.eq(null!=addressBook.getUserId(),AddressBook::getUserId,addressBook.getUserId());
        //排序
        wrapper.orderByAsc(AddressBook::getUpdateTime);
        //查询出结果，并返回
        List<AddressBook> list = addressBookService.list(wrapper);
        return R.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> queryWrapper=new LambdaUpdateWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getThread());
        //先把当前用户所有的地址都改为非默认地址
        queryWrapper.set(AddressBook::getIsDefault,0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(queryWrapper);
        //把选择上的地址设置为默认地址(注意和上面的查询条件两个id是不一样的)
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getThread());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if(addressBook==null){
            return R.error("查询默认地址失败");
        }else {
            return R.success(addressBook);
        }
    }
    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 修改信息
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBookService.removeById(addressBook.getId());
        addressBookService.save(addressBook);
        return R.success("修改地址信息成功");
    }

    /**
     * 删除地址信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> remove(Long ids){
        addressBookService.removeById(ids);
        return R.success("删除地址信息成功");
    }
}
