package com.stillcoolme.designpattern.construct.adapter.second;

/**
 * Author: stillcoolme
 * Date: 2020/3/12 21:00
 * Description:
 */
public class ASensitiveWordsFilterAdaptor implements ISensitiveWordsFilter {

    private ASensitiveWordsFilter aFilter;

    @Override
    public String filter(String text) {
        String maskedText = aFilter.filterPoliticalWords(text);
        maskedText = aFilter.filterSexyWords(maskedText);
        return maskedText;
    }

}
