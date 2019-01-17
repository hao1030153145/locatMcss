package com.transing.mcss4dpm.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jeeframework.jeetask.startup.JeeTaskClient;
import com.jeeframework.util.encrypt.MD5Util;
import com.transing.mcss4dpm.JobEvent.Bo.McssTask;
import com.transing.mcss4dpm.JobEvent.Bo.ScriptTask;
import com.transing.mcss4dpm.biz.service.TaskService;
import com.transing.mcss4dpm.integration.bo.*;
import com.transing.mcss4dpm.util.*;
import com.transing.mcss4dpm.web.form.SubJsonParam;
import com.transing.mcss4dpm.web.form.TaskJsonParamRequest;
import com.transing.mcss4dpm.web.po.*;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Controller("taskController")
@Api(value = "任务管理", description = "任务管理相关的访问接口", position = 2)
public class TaskController {
    @Resource
    private TaskService taskService;
    @Resource
    private JeeTaskClient jeeTaskClient;

    @RequestMapping(value = "/crawlTask/executeCrawl.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "启动任务", position = 0)
    public CrawlStartPO startExecuteCrawl(
            @RequestParam(value = "projectId", required = false) @ApiParam String projectId,
            @RequestParam(value = "firstDetailId", required = false) @ApiParam String firstDetailId,
            @RequestParam(value = "flowId", required = false) @ApiParam String flowId,
            @RequestParam(value = "workFlowId", required = false) @ApiParam String workFlowId,
            @RequestParam(value = "flowDetailId", required = false) @ApiParam String flowDetailId,
            @RequestParam(value = "typeNo", required = false) @ApiParam String typeNo,
            @RequestParam(value = "jsonParam", required = false) @ApiParam String jsonParam,
            @RequestParam(value = "paramType", required = false) @ApiParam String paramType,
            @RequestParam(value = "batchNo", required = false) @ApiParam String batchNo,
            HttpServletRequest req, HttpServletResponse res) {
//        dpm调用接口
//                解析参数
//        根据参数id查询task表是否存在
//        根据数据源，数据源类型查询processor表获取处理类
//                把sub_task添加到zk中
//        task表添加 / 更新数据
//        已有task，查询task_sub表获取，获取待处理sub_task
//        没有task,解析子任务条数，往task_sub里添加数据
        System.out.println("XXXX: remote_mcss : executeCrawl ");
        System.out.println("XXXX: remote_mcss : projectId " + projectId);
        System.out.println("XXXX: remote_mcss : firstDetailId " + firstDetailId);
        System.out.println("XXXX: remote_mcss : flowId " + flowId);
        System.out.println("XXXX: remote_mcss : flowDetailId " + flowDetailId);
        System.out.println("XXXX: remote_mcss : typeNo " + typeNo);
        System.out.println("XXXX: remote_mcss : jsonParam " + jsonParam);
        System.out.println("XXXX: remote_mcss : paramType " + paramType);
        System.out.println("XXXX: remote_mcss : workFlowId " + workFlowId);
        System.out.println("XXXX: remote_mcss : batchNo " + batchNo);
        boolean taskIsCreate = false;
        Long taskId = null;
        int taskNu = 0;
        TaskJsonParamRequest taskJsonParam = JSON.parseObject(jsonParam, TaskJsonParamRequest.class);
        List<Task> taskList = taskService.getTaskById(flowDetailId);
        if (taskList.size() > 0) {
            taskIsCreate = true;
            taskId = taskList.get(0).getTaskId();
            taskNu = Integer.parseInt(taskList.get(0).getTaskNu());
        }
        //不存在任务
        //添加任务记录
        SubJsonParam subJsonParam = taskJsonParam.getJsonParam();
        //获取输入参数列表
        if (taskId == null) {
            Task task = new Task();
            task.setpId(Long.parseLong(flowDetailId));
            task.setFirstDetailId(firstDetailId);
            task.setTaskName(subJsonParam.getTaskName());
            task.setDataSource(subJsonParam.getDatasourceName());
            task.setDataType(subJsonParam.getDatasourceTypeName());
            task.setStatus(subJsonParam.getStatus() + "");
            task.setCompleteNu(0 + "");
            task.setCompleteTime("");
            task.setInputPara(subJsonParam.getInputParamArray().toString());
            task.setTaskNu("0");
            task.setDetailId(flowDetailId);
            task.setFlowId(flowId);
            task.setProjectId(projectId);
            task.setBatchNo(batchNo);
            taskService.addTask(task);
            taskId = task.getTaskId();
        }
        JSONArray inputArray = JSON.parseArray(subJsonParam.getInputParamArray().toString());
        List<List<SubTaskParam>> subTaskParamList = new ArrayList<>();

        try {
            CrawlInputTypeUtil crawlInputTypeUtil = new CrawlInputTypeUtil();
            crawlInputTypeUtil.InputParamSplit(inputArray, subTaskParamList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //对subTaskParamList进行笛卡尔
        List<List<SubTaskParam>> resuletList = new ArrayList<List<SubTaskParam>>();
        DescartesUtil.recursive(subTaskParamList, resuletList, 0, new ArrayList<>());
        for (List<SubTaskParam> subTaskParams : resuletList) {
            SubTask subTask = new SubTask();
            String inputparamer = net.sf.json.JSONArray.fromObject(subTaskParams).toString();
            subTask.setInputPara(inputparamer);
            subTask.setDataSource(subJsonParam.getDatasourceId());
            subTask.setDataType(subJsonParam.getDatasourceTypeId());
            subTask.setDealClass("ScriptRunning");
            subTask.setTaskId(taskId + "");
            subTask.setStatus("0");
            taskService.addSubTask(subTask);
            setZookeeper(subJsonParam.getTaskName(), workFlowId, inputparamer, subJsonParam.getDatasourceTypeId(), "ScriptRunning", subTask.getId() + "", taskId + "");
        }
        taskService.updateTaskNu(taskId + "", (taskNu + resuletList.size()) + "");
        //通知dpm子任务个数
        String dpmServer = WebUtil.getDpmServerByEnv();
        String getAcceptCallbackUrl = "/updateTotalTaskNum.json";
        Map<String, String> dpmMap = new HashMap<String, String>();
        dpmMap.put("detailId", flowDetailId);
        dpmMap.put("projectId", projectId);
        dpmMap.put("totalNum", (taskNu + resuletList.size()) + "");
        dpmMap.put("workFlowId", workFlowId);
        CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + getAcceptCallbackUrl, "post", dpmMap);

        return new CrawlStartPO();
    }

    @RequestMapping(value = "/crawlTask/stopCrawl.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "停止任务", position = 0)
    public CrawlStopPO stopExecuteCrawl(
            @RequestParam(value = "taskId", required = true) @ApiParam String taskId,
            HttpServletRequest req, HttpServletResponse res) {
        //调用zk停止接口
        List<TTask> tTaskList = taskService.getTTaskByTaskId(taskId);
        if (tTaskList != null && tTaskList.size() > 0) {
            for (TTask tTask : tTaskList) {
                jeeTaskClient.stopTask(tTask.getId());
            }
        }
        return new CrawlStopPO();
    }

    @RequestMapping(value = "/crawlTask/updataCopleteNu.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "更新任务完成数量", position = 0)
    public UpdateTaskPO stopExecuteCrawl(
            @RequestParam(value = "subTaskId", required = true) @ApiParam String subTaskId,
            @RequestParam(value = "status", required = true) @ApiParam String status,
            HttpServletRequest req, HttpServletResponse res) {
        //根据子任务id，更新子任务状态
        taskService.updateSubTaskStatus(subTaskId, status);
        //根据子任务id，更新任务完成数量，如果任务完成数量等于子任务数量，更新任务状态
        SubTask subTask = taskService.getSubTaskBySubId(subTaskId);
        List<Task> taskList = taskService.getTaskByTaskId(subTask.getTaskId());
        if (taskList.size() > 0) {
            Task task = taskList.get(0);
            int completeNu = Integer.parseInt(task.getCompleteNu());
            int taskNu = Integer.parseInt(task.getTaskNu());
            if (taskNu == completeNu + 1) {
                //任务完成
                taskService.updateTaskById(task.getTaskId() + "", "1", completeNu + 1);
            } else {
                taskService.updateTaskById(task.getTaskId() + "", "0", completeNu + 1);
            }
        }
        return new UpdateTaskPO();
    }

    @RequestMapping(value = "/crawlTask/taskStatus.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询任务状态", position = 0)
    public ModelAndView getTaskStatus(
            @RequestParam(value = "subTaskId", required = true) @ApiParam String subTaskId,
            HttpServletRequest req, HttpServletResponse res) {
        SubTask subTask = taskService.getSubTaskBySubId(subTaskId);
        List<Task> taskList = taskService.getTaskByTaskId(subTask.getTaskId());
        if (taskList.size() > 0) {
            taskList.get(0).getStatus();
        }
        return new ModelAndView("user/userList");
    }

    @RequestMapping(value = "/crawlTask/preserveData.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存抓取数据", position = 0)
    public preservePO preserveData(
            @RequestParam(value = "taskId", required = true) @ApiParam String subTaskId,
            @RequestParam(value = "jsonParam", required = true) @ApiParam String jsonParam,
            @RequestParam(value = "workFlowId", required = false) @ApiParam String workFlowId,
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            @RequestParam(value = "formerUrl", required = false) @ApiParam String formerUrl,
            HttpServletRequest req, HttpServletResponse res) {
        System.out.println("XXXX: remote_mcss : preserveData ");
        System.out.println("XXXX: remote_mcss : taskId " + subTaskId);
        System.out.println("XXXX: remote_mcss : jsonParam " + jsonParam);
        System.out.println("XXXX: remote_mcss : workFlowId " + workFlowId);
        System.out.println("XXXX: remote_mcss : datasourceTypeId " + datasourceTypeId);
        System.out.println("XXXX: remote_mcss : formerUrl " + formerUrl);
        //根据数据源类型查看存储表名
        String baseServer = WebUtil.getBaseDataServerByEnv();
        String getDataUrl = "/common/getDataSourceTypeAndTableName.json" + "?datasourceTypeId=" + datasourceTypeId;
        Map<String, String> dataTableNameMap = new HashMap<String, String>();
        Object firstObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataTableNameMap);
        if (null != firstObject) {
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) firstObject;
            String storageTypeTable = jsonObject.getString("storageTypeTable");
            if (!storageTypeTable.equalsIgnoreCase("")) {
                System.out.println("XXXX: remote_mcss : storageTypeTable " + storageTypeTable);
                SubTask subTask = taskService.getSubTaskBySubId(subTaskId);
                List<Task> taskList = taskService.getTaskByTaskId(subTask.getTaskId());
                if (taskList != null && taskList.size() > 0) {
                    Task task = taskList.get(0);
                    String projectId = task.getProjectId();
                    String firstDetailId = task.getFirstDetailId();
                    String detailId = task.getDetailId();
                    String batchNo = task.getBatchNo();
                    //Map<String, Object> map = JSON.parseObject(jsonParam);
                    //List<Map<String, Object>> map1 = JSON.parseObject(jsonParam,List.class);

                    com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(jsonParam);
                    List<Map<String, Object>> mapListJson = (List) jsonArray;

                    String resultNum = "";

                    if (mapListJson != null) {
                        int i = 1;
                        for (Map<String, Object> map : mapListJson) {
                            i++;
                            map.put("detailId", detailId);
                            map.put("projectID", Integer.parseInt(projectId));
                            map.put("uniqueValue", MD5Util.encrypt(firstDetailId + formerUrl + projectId + i));
                            map.put("datasourceTypeId", datasourceTypeId);
                            map.put("url", MD5Util.encrypt(firstDetailId + formerUrl + projectId));
                            map.put("batchNo", batchNo);
                        }
                        String dataJSON = net.sf.json.JSONArray.fromObject(mapListJson).toString();
                        //es存储
                        String corpusServer = WebUtil.getEsServerByEnv();
                        String getCorpusDataUrl = "/addDataInSearcher.json";
                        Map<String, String> corpusNameMap = new HashMap<String, String>();
                        corpusNameMap.put("dataType", storageTypeTable);
                        corpusNameMap.put("dataJSON", dataJSON);
                        System.out.println("XXXX: remote_mcss : dataType " + storageTypeTable);
                        System.out.println("XXXX: remote_mcss : dataJSON " + dataJSON);

                        CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), corpusServer + getCorpusDataUrl, "post", corpusNameMap);
                        // 如果抓取结果不为空，就赋值
                        resultNum = String.valueOf(mapListJson.size());

                    } else {
                        resultNum = "0";
                    }

                    // 如果jsonParam为空的话，那就直接调用dpm回调接口，如果不为空，那就等待35s再调用此回调接口
                    if (!jsonParam.isEmpty() && !("").equals(jsonParam) && !("[]").equals(jsonParam)) {
                        try {
                            Thread.sleep(35 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

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
                    dpmMap.put("dataJsonArray", jsonParam);
                    dpmMap.put("workFlowId", workFlowId);
                    CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + getAcceptCallbackUrl, "post", dpmMap);

                    //dpm
                    String updateResultNumAndFinishNum = "/updateResultNumAndFinishNum.json";
                    Map<String, String> dpmTaskNimMap = new HashMap<String, String>();
                    dpmTaskNimMap.put("detailId", task.getDetailId());
                    dpmTaskNimMap.put("finishNum", "1");
                    dpmTaskNimMap.put("resultNum", resultNum);
                    dpmTaskNimMap.put("projectId", task.getProjectId());
                    dpmTaskNimMap.put("workFlowId", workFlowId);

                    System.out.println("xxxxxxxx detailId = " + task.getDetailId());
                    System.out.println("xxxxxxxx finishNum = " + "1");
                    System.out.println("xxxxxxxx resultNum = " + resultNum);
                    System.out.println("xxxxxxxx projectId = " + task.getProjectId());
                    System.out.println("xxxxxxxx workFlowId = " + workFlowId);
                    CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + updateResultNumAndFinishNum, "post", dpmTaskNimMap);
                }
            }
        }
        return new preservePO();
    }

    @RequestMapping(value = "/crawlTask/preserveWeiXinArticleData.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存抓取微信文章文章数据", position = 0)
    public preservePO preserveWeiXInArticleData(
            @RequestParam(value = "taskId", required = true) @ApiParam String subTaskId,
            @RequestParam(value = "workFlowId", required = false) @ApiParam String workFlowId,
            @RequestParam(value = "jsonParam", required = true) @ApiParam String jsonParam,
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        System.out.println("XXXX: remote_mcss : preserveWeiXinData");
        CrawlWeiXinArticleBO crawlWeiXinArticleBO = JSON.parseObject(jsonParam, CrawlWeiXinArticleBO.class);
        //根据数据源类型查看存储表名
        String baseServer = WebUtil.getBaseDataServerByEnv();
        String getDataUrl = "/common/getDataSourceTypeAndTableName.json" + "?datasourceTypeId=" + datasourceTypeId;

        Map<String, String> dataTableNameMap = new HashMap<String, String>();
        Object firstObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataTableNameMap);
        if (null != firstObject) {
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) firstObject;
            String storageTypeTable = jsonObject.getString("storageTypeTable");

            if (!storageTypeTable.equalsIgnoreCase("")) {
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
                    // 存储es  idc

//                    List<EsWeixinArticle> esWeixinArticleList = new ArrayList<>();
//                    EsWeixinArticle esWeixinArticle = new EsWeixinArticle();
//                    esWeixinArticle.setUrl(crawlWeiXinArticleBO.getUrl());
//                    esWeixinArticle.setLtimes(Integer.parseInt(crawlWeiXinArticleBO.getReplytimes()));
//                    esWeixinArticle.setVtimes(Integer.parseInt(crawlWeiXinArticleBO.getViewtimes()));
//                    esWeixinArticle.setUniqueValue(MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
//                    esWeixinArticle.setCrawltime(new Date());
//                    esWeixinArticle.setAuthor(crawlWeiXinArticleBO.getAuthor());
//                    esWeixinArticle.setSource(crawlWeiXinArticleBO.getSource());
//                    esWeixinArticle.setUid(crawlWeiXinArticleBO.getWeixinId());
//                    esWeixinArticle.setTempurl(crawlWeiXinArticleBO.getFormerUrl());
//                    esWeixinArticle.setContent(crawlWeiXinArticleBO.getContent());
//                    esWeixinArticle.setTitle(crawlWeiXinArticleBO.getTitle());
//                    esWeixinArticle.setProjectID(Integer.parseInt(projectId));
//                    esWeixinArticle.setImage(crawlWeiXinArticleBO.getImgUrl());
//                    esWeixinArticle.setDetailId(detailId);
//                    System.out.println("xxxx: " + "remote_imgUrl: " + crawlWeiXinArticleBO.getImgUrl());
//                    int lTimes = crawlWeiXinArticleBO.getReplytimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getReplytimes());
//                    int vTimes = crawlWeiXinArticleBO.getViewtimes() == null ? 0 : Integer.parseInt(crawlWeiXinArticleBO.getViewtimes());
//                    esWeixinArticle.setHtimes((vTimes + lTimes * 500) + "");
//                    if (crawlWeiXinArticleBO.getPublishTime() != null) {
//                        esWeixinArticle.setDatetime(DateUtil.parseDate(crawlWeiXinArticleBO.getPublishTime()));
//                    }

                    // 存储es  dev
//                    List<EsWeixinArticle> esWeixinArticleList = new ArrayList<>();
//                    EsWeixinArticle esWeixinArticle = new EsWeixinArticle();
//                    esWeixinArticle.setUrl(crawlWeiXinArticleBO.getUrl());
//                    esWeixinArticle.setFavorabletimes(crawlWeiXinArticleBO.getReplytimes());
//                    esWeixinArticle.setViewtimes(crawlWeiXinArticleBO.getViewtimes());
//                    esWeixinArticle.setUniqueValue(MD5Util.encrypt(firstDetailId + crawlWeiXinArticleBO.getFormerUrl() + projectId));
//                    esWeixinArticle.setCrawltime(new Date());
//                    esWeixinArticle.setAuthor(crawlWeiXinArticleBO.getAuthor());
//                    esWeixinArticle.setContent(crawlWeiXinArticleBO.getContent());
//                    esWeixinArticle.setTitle(crawlWeiXinArticleBO.getTitle());
//                    esWeixinArticle.setProjectID(Integer.parseInt(projectId));
//                    esWeixinArticle.setDetailId(detailId);
//                    String commentStr= net.sf.json.JSONArray.fromObject(crawlWeiXinArticleBO.getCommentlist()).toString();
//                    System.out.println("XXXX: remote_mcss : commentStr "+commentStr);
//                    esWeixinArticle.setCommentlist(commentStr);

                    esWeixinArticleList.add(esWeixinArticle);
                    String esWeixinArticleJsonParam = net.sf.json.JSONArray.fromObject(esWeixinArticleList).toString();
                    System.out.println("XXXX: remote_mcss : dataJSON");
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
                    dpmMap.put("workFlowId", workFlowId);
                    CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), dpmServer + getAcceptCallbackUrl, "post", dpmMap);
                }
            }
        }
        return new preservePO();
    }

    @RequestMapping(value = "/getDevicesList.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取设备列表", position = 0)
    public DevicesPO getDevicesList(@RequestParam(value = "registServer", required = true) @ApiParam(value = "registServer", required = true) String registServer,
                                    @RequestParam(value = "funcation", required = false) @ApiParam(value = "funcation", required = false) String funcation,
                                    HttpServletRequest req, HttpServletResponse res) {
        DevicesPO devicesPO = new DevicesPO();


        Map<String, String> param = new HashMap<>();
        param.put("registServer", registServer);
        if (funcation != null && !("").equals(funcation)) {
            param.put("funcation", funcation);
        }

        List<DevicesInf> devicesInfList = taskService.getDevicesList(param);
        if (devicesInfList.size() > 0) {
            devicesPO.getData().addAll(devicesInfList);
        }
        return devicesPO;
    }

    @RequestMapping(value = "/testThread.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取设备列表", position = 0)
    public void testThread(@RequestParam(value = "registServer", required = true) @ApiParam(value = "registServer", required = true) String registServer,
                           HttpServletRequest req, HttpServletResponse res) {


        Map<String, String> param = new HashMap<>();

        while (true) {
            Future<List<DevicesInf>> future = Executors.newSingleThreadExecutor().submit(() -> {
                param.put("registServer", registServer);
                List<DevicesInf> devicesInfList = taskService.getDevicesList(param);
                System.out.println("11");
                return devicesInfList;
            });
            try {
                List<DevicesInf> devicesInfs = future.get();
                System.out.println(JSON.toJSONString(devicesInfs));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    @RequestMapping(value = "/test.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取设备列表", position = 0)
    public DevicesPO getTest(
            HttpServletRequest req, HttpServletResponse res) {
        DevicesPO devicesPO = new DevicesPO();
        ScriptTask task = new ScriptTask();
        task.setDataTypeId("36");
        String dealClass = "LaunchApp";
        try {
            String path = "com.transing.mcss4dpm.job";
            Class c = Class.forName(path + ".DealClass." + dealClass);
            Method m = c.getMethod("execue", McssTask.class); //Sigleton有一个方法为print
            m.invoke(c.newInstance(), task); //调用print方法
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return devicesPO;
    }

    @RequestMapping(value = "/test2.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取设备列表", position = 0)
    public DevicesPO getTest2(
            @RequestParam(value = "taskId", required = true) @ApiParam MultipartFile file,
            @RequestParam(value = "taskId", required = true) @ApiParam byte[] file2,
            HttpServletRequest req, HttpServletResponse res) {
        DevicesPO devicesPO = new DevicesPO();
        McssTask task = new McssTask();
        task.setDataTypeId("39");
        String dealClass = "ScriptRunning";
        try {
            String path = "com.transing.mcss4dpm.job";
            Class c = Class.forName(path + ".DealClass." + dealClass);
            Method m = c.getMethod("execue", McssTask.class); //Sigleton有一个方法为print
            m.invoke(c.newInstance(), task); //调用print方法
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return devicesPO;
    }

    //添加到zk中
    private void setZookeeper(String taskName, String workFlowId, String taskParam, String dataTypeId, String dealClass, String subTaskId, String taskId) {
        McssTask task = new McssTask();
        task.setParam(taskParam);
        task.setDealClass(dealClass);
        task.setJobClass("com.transing.mcss4dpm.job.TaskJob");
        task.setDataTypeId(dataTypeId);
        task.setName(taskName);
        task.setTaskId(taskId);
        task.setSubTaskId(subTaskId);
        task.setWorkFlowId(workFlowId);
        jeeTaskClient.submitTask(task);
    }


}