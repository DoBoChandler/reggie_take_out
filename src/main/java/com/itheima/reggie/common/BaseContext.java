package com.itheima.reggie.common;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/22 22:27
 * 基于ThreadLocal封装工具类，方便前面使用获取当前线程中的属性
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void setThread(Long id){
        threadLocal.set(id);
    }

    public static Long getThread(){
        Long id = threadLocal.get();
        return id;
    }
}
