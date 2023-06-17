package com.alibaba.csp.sentinel.core.auth.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.util.common.matcher.Matcher;

public class AuthCondition {

    private static final Logger log = LoggerFactory.getLogger(AuthCondition.class);
    private ValidationType type;
    private String key;
    private Matcher matcher;

    public AuthCondition() {

    }

    public AuthCondition(ValidationType type, Matcher matcher) {
        this.type = type;
        this.matcher = matcher;
    }

    public AuthCondition(ValidationType type, String key, Matcher matcher) {
        this(type, matcher);
        this.key = key;
    }

    public ValidationType getType() {
        return type;
    }

    public void setType(ValidationType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public enum ValidationType {

        /**
         * All types of auth validation.
         */
        HEADER, SOURCE_IP, REMOTE_IP, DEST_IP, REQUEST_PRINCIPALS, AUTH_AUDIENCES, AUTH_CLAIMS, AUTH_PRESENTERS, HOSTS, PATHS, PORTS, METHODS, IDENTITY

    }

}
