package com.stillcoolme.framework.self.di.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stillcoolme.framework.self.di.BeanConfigParser;
import com.stillcoolme.framework.self.di.BeanDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:47
 * Function:
 *  Class type = Class.forName("java.lang." + args.get("type")); 这句写死了。。。
 */
public class XmlBeanConfigParser implements BeanConfigParser {

    @Override
    public List<BeanDefinition> parse(String configLocation) {
        InputStream is = null;
        is = this.getClass().getResourceAsStream("/" + configLocation);
        if(is == null) {
            throw new RuntimeException("Can not find config file: " + configLocation);
        }
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String content = "";
        String temp = "";
        try {
            while ((temp = bufferedReader.readLine()) != null) {
                content += temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isr.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        JSONObject jsonObject = (JSONObject) JSON.parse(content);
        JSONObject beans = (JSONObject) jsonObject.get("beans");
        JSONArray beanArray = beans.getJSONArray("bean");
        List<BeanDefinition> list = new ArrayList<>();
        for (int i = 0; i < beanArray.size(); i++) {
            BeanDefinition beanDefinition = new BeanDefinition();
            JSONObject bean = (JSONObject) beanArray.get(i);
            beanDefinition.setId((String) bean.get("id"));
            beanDefinition.setClassName((String) bean.get("class"));

            String lazyInit = (String) bean.get("lazy-init");
            if (lazyInit != null && lazyInit.equals("true")) {
                beanDefinition.setLazyInit(true);
            }
            String scope = (String) bean.get("scope");
            if (scope != null && lazyInit.equals("prototype")) {
                beanDefinition.setScope(BeanDefinition.Scope.PROTOTYPE);
            }

            JSONArray argJsonObject = (JSONArray) bean.get("constructor-arg");
            List<BeanDefinition.ConstructorArg> constructorArgs = new ArrayList();
            for (int j = 0; j < argJsonObject.size(); j++) {
                JSONObject args = (JSONObject) argJsonObject.get(j);
                String ref = (String) args.get("ref");
                String type = (String) args.get("type");
                String value = (String) args.get("value");
                try {
                    BeanDefinition.ConstructorArg constructorArg = BeanDefinition.ConstructorArg
                            .newBuilder().setIsRef(ref != null ? true : false)
                            .setType(type != null ? Class.forName("java.lang." + args.get("type")) : null)
                            .setArg(ref != null ? ref :
                                        (value != null ? value : null)
                            )
                            .doBuilder();
                    constructorArgs.add(constructorArg);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            beanDefinition.setConstructorArgs(constructorArgs);
            list.add(beanDefinition);
        }
        return list;
    }

    public static void main(String[] args) {
        XmlBeanConfigParser xmlBeanConfigParser = new XmlBeanConfigParser();
        xmlBeanConfigParser.parse("beans.json");
    }
}
