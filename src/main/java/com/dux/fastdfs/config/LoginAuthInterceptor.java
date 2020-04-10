package com.dux.fastdfs.config;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginAuthInterceptor extends HandlerInterceptorAdapter {

    private static final String APP_TOKEN = "appToken";

    private static final String ACCOUNT = "amazingmq";

    private static final String PASSWORD = "qqwwee";

    @Override
    public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2)
            throws Exception {
        if ("/index".equals(arg0.getRequestURI()) || "/login".equals(arg0.getRequestURI())) {
            return true;}
        //重定向
        Object object = arg0.getSession().getAttribute("users");
        if (null == object) {
            arg1.sendRedirect("/login");
            return false;}
        return true;
    }

    private boolean validSysToken(HttpServletRequest request) {

        String token = request.getHeader(APP_TOKEN);
        if (StringUtils.isBlank(token)) {
            return false;
        }
        return true;
    }

    private void sendRestResponse(ServletRequest request, ServletResponse response, JSONObject result) {

        WebUtil.sendRestResponse(request, response, result);
    }
}
