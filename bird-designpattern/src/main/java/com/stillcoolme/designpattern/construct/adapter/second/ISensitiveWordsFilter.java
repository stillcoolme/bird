package com.stillcoolme.designpattern.construct.adapter.second;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 20:59
 * Description:
 *   统一的适配器接口定义
 */
public interface ISensitiveWordsFilter {
    String filter(String text);
}
