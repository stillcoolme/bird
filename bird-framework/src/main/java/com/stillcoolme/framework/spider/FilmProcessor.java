package com.stillcoolme.framework.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/11/14 13:32
 * @description:
 */
public class FilmProcessor implements PageProcessor {

    public static final String URL = "http://www.dytt8.net";

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        //解析列表页
        if(URL.equals(page.getUrl().toString())) {
            List<Selectable> contentNodes = html.xpath("//div[@class='co_content2']/ul/a").nodes();
            for (int i = 1; i < contentNodes.size(); i++) {
                //第一条过滤，从第二条开始遍历
                Selectable linkNode = contentNodes.get(i);
                if(linkNode == null) {
                    continue;
                }
                String linkTmp = linkNode.links().get();
                if(linkTmp != null && linkTmp.length() > 0) {
                    // 将找到的链接放到 TargetRequest，会自动发起请求
                    page.addTargetRequest(linkTmp);
                    // 输出到控制台
                    System.out.println(linkTmp);
                }
            }
        } else { //解析电影详情页面
            //获取html
            Selectable movieNames = html.xpath("//title/text()");
            Selectable movieDownloads = html.xpath("//a[starts-with(@href,'ftp')]/text()");
            System.out.println("movieName: " + movieNames.get());
            System.out.println("downloadURL: " + movieDownloads.get());
            System.out.println("===============");

        }


    }

    @Override
    public Site getSite() {
        return Site.me().setTimeOut(10000);
    }
}
