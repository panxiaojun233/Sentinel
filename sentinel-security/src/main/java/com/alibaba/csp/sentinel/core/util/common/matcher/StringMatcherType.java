

package com.alibaba.csp.sentinel.core.util.common.matcher;


public enum StringMatcherType {

    /**
     * exact match.
     */
    EXACT("exact"),
    /**
     * prefix match.
     */
    PREFIX("prefix"),
    /**
     * suffix match.
     */
    SUFFIX("suffix"),
    /**
     * present match.
     */
    PRESENT("present"),
    /**
     * regex match.
     */
    REGEX("regex"),
    /**
     * contain match.
     */
    CONTAIN("contain");

    /**
     * type of matcher.
     */
    public final String type;

    StringMatcherType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

}
