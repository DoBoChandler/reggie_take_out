package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/22 21:44
 * 字段自动填充：
 *  1、在前面实体类界面加上注解@TableField(fill = FieldFill.INSERT_UPDATE)，表示哪些字段是要填充的
 *  2、加入这个类，表示哪些是需要填充的字段，当执行插入或者是更新的时候会自动填填充
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
      metaObject.setValue("createTime", LocalDateTime.now());
      metaObject.setValue("updateTime", LocalDateTime.now());
      metaObject.setValue("createUser", BaseContext.getThread());
      metaObject.setValue("updateUser", BaseContext.getThread());
      //metaObject.setValue("isDeleted",0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
      metaObject.setValue("updateTime", LocalDateTime.now());
      metaObject.setValue("updateUser", BaseContext.getThread());
    }
}
