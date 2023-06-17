package com.alibaba.csp.sentinel.xds.filiter;

public interface XdsResolveFilter<T> {

    boolean resolve(T t);

    String getTypeUrl();

}
