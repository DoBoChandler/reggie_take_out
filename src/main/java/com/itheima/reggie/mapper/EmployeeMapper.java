package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/14 14:59
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
