package com.transing.mcss4dpm.web.controller;

import com.alibaba.fastjson.JSON;
import com.jeeframework.util.encrypt.MD5Util;
import com.jeeframework.webframework.exception.WebException;
import com.transing.mcss4dpm.JobEvent.Bo.McssTask;
import com.transing.mcss4dpm.JobEvent.Bo.ScriptTask;
import com.transing.mcss4dpm.biz.service.TaskService;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXinArticleBO;
import com.transing.mcss4dpm.integration.bo.EsWeixinArticle;
import com.transing.mcss4dpm.integration.bo.SubTask;
import com.transing.mcss4dpm.integration.bo.Task;
import com.transing.mcss4dpm.util.Base64Util;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import com.transing.mcss4dpm.util.DateUtil;
import com.transing.mcss4dpm.util.WebUtil;
import com.transing.mcss4dpm.web.exception.MySystemCode;
import com.transing.mcss4dpm.web.po.DevicesPO;
import com.transing.mcss4dpm.web.po.preservePO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/25
 */
@Controller("weiXinController")
@Api(value = "微信存储", description = "微信存储相关访问接口", position = 2)
@RequestMapping(path = "/weixin")
public class WeiXinController {
    @Resource
    private TaskService taskService;


    @RequestMapping(value = "/preserveWeiXinCommentData.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存抓取微信文章评论数据", position = 0)
    public preservePO preserveWeiXinCommentData(
            @RequestParam(value = "taskId", required = true) @ApiParam String subTaskId,
            @RequestParam(value = "jsonParam", required = true) @ApiParam String jsonParam,
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        String storageTypeTable = getDataTableName(datasourceTypeId);
        if (storageTypeTable != null) {
            SubTask subTask = taskService.getSubTaskBySubId(subTaskId);
            List<Task> taskList = taskService.getTaskByTaskId(subTask.getTaskId());
            if (taskList != null && taskList.size() > 0) {
                Task task = taskList.get(0);
                String projectId = task.getProjectId();
                String getFlowId = task.getFlowId();
                String detailId = task.getDetailId();

                com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(jsonParam);
                List<Map<String, Object>> mapListJson = (List) jsonArray;
                for (Map<String, Object> map : mapListJson) {
                    map.put("detailId", detailId);
                    map.put("projectID", Integer.parseInt(projectId));
                    map.put("crawltime", new Date());
                }
                String dataJSON = JSONArray.fromObject(mapListJson).toString();
                //es存储
                String corpusServer = WebUtil.getEsServerByEnv();
                String getCorpusDataUrl = "/addDataInSearcher.json";
                Map<String, String> corpusNameMap = new HashMap<String, String>();
                corpusNameMap.put("dataType", storageTypeTable);
                corpusNameMap.put("dataJSON", dataJSON);
                System.out.println("XXXX: preserveWeiXinCommentData_storageTypeTable : "+storageTypeTable);
                CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), corpusServer + getCorpusDataUrl, "post", corpusNameMap);
                System.out.println("XXXX: preserveWeiXinCommentData 2");
                //回调dpm
                String dpmServer = WebUtil.getDpmServerByEnv();
                String getAcceptCallbackUrl = "/acceptCallback.json";

                Map<String, String> dpmMap = new HashMap<String, String>();
                dpmMap.put("detailId", dataJSON);
                dpmMap.put("projectId", projectId);
                dpmMap.put("flowId", getFlowId);
                dpmMap.put("progress", "0");
                dpmMap.put("status", "2");
                dpmMap.put("errorMessage", "");
                dpmMap.put("num", "1");
                dpmMap.put("dataJsonArray", dataJSON);
                CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + getAcceptCallbackUrl, "post", dpmMap);
                System.out.println("XXXX: preserveWeiXinCommentData 3");
            }
        } else {
            throw new WebException(MySystemCode.QUERY_TABLE_NULL);
        }
        return new preservePO();
    }

    @RequestMapping(value = "/preserveWeiXinArticleData.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存抓取微信文章文章数据", position = 0)
    public preservePO preserveWeiXinArticleData(
            @RequestParam(value = "taskId", required = true) @ApiParam String subTaskId,
            @RequestParam(value = "jsonParam", required = true) @ApiParam String jsonParam,
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        System.out.println("XXXX: remote_mcss : preserveWeiXinData "+datasourceTypeId);
        CrawlWeiXinArticleBO crawlWeiXinArticleBO = JSON.parseObject(jsonParam, CrawlWeiXinArticleBO.class);
        //根据数据源类型查看存储表名
        String storageTypeTable = getDataTableName(datasourceTypeId);
        if (storageTypeTable != null) {
            //根据taskId获取task_crawl任务信息
            SubTask subTask = taskService.getSubTaskBySubId(subTaskId);
            List<Task> taskList = taskService.getTaskByTaskId(subTask.getTaskId());
            if (taskList != null && taskList.size() > 0) {
                Task task = taskList.get(0);
                String projectId = task.getProjectId();
                String firstDetailId = task.getFirstDetailId();
                String detailId = task.getDetailId();
                // 存储es  test
                List<EsWeixinArticle> esWeixinArticleList = new ArrayList<>();
                EsWeixinArticle esWeixinArticle = new EsWeixinArticle();
                esWeixinArticle.setUrl(crawlWeiXinArticleBO.getUrl());
                esWeixinArticle.setLtimes(Integer.parseInt(crawlWeiXinArticleBO.getReplytimes()));
                esWeixinArticle.setVtimes(Integer.parseInt(crawlWeiXinArticleBO.getViewtimes()));
                System.out.println("xxxx: " + "firstDetailId: " + firstDetailId);
                System.out.println("xxxx: " + "crawlWeiXinArticleBO.getFormerUrl(): " + crawlWeiXinArticleBO.getFormerUrl());
                System.out.println("xxxx: " + "projectId: " + projectId);
                System.out.println("xxxx: " + "projectId: " + projectId);
                System.out.println("xxxx: " + "original: " + (firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
                System.out.println("xxxx: " + "MD5Util: " + MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
                esWeixinArticle.setUniqueValue(MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
                esWeixinArticle.setCrawltime(new Date());
                esWeixinArticle.setAuthor(crawlWeiXinArticleBO.getAuthor());
                esWeixinArticle.setSource(crawlWeiXinArticleBO.getSource());
                esWeixinArticle.setUid(crawlWeiXinArticleBO.getWeixinId());
                esWeixinArticle.setTempurl(crawlWeiXinArticleBO.getFormerUrl());
                esWeixinArticle.setContent(crawlWeiXinArticleBO.getContent());
                esWeixinArticle.setTitle(crawlWeiXinArticleBO.getTitle());
                esWeixinArticle.setProjectID(Integer.parseInt(projectId));
                esWeixinArticle.setImage(crawlWeiXinArticleBO.getImgUrl());
                esWeixinArticle.setDetailId(detailId);
                System.out.println("xxxx: " + "remote_imgUrl: " + crawlWeiXinArticleBO.getImgUrl());
                int lTimes = crawlWeiXinArticleBO.getReplytimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getReplytimes());
                int vTimes = crawlWeiXinArticleBO.getViewtimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getViewtimes());
                esWeixinArticle.setHtimes((vTimes + lTimes * 500) + "");
                if (crawlWeiXinArticleBO.getPublishTime() != null) {
                    esWeixinArticle.setDatetime(DateUtil.parseDate(crawlWeiXinArticleBO.getPublishTime()));
                }
                String commentStr = JSONArray.fromObject(crawlWeiXinArticleBO.getCommentlist()).toString();
                String commentBase64 = Base64Util.getBase64(commentStr);
                System.out.println("XXXX: remote_mcss : commentStr " + commentStr);
                System.out.println("XXXX: remote_mcss : commentBase64 " + commentBase64);
                esWeixinArticle.setCommentlist(commentBase64);
                // 存储es  idc

//                List<EsWeixinArticle> esWeixinArticleList = new ArrayList<>();
//                EsWeixinArticle esWeixinArticle = new EsWeixinArticle();
//                esWeixinArticle.setUrl(crawlWeiXinArticleBO.getUrl());
//                esWeixinArticle.setLtimes(Integer.parseInt(crawlWeiXinArticleBO.getReplytimes()));
//                esWeixinArticle.setVtimes(Integer.parseInt(crawlWeiXinArticleBO.getViewtimes()));
//                esWeixinArticle.setUniqueValue(MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
//                esWeixinArticle.setCrawltime(new Date());
//                esWeixinArticle.setAuthor(crawlWeiXinArticleBO.getAuthor());
//                esWeixinArticle.setSource(crawlWeiXinArticleBO.getSource());
//                esWeixinArticle.setUid(crawlWeiXinArticleBO.getWeixinId());
//                esWeixinArticle.setTempurl(crawlWeiXinArticleBO.getFormerUrl());
//                esWeixinArticle.setContent(crawlWeiXinArticleBO.getContent());
//                esWeixinArticle.setTitle(crawlWeiXinArticleBO.getTitle());
//                esWeixinArticle.setProjectID(Integer.parseInt(projectId));
//                esWeixinArticle.setImage(crawlWeiXinArticleBO.getImgUrl());
//                esWeixinArticle.setDetailId(detailId);
//                System.out.println("xxxx: " + "remote_imgUrl: " + crawlWeiXinArticleBO.getImgUrl());
//                int lTimes = crawlWeiXinArticleBO.getReplytimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getReplytimes());
//                int vTimes = crawlWeiXinArticleBO.getViewtimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getViewtimes());
//                esWeixinArticle.setHtimes((vTimes + lTimes * 500) + "");
//                if (crawlWeiXinArticleBO.getPublishTime() != null) {
//                    esWeixinArticle.setDatetime(DateUtil.parseDate(crawlWeiXinArticleBO.getPublishTime()));
//                }

                // 存储es  dev
//                List<EsWeixinArticle> esWeixinArticleList = new ArrayList<>();
//                EsWeixinArticle esWeixinArticle = new EsWeixinArticle();
//                esWeixinArticle.setUrl(crawlWeiXinArticleBO.getUrl());
//                esWeixinArticle.setFavorabletimes(crawlWeiXinArticleBO.getReplytimes());
//                esWeixinArticle.setViewtimes(crawlWeiXinArticleBO.getViewtimes());
//                esWeixinArticle.setUniqueValue(MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
//                esWeixinArticle.setCrawltime(new Date());
//                esWeixinArticle.setAuthor(crawlWeiXinArticleBO.getAuthor());
//                esWeixinArticle.setContent(crawlWeiXinArticleBO.getContent());
//                esWeixinArticle.setTitle(crawlWeiXinArticleBO.getTitle());
//                esWeixinArticle.setProjectID(Integer.parseInt(projectId));
//                esWeixinArticle.setDetailId(detailId);
//                String commentStr = JSONArray.fromObject(crawlWeiXinArticleBO.getCommentlist()).toString();
//                String commentBase64 = Base64Util.getBase64(commentStr);
//                System.out.println("XXXX: remote_mcss : commentStr " + commentStr);
//                System.out.println("XXXX: remote_mcss : commentBase64 " + commentBase64);
//                esWeixinArticle.setCommentlist(commentBase64);

                //
                esWeixinArticleList.add(esWeixinArticle);
                String esWeixinArticleJsonParam = net.sf.json.JSONArray.fromObject(esWeixinArticleList).toString();
                String corpusServer = WebUtil.getEsServerByEnv();
                String getCorpusDataUrl = "/addDataInSearcher.json";
                Map<String, String> corpusNameMap = new HashMap<String, String>();
                corpusNameMap.put("dataType", storageTypeTable);
                corpusNameMap.put("dataJSON", esWeixinArticleJsonParam);
                CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), corpusServer + getCorpusDataUrl, "post", corpusNameMap);

                //调用dpm接口
                String dpmServer = WebUtil.getDpmServerByEnv();
                String getAcceptCallbackUrl = "/acceptCallback.json";

                Map<String, String> dpmMap = new HashMap<String, String>();
                dpmMap.put("detailId", task.getDetailId());
                dpmMap.put("projectId", task.getProjectId());
                dpmMap.put("flowId", task.getFlowId());
                dpmMap.put("progress", "0");
                dpmMap.put("status", "2");
                dpmMap.put("errorMessage", "");
                dpmMap.put("num", "1");
                dpmMap.put("dataJsonArray", esWeixinArticleJsonParam);
                CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + getAcceptCallbackUrl, "post", dpmMap);
            }
        } else {
            System.out.println("XXXX: remote_mcss : preserveWeiXinData 查询存贮表为空!");
            throw new WebException(MySystemCode.QUERY_TABLE_NULL);
        }
        return new preservePO();
    }

    private String getDataTableName(String datasourceTypeId) {
        String storageTypeTable = null;
        String baseServer = WebUtil.getBaseDataServerByEnv();
        String getDataUrl = "/common/getDataSourceTypeAndTableName.json" + "?datasourceTypeId=" + datasourceTypeId;
        Map<String, String> dataTableNameMap = new HashMap<String, String>();
        Object firstObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataTableNameMap);
        if (null != firstObject) {
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) firstObject;
            storageTypeTable = jsonObject.getString("storageTypeTable");
        }
        return storageTypeTable;
    }

    @RequestMapping(value = "/test.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "测试类", position = 0)
    public DevicesPO getTest(
            HttpServletRequest req, HttpServletResponse res) {
        DevicesPO devicesPO = new DevicesPO();
        String message="{\"dataTypeId\":\"16\",\"dealClass\":\"LaunchApp\",\"deviceId\":\"1\",\"id\":0}";
        ScriptTask task = JSON.parseObject(message, ScriptTask.class);
        String dealClass = task.getDealClass();
        try {
            String path="com.transing.mcss4dpm.job";
            Class c = Class.forName(path + ".DealClass." + dealClass);
            Method m = c.getMethod("execue", ScriptTask.class); //Sigleton有一个方法为print
            m.invoke(c.newInstance(), task); //调用print方法
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return devicesPO;
    }
}
