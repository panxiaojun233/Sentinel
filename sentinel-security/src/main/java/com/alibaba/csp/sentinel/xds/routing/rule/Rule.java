
package com.alibaba.csp.sentinel.xds.routing.rule;


public abstract interface Rule {

    /**
     * get type of rule.
     *
     * @return String
     */
    String getType();

    /**
     * get condition.
     *
     * @return String
     */
    String getCondition();

    /**
     * set condition.
     *
     * @param condition {@link String}
     */
    void setCondition(String condition);

    /**
     * get key of rule.
     *
     * @return String
     */
    String getKey();

    /**
     * set key of rule.
     *
     * @param key {@link String}
     */
    void setKey(String key);

    /**
     * get value of rule.
     *
     * @return String
     */
    String getValue();

    /**
     * set value of rule.
     *
     * @param value {@link String}
     */
    void setValue(String value);

}
