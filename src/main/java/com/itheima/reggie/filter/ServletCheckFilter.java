package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chandler
 * @version 2021.2
 * @date 2022/11/16 9:14
 */
@WebFilter(filterName = "servletCheckFilter", urlPatterns = "/*")
@Slf4j
public class ServletCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        //2、判断本次请求是否需要处理，定义不需要处理的请求路径
        String[] urls={
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/sendMsg",//移动端发短信
            "/user/login"   //移动端登录
        };
        //3、如果不需要处理就直接放行
        boolean check = check(urls, requestURI);
        if(check){
            log.info("本次请求不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4-1、判断是否为登录状态，如果是登录状态则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("登录状态");
            Long employeeId=(Long) request.getSession().getAttribute("employee");
            BaseContext.setThread(employeeId);
            filterChain.doFilter(request,response);
            return;
        }
        //4-2、判断是否为登录状态，如果是登录状态则直接放行
        User user=(User) request.getSession().getAttribute("user");
        if(user!=null){
            log.info("登录状态");
            BaseContext.setThread(user.getId());
            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
        //5、如果未登录则返回未登录结果,以输出流的方式像页面返回结果
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        //System.out.println("拦截到的请求{}"+httpServletRequest.getRequestURI());
        //log.info("拦截到的请求{}"+request.getRequestURI());

    }
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
