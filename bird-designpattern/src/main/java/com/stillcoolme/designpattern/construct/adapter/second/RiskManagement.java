package com.stillcoolme.designpattern.construct.adapter.second;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:07
 * Description:
 *  对适配器模式的调用2： 统一多个类的接口设计： 现在有多个过滤敏感词的三方应用，需要适配！
 *
 *  使用 适配器模式 扩展性更好，更加符合开闭原则，如果添加一个新的敏感词过滤系统，
 *  这个类完全不需要改动；而且基于接口而非实现编程，代码的可测试性更好
 */
public class RiskManagement {
    private List<ISensitiveWordsFilter> filters = new ArrayList<>();

    public void addFilter(ISensitiveWordsFilter filter) {
        filters.add(filter);
    }

    public String filterSensitiveWord(String text) {
        String maskedText = text;
        for(ISensitiveWordsFilter filter: filters) {
            filter.filter(maskedText);
        }
        return maskedText;
    }
}
