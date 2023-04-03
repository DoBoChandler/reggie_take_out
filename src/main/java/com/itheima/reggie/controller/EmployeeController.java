package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/14 15:12
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1、将页面提交的密码password进行md5加密处理
        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提供的信息获取用户名username查询数据库
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(wrapper);
        //3、如果没有查询到结果就显示失败结果
        if(null==emp){
            return R.error("登陆失败");
        }
        //4、密码比对，如果不一样则显示失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //5、查看员工状态是否是被禁用了
        if(emp.getStatus()==0){
            return R.error("账号已经被禁用");
        }
        //6、登陆成功，把账号密码存储到session中
        request.getSession().setAttribute("employee",emp.getId());
        System.out.println(request.getSession().getAttribute("employee"));
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清楚当前登录的员工的session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){

        //初始化密码为123456，使用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //添加前端没有添加的几个属性，如下：
       /* employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //获得大当前登录用户的id
        Long employeeId = (Long)request.getSession().getAttribute("employee");
        employee.setCreateUser(employeeId);
        employee.setUpdateUser(employeeId);*/
        employeeService.save(employee);
        return R.success("添加成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //输出一个日志
        log.info("page{},pageSize,name"+page,pageSize,name);
        //构建分页器
        Page pageInfo=new Page(page,pageSize);
        //创建条件分页
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<>();
        //创建模糊条件查询条件
        wrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //创建排序条件
        wrapper.orderByAsc(Employee::getUpdateTime);
        //使用mp中的分页查询方法，里面传上上面的两个参数
        Page page1 = employeeService.page(pageInfo, wrapper);
        return R.success(page1);
    }
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //获取当前用户id
       /* Long employeeId = (Long)request.getSession().getAttribute("employee");
        //设置当前更新时间
        employee.setUpdateTime(LocalDateTime.now());
        //修改设置当前更新的用户
        employee.setUpdateUser(employeeId);*/
        //通过id修改信息
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    /**
     * 回显数据，通过id查询当前数据
     * */
    @GetMapping("/{id}")
    public R<Employee> queryById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到相关信息");
    }
}
