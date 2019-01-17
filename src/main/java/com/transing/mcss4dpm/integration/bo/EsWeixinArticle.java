package com.transing.mcss4dpm.integration.bo;


import java.util.Date;

public class EsWeixinArticle {
    private int projectID;
    private String detailId;
    private String uniqueValue; //url+projectId md5加密
    private String url;
//    private String imgs;
    private String image;
    private int ltimes; //点赞数
    private int vtimes; //阅读数
    private Date crawltime;
    private Date datetime;  //发布时间
    private String title;
    private String author;
    private String source;  //来源
    private String uid;     //微信Id
    private String htimes;     //热度
    private String tempurl;     //原微信链接
    private String content;
    private String commentlist;

//    private int projectID;
//    private String detailId;
//    private String uniqueValue; //url+projectId md5加密
//    private String title;
//    private String url;
//    private String author;
//    private String content;
//    private String favorabletimes;
//    private String viewtimes;
//    private Date crawltime;
//    private String commentlist;


    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLtimes() {
        return ltimes;
    }

    public void setLtimes(int ltimes) {
        this.ltimes = ltimes;
    }

    public int getVtimes() {
        return vtimes;
    }

    public void setVtimes(int vtimes) {
        this.vtimes = vtimes;
    }

    public Date getCrawltime() {
        return crawltime;
    }

    public void setCrawltime(Date crawltime) {
        this.crawltime = crawltime;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHtimes() {
        return htimes;
    }

    public void setHtimes(String htimes) {
        this.htimes = htimes;
    }

    public String getTempurl() {
        return tempurl;
    }

    public void setTempurl(String tempurl) {
        this.tempurl = tempurl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCommentlist() {
        return commentlist;
    }

    public void setCommentlist(String commentlist) {
        this.commentlist = commentlist;
    }
}
