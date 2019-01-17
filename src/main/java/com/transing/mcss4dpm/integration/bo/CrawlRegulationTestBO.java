package com.transing.mcss4dpm.integration.bo;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/1/25 0025.
 */
public class CrawlRegulationTestBO implements Serializable {

    private int scriptId;

    private String item;

    private String description;

    private String type;

    private String length;

    private String isNull;

    private int step;

    private int dataFrom;

    private String crawlParam;

    private String crawlArray;

    private String afterProcessorArray;

    public int getScriptId() {
        return scriptId;
    }

    public void setScriptId(int scriptId) {
        this.scriptId = scriptId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getIsNull() {
        return isNull;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public int getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(int dataFrom) {
        this.dataFrom = dataFrom;
    }

    public String getCrawlParam() {
        return crawlParam;
    }

    public void setCrawlParam(String crawlParam) {
        this.crawlParam = crawlParam;
    }

    public String getCrawlArray() {
        return crawlArray;
    }

    public void setCrawlArray(String crawlArray) {
        this.crawlArray = crawlArray;
    }

    public String getAfterProcessorArray() {
        return afterProcessorArray;
    }

    public void setAfterProcessorArray(String afterProcessorArray) {
        this.afterProcessorArray = afterProcessorArray;
    }
}
