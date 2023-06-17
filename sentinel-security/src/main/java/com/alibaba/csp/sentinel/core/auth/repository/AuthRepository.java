package com.alibaba.csp.sentinel.core.auth.repository;


import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.core.auth.rule.AuthRule;
import com.alibaba.csp.sentinel.core.auth.rule.JwtRule;


/**
 * 规则库
 */
public class AuthRepository {

    private Map<String, AuthRule> allowAuthRules = new HashMap<>();

    private Map<String, AuthRule> denyAuthRules = new HashMap<>();

    private Map<String, JwtRule> jwtRules = new HashMap<>();

    public AuthRepository() {

    }


    public AuthRepository(Map<String, AuthRule> allowAuthRules, Map<String, AuthRule> denyAuthRules, Map<String, JwtRule> jwtRules) {
        this.allowAuthRules = allowAuthRules;
        this.denyAuthRules = denyAuthRules;
        this.jwtRules = jwtRules;
    }

    public Map<String, AuthRule> getAllowAuthRules() {
        return allowAuthRules;
    }

    public Map<String, AuthRule> getDenyAuthRules() {
        return denyAuthRules;
    }

    public void setDenyAuthRules(Map<String, AuthRule> denyAuthRules) {
        this.denyAuthRules = denyAuthRules;
    }

    public Map<String, JwtRule> getJwtRules() {
        return jwtRules;
    }

    public void setAllowAuthRule(Map<String, AuthRule> allowAuthRules) {
        this.allowAuthRules = allowAuthRules;
    }

    public void setJwtRule(Map<String, JwtRule> jwtRules) {
        this.jwtRules = jwtRules;
    }

    @Override
    public String toString() {
        return "AuthRepository{" + "allowAuthRules=" + allowAuthRules + ", denyAuthRules=" + denyAuthRules + ", jwtRules=" + jwtRules + '}';
    }
}
