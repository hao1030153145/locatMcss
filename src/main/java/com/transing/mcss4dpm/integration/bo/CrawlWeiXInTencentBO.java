package com.transing.mcss4dpm.integration.bo;

import java.util.Date;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/4/18
 */
public class CrawlWeiXInTencentBO {
    private String id;
    private String url;
    private String parent;
    private String author;
    private String uid;
    private String icon;
    private String from;
    private String content;
    private String uniqueValue;
    private Date datetime;
    private Date crawltime;
    private int ltimes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Date getCrawltime() {
        return crawltime;
    }

    public void setCrawltime(Date crawltime) {
        this.crawltime = crawltime;
    }

    public int getLtimes() {
        return ltimes;
    }

    public void setLtimes(int ltimes) {
        this.ltimes = ltimes;
    }
}
