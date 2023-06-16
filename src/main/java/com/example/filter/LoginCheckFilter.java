package com.example.filter;

import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //拦截请求
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取请求的url
        String requestURI = request.getRequestURI();

        //判断此url是否需要拦截,无需处理则直接放行
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        if (YesOrNo(urls, requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        //判断是否登录，如果登录则放行
        Long EmployeeID = (Long) request.getSession().getAttribute("employee");
        if (EmployeeID != null) {
            //放行
            log.info("开始判断，当前后台session：{}",EmployeeID);
            BaseContext.setCurrentId(EmployeeID);
            filterChain.doFilter(request, response);
            return;
        }

        //用户端
        Long UserID = (Long) request.getSession().getAttribute("user");
        log.info("开始判断，当前session{}",UserID);
        if (UserID != null) {
            //放行
            BaseContext.setCurrentId(UserID);
            filterChain.doFilter(request, response);
            return;
        }


        //未登录，返回信息，告知登录
        log.info("当前拦截路径为：{}",request.getRequestURI());
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean YesOrNo(String[] urls, String url) {
        for (String s : urls) {
            if (PATH_MATCHER.match(s, url)) {
                return true;
            }
        }
        return false;
    }
}
