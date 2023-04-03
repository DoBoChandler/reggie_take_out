package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 客户端用户操作
 * @author Chandler
 * @version 2021.2
 * @date 2023/1/28 19:41
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 发送验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取电话号码
        String phone = user.getPhone();

        //判断电话号码是否为空
        if(StringUtils.isNotEmpty(phone)){
            //生成四位数的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code{}"+code);
            //调用阿里云短信服务的API把验证码发送到手机上
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //将生成的验证码保存到session中
            //session.setAttribute(phone,code);
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码发送成功");
        }
        return R.error("手机验证码发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //获取电话号码
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();
        log.info("code"+code);
        //从之前发送验证码之后存入session中获取里面的code，并进行比对
        //Object codeInSession = session.getAttribute(phone);
        //从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        //验证码对比，发送的验证码和输入的验证码进行对比
        if(codeInSession!=null && codeInSession.equals(code)){
            //如果相同，说明可以登录成功，但需要再验证一次是否为第一次注册
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);

            if(user == null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user);
            //从redis中删除验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
