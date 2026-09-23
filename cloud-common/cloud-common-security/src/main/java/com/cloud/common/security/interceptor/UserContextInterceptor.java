package com.cloud.common.security.interceptor;

import com.cloud.common.core.constant.CommonConstants;
import com.cloud.common.security.context.UserContext;
import com.cloud.common.security.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 从网关注入的可信请求头中填充 {@link UserContext}。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(CommonConstants.HEADER_USER_ID);
        String username = request.getHeader(CommonConstants.HEADER_USERNAME);
        if (StringUtils.hasText(userId) || StringUtils.hasText(username)) {
            LoginUser loginUser = new LoginUser();
            if (StringUtils.hasText(userId)) {
                loginUser.setUserId(Long.valueOf(userId));
            }
            loginUser.setUsername(username);
            UserContext.set(loginUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
