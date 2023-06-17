package com.alibaba.csp.sentinel.core.inter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthTlsWebValidatorInterface {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception;
}
