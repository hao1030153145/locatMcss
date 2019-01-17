package com.transing.mcss4dpm.job.DealClass;

import com.alibaba.fastjson.JSON;
import com.transing.mcss4dpm.JobEvent.Bo.McssTask;
import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.WeixinCommentTask;
import com.transing.mcss4dpm.integration.bo.SubTaskParam;
import com.transing.mcss4dpm.util.Base64Util;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import net.sf.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${微信评论处理类}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/25
 */
public class WeiXinComment {
    private static WeiXinComment inst = null;

    public static WeiXinComment getInstance() {
        if (inst == null) {
            inst = new WeiXinComment();
        }
        return inst;
    }

    public void execue(McssTask task) {
        String dealClass = task.getDealClass();
        String datasourceTypeId = task.getDataTypeId();
        String subTaskid = task.getSubTaskId(); //子任务id
        String param = task.getParam();
        System.out.println("XXXX: location_mcss_weixin_comment " + param);
        //解析微信文章评论输入参数
        List<SubTaskParam> subTaskParams1 = JSON.parseArray(param, SubTaskParam.class);
        WeixinCommentTask weixinCommentTask = new WeixinCommentTask();
        for (SubTaskParam subTaskParam : subTaskParams1) {
            System.out.println("XXXX: location_mcss_weixin_En_name " + subTaskParam.getParamEnName());
            if (subTaskParam.getParamEnName().equalsIgnoreCase("url")) {
                System.out.println("XXXX: location_mcss_weixin_Cn_name " + subTaskParam.getSubParam());
                weixinCommentTask.setUrl(subTaskParam.getSubParam());
            }
            if (subTaskParam.getParamEnName().equalsIgnoreCase("commentList")) {
                String subParam = Base64Util.getFromBase64(subTaskParam.getSubParam());
                weixinCommentTask.setComment_list(subParam);
            }
        }

        String url = weixinCommentTask.getUrl();
        com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(weixinCommentTask.getComment_list());
        List<Map<String, Object>> mapListJson = (List) jsonArray;
        for (Map<String, Object> map : mapListJson) {
            map.put("url", url);
        }

        String jsonParam = JSONArray.fromObject(mapListJson).toString();
        System.out.println("XXXX: location_mcss_jsonParam " + jsonParam);
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
        System.out.println("XXXX: location_mcss : doJob taskId: " + subTaskid);
        System.out.println("XXXX: location_mcss : doJob datasourceTypeId: " + datasourceTypeId);
        System.out.println("XXXX: location_mcss : doJob crawlWeiXinArticleBO: " + jsonParam);
        CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), mcssServer + getDataUrl, "post", dataTypePassMap);
    }
}
