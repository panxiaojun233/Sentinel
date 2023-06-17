

package com.alibaba.csp.sentinel.core.auth.rule;


import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.core.auth.condition.AuthCondition;


public class AuthRule {

    private RuleOperation op = RuleOperation.UNKNOWN;
    private List<AuthRule> children = new ArrayList<>();
    private AuthCondition condition;
    private boolean isNot;

    public AuthRule() {

    }

    public AuthRule(RuleOperation op) {
        this.op = op;
    }

    public AuthRule(RuleOperation op, boolean isNot) {
        this(op);
        this.isNot = isNot;
    }

    public AuthRule(AuthCondition condition) {
        this.condition = condition;
    }

    public AuthRule(AuthCondition condition, boolean isNot) {
        this(condition);
        this.isNot = isNot;
    }

    public void addChildren(AuthRule rule) {
        children.add(rule);
    }

    public boolean isEmpty() {
        if (children.isEmpty()) {
            return condition == null;
        }
        return false;
    }

    public boolean isLeaf() {
        return condition != null;
    }

    public RuleOperation getOp() {
        return op;
    }

    public void setOp(RuleOperation op) {
        this.op = op;
    }

    public List<AuthRule> getChildren() {
        return children;
    }

    public void setChildren(List<AuthRule> children) {
        this.children = children;
    }

    public AuthCondition getCondition() {
        return condition;
    }

    public void setCondition(AuthCondition condition) {
        this.condition = condition;
    }

    public boolean isNot() {
        return isNot;
    }

    public void setNot(boolean not) {
        isNot = not;
    }

    public enum RuleOperation {

        /**
         * In what way are subrules connected.
         */
        UNKNOWN, AND, OR

    }

}
