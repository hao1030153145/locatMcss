package com.transing.mcss4dpm.job.DealClass;

import com.alibaba.fastjson.JSON;
import com.transing.mcss4dpm.JobEvent.Bo.McssTask;
import com.transing.mcss4dpm.biz.service.impl.api.AppiumAction;
import com.transing.mcss4dpm.biz.service.impl.api.DriverManager2;
import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.WeiXinTencentTask;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AppiumDriverManager;
import com.transing.mcss4dpm.biz.service.impl.api.script.CrawlWeiXinTencentCommentScript;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXInTencentBO;
import com.transing.mcss4dpm.integration.bo.SubTaskParam;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import net.sf.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/4/11
 */
public class WeiXinTencentComment {
    private static WeiXinTencentComment inst = null;

    public static WeiXinTencentComment getInstance() {
        if (inst == null) {
            inst = new WeiXinTencentComment();
        }
        return inst;
    }

    public void execue(McssTask task) {
        System.out.println("WeiXinTencentComment 类启动>>>>>>>");
        String datasourceTypeId = task.getDataTypeId();
        String subTaskid = task.getSubTaskId(); //子任务id
        String param = task.getParam();
        List<SubTaskParam> subTaskParams1 = JSON.parseArray(param, SubTaskParam.class);
        WeiXinTencentTask weiXinTencentTask = new WeiXinTencentTask();
        for (SubTaskParam subTaskParam : subTaskParams1) {
            if (subTaskParam.getParamEnName().equalsIgnoreCase("URL")) {
                weiXinTencentTask.setUrl(subTaskParam.getSubParam());
                break;
            }
        }
        AppiumAction appiumAction = new AppiumAction();
        DriverManager2 driverManager = DriverManager2.getInstance();
        AppiumDriverManager appiumDriverManager = driverManager.getDriverByStatus("0");
        if (appiumDriverManager == null) {
            appiumAction.sleep(15 * 60 * 1000);
            System.out.println("XXXX: location_mcss : 没有可用设备");
            return;
        }
        try {
            CrawlWeiXinTencentCommentScript crawlWeiXinArticleScript = new CrawlWeiXinTencentCommentScript(appiumDriverManager,driverManager,datasourceTypeId);
            List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOList=crawlWeiXinArticleScript.operateProcess(weiXinTencentTask);
            String jsonParam = JSONArray.fromObject(crawlWeiXInTencentBOList).toString();
            //调用远程mcss存储接口
            String mcssServer = "";
            try {
                mcssServer = System.getProperty("mcss_url");
            } catch (Exception e) {
                System.out.println("XXXX: location_mcss error : get mcssServer base url ");
            }
            String getDataUrl = "/weixin/preserveWeiXinCommentData.json";
            Map<String, String> dataTypePassMap = new HashMap<String, String>();
            dataTypePassMap.put("taskId", subTaskid);
            dataTypePassMap.put("datasourceTypeId", datasourceTypeId);
            dataTypePassMap.put("jsonParam", jsonParam);
            System.out.println("XXXX: location_mcss >>>>>>>>> doJob taskId: " + subTaskid);
            System.out.println("XXXX: location_mcss >>>>>>>>> doJob datasourceTypeId: " + datasourceTypeId);
            System.out.println("XXXX: location_mcss >>>>>>>>> doJob crawlWeiXinArticleBO: " + jsonParam);
            CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), mcssServer + getDataUrl, "post", dataTypePassMap);
        } catch (InterruptedException | IOException e) {
            System.out.println("腾讯视频异常 >>>>>>>" + e);
            e.printStackTrace();
        }finally {
            //释放设备
            driverManager.releaseAppiumDriver(appiumDriverManager);
        }
    }
}
