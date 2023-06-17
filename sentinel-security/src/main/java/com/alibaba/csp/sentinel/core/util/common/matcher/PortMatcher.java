
package com.alibaba.csp.sentinel.core.util.common.matcher;


public class PortMatcher implements Matcher {

    private Integer matcher;

    public PortMatcher() {

    }

    public PortMatcher(Integer matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean match(Object object) {
        if (!(object instanceof Integer)) {
            return false;
        }
        return matcher != null && matcher.equals(object);
    }

    public void setMatcher(Integer matcher) {
        this.matcher = matcher;
    }

}
