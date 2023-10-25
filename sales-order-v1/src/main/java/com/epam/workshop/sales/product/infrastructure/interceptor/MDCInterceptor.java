package com.epam.workshop.sales.product.infrastructure.interceptor;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class MDCInterceptor implements HandlerInterceptor {

    public static final String MDC_USER_CODE = "MDC_user_code";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (MDC.get(MDC_USER_CODE) == null) {
            MDC.put(MDC_USER_CODE, UUID.randomUUID().toString());
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(MDC_USER_CODE);
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
