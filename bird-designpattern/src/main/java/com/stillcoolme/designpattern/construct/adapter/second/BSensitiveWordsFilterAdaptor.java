package com.stillcoolme.designpattern.construct.adapter.second;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:02
 * Description:
 */
public class BSensitiveWordsFilterAdaptor implements ISensitiveWordsFilter {
    private BSensitiveWordsFilter bFilter;

    @Override
    public String filter(String text) {
        return bFilter.filter(text);
    }
}
