package com.transing.mcss4dpm.job.DealClass;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.transing.mcss4dpm.JobEvent.Bo.McssTask;
import com.transing.mcss4dpm.biz.service.impl.api.DriverManager;
import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.WeixinArticleTask;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AndroidDriverStatus;
import com.transing.mcss4dpm.biz.service.impl.api.script.CrawlWeiXinArticleScript;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXinArticleBO;
import com.transing.mcss4dpm.integration.bo.SubTaskParam;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${微信文章处理类}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/7
 */
public class WeiXinArticle {
    private static WeiXinArticle inst = null;

    public static WeiXinArticle getInstance() {
        if (inst == null) {
            inst = new WeiXinArticle();
        }
        return inst;
    }

    private DriverManager driverManager;
    private AndroidDriverStatus androidDriverStatus;

    public void execue(McssTask task) {
        String dealClass = task.getDealClass();
        String datasourceTypeId = task.getDataTypeId();
        String subTaskid = task.getSubTaskId(); //子任务id
        String param = task.getParam();
        //微信文章抓取
        List<SubTaskParam> subTaskParams1 = JSON.parseArray(param, SubTaskParam.class);
        WeixinArticleTask weixinArticleTask = new WeixinArticleTask();
        for (SubTaskParam subTaskParam : subTaskParams1) {
            if (subTaskParam.getParamEnName().equalsIgnoreCase("URL")) {
                weixinArticleTask.setUrl(subTaskParam.getSubParam());
                break;
            }
        }
        driverManager = DriverManager.getInstance();
        androidDriverStatus = driverManager.getEnableDriver();
        if (androidDriverStatus == null) {
            System.out.println("XXXX: location_mcss : 没有可用设备");
            return;
        }
        CrawlWeiXinArticleScript crawlWeiXinArticleScript = new CrawlWeiXinArticleScript(androidDriverStatus);
        CrawlWeiXinArticleBO crawlWeiXinArticleBO = null;
        try {
            crawlWeiXinArticleBO = crawlWeiXinArticleScript.operateProcess(weixinArticleTask);
            //释放设备
            boolean isRelease = driverManager.releaseDriver(androidDriverStatus.getDeviceName());
            if (!isRelease) {
                System.out.println("XXXX: 回收设备失败");
            }
        } catch (Exception e) {
            //所有抓取中没有哦处理到的异常
            //删除该appium server,清楚残余adb ,chromeDriver
            System.out.println("XXXX: location_mcss : all error: " + e.toString());
            boolean isKill = driverManager.stopAppium(androidDriverStatus);
            if (isKill) {
                //处理DriverManager.driverList设备状态
                //重启该id,db的appium server
                driverManager.restartDriver(androidDriverStatus.getDeviceName());
            }

        }

        if (crawlWeiXinArticleBO != null) {
            //调用远程mcss存储接口
            String mcssServer = "";
            try {
                mcssServer = System.getProperty("mcss_url");
            } catch (Exception e) {
                System.out.println("XXXX: location_mcss error : get mcssServer base url ");
            }
            String getDataUrl = "/weixin/preserveWeiXinArticleData.json";

            String jsonParam = JSONObject.toJSONString(crawlWeiXinArticleBO);
            Map<String, String> dataTypePassMap = new HashMap<String, String>();
            dataTypePassMap.put("taskId", subTaskid);
            dataTypePassMap.put("datasourceTypeId", datasourceTypeId);
            dataTypePassMap.put("jsonParam", jsonParam);
            System.out.println("XXXX: location_mcss : doJob taskId: " + subTaskid);
            System.out.println("XXXX: location_mcss : doJob datasourceTypeId: " + datasourceTypeId);
            System.out.println("XXXX: location_mcss : doJob crawlWeiXinArticleBO: " + jsonParam);
            CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), mcssServer + getDataUrl, "post", dataTypePassMap);
            System.out.println("XXXX: location_mcss : doJob call_remote: true");
        } else {
            System.out.println("XXXX: location_mcss : 没抓到8");
        }

        finishWorker(subTaskid);
    }

    private boolean finishWorker(String subTaskId) {
        //完成操作,更新任务,子任务状态
        String baseServer = System.getProperty("mcss_url");
        String getDataUrl = "/crawlTask/updataCopleteNu.json" + "?subTaskId=" + subTaskId + "&status=1";
        System.out.println("XXXX: location_mcss : doJob mcssServer : " + baseServer + getDataUrl);
        Map<String, String> dataMap = new HashMap<String, String>();
        Object firstObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataMap);
        if (null == firstObject) {
            System.out.println("XXXX: location_mcss : false");
            return false;
        }
        System.out.println("XXXX: location_mcss : true");
        return true;
    }
}
