package com.stillcoolme.designpattern.construct.adapter.second;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:03
 * Description:
 */
public class CSensitiveWordsFilterAdaptor implements ISensitiveWordsFilter {
    private CSensitiveWordsFilter cFilter;

    @Override
    public String filter(String text) {
        String mask = "";
        return cFilter.filter(text, mask);
    }
}
