package com.transing.mcss4dpm.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeeframework.jeetask.startup.JeeTaskClient;
import com.jeeframework.logicframework.biz.service.mq.producer.BaseKafkaProducer;
import com.jeeframework.logicframework.integration.sao.hdfs.BaseSaoHDFS;
import com.jeeframework.logicframework.integration.sao.zookeeper.BaseSaoZookeeper;
import com.jeeframework.webframework.exception.WebException;
import com.transing.mcss4dpm.JobEvent.Bo.ScriptTask;
import com.transing.mcss4dpm.biz.service.ScriptService;
import com.transing.mcss4dpm.biz.service.TaskService;
import com.transing.mcss4dpm.biz.service.impl.api.CrawlAction;
import com.transing.mcss4dpm.biz.service.impl.api.bo.ActionBo;
import com.transing.mcss4dpm.biz.service.impl.api.bo.ChromeOption;
import com.transing.mcss4dpm.biz.service.impl.api.bo.KeystoreOption;
import com.transing.mcss4dpm.integration.bo.*;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import com.transing.mcss4dpm.util.DateUtil;
import com.transing.mcss4dpm.util.WebUtil;
import com.transing.mcss4dpm.web.exception.MySystemCode;
import com.transing.mcss4dpm.web.po.*;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * ${脚本规则,脚本录制}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
@Controller("scriptController")
@Api(value = "脚本规则,脚本录制相关", description = "脚本规则,脚本录制相关访问接口", position = 2)
@RequestMapping(path = "/scriptRegulation")
public class ScriptController {
    @Resource
    private TaskService taskService;
    @Resource
    private ScriptService scriptService;
    @Resource
    private BaseSaoHDFS baseSaoHDFS;
    @Resource
    private BaseKafkaProducer baseKafkaProducer;
    @Autowired
    private BaseSaoZookeeper baseSaoZookeeper;
    @Resource
    private JeeTaskClient jeeTaskClient;

    @RequestMapping(value = "/getApplicationInfo.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据应用id获取启动信息", position = 0)
    public ApplicationPO preserveWeiXinCommentDat(
            @RequestParam(value = "datatypeId", required = true) @ApiParam String datatypeid,
            HttpServletRequest req, HttpServletResponse res) {
        ApplicationPO applicationPO = new ApplicationPO();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(datatypeid);
        System.out.println("根据应用id获取启动信息===============>>>>>" + datatypeid);
        if (scriptInfoBO != null) {
            int appId = scriptInfoBO.getAppId();
            ApplicationBO applicationBO = scriptService.getApplicationById(appId + "");
            if (applicationBO != null) {
                KeystoreOption keystoreOption = new KeystoreOption();
                ChromeOption chromeOption = new ChromeOption();

                if (applicationBO.getKeystorePath() != null && !applicationBO.getKeystorePath().isEmpty()) {
                    keystoreOption.setKeyAlias(applicationBO.getKeyAlias());
                    keystoreOption.setKeyPassword(applicationBO.getKeyPassword());
                    keystoreOption.setKeystorePassword(applicationBO.getKeystorePassword());
                    keystoreOption.setKeystorePath(applicationBO.getKeystorePath());
                    applicationPO.setKeystoreOption(keystoreOption);
                }

                if (applicationBO.getAndroidPackage() != null && !applicationBO.getAndroidPackage().isEmpty()) {
                    chromeOption.setAndroidActivity(applicationBO.getAndroidActivity());
                    chromeOption.setAndroidPackage(applicationBO.getAndroidPackage());
                    chromeOption.setAndroidProcess(applicationBO.getAndroidProcess());
                    chromeOption.setAndroidUseRunningApp(applicationBO.getAndroidUserunningApp() == 1);
                    applicationPO.setChromeOption(chromeOption);
                }

                applicationPO.setId(applicationBO.getId());
                applicationPO.setName(applicationBO.getName());
                applicationPO.setApp(applicationBO.getApp());
                applicationPO.setAppPackage(applicationBO.getAppPackage());
                applicationPO.setAppActivity(applicationBO.getAppActivity());
                applicationPO.setPlatformName(applicationBO.getPlatformName());
                applicationPO.setAutomationName(applicationBO.getAutomationName());
                applicationPO.setFullReset(applicationBO.getFullReset());
                applicationPO.setNoReset(applicationBO.getNoReset());
                applicationPO.setUnicodeKeyBoard(applicationBO.getUnicodeKeyBoard());
                applicationPO.setResetKeyboard(applicationBO.getResetKeyboard());
                applicationPO.setNewCommandTimeout(applicationBO.getNewCommandTimeout() == 0 ? 300 : applicationBO.getNewCommandTimeout());
                applicationPO.setAutoLaunch(applicationBO.getAutoLaunch());
            } else {
                throw new WebException(MySystemCode.GET_APPLICATION_INFO_NULL);
            }
        }

        return applicationPO;
    }

    @RequestMapping(value = "/getScriptBytypeId.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据数据源类型id获取脚本信息", position = 0)
    public List<ScriptDetailBO> getScriptBytypeId(
            @RequestParam(value = "datatypeId", required = true) @ApiParam String datatypeid,
            HttpServletRequest req, HttpServletResponse res) {
        List<ScriptDetailBO> scriptDetailBOList = new ArrayList<>();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(datatypeid);
        System.out.println("根据数据源类型id获取脚本信息===============>>>>>" + datatypeid);
        if (scriptInfoBO != null) {
            int scriptId = scriptInfoBO.getId();
            scriptDetailBOList = scriptService.getScriptDetailByScriptid(scriptId + "");
        } else {
            throw new WebException(MySystemCode.GET_APPLICATION_INFO_NULL);
        }
        return scriptDetailBOList;
    }

    @RequestMapping(value = "/getCrawlRegulationByTypeid.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据应用id获取抓取规则", position = 0)
    public CrawlRegulationItemBO getCrawlRegulationByTypeid(
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        CrawlRegulationItemBO crawlRegulationItemBO = new CrawlRegulationItemBO();
        List<CrawlRegulationBO> crawlRegulationBOs = new ArrayList<>();
        CrawlRegulationListBO crawlRegulationListBO = new CrawlRegulationListBO();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(datasourceTypeId);
        System.out.println("根据应用id获取抓取规则===============>>>>>" + datasourceTypeId);
        if (scriptInfoBO != null) {
            int scriptId = scriptInfoBO.getId();
            crawlRegulationBOs = scriptService.getCrawlRegulationByTypeid(scriptId + "");
            crawlRegulationListBO = scriptService.getCrawlRegulationListByTypeid(scriptId + "");
        }
        crawlRegulationItemBO.setCrawlRegulationBOList(crawlRegulationBOs);
        crawlRegulationItemBO.setCrawlRegulationListBO(crawlRegulationListBO);
        return crawlRegulationItemBO;
    }

    @RequestMapping(value = "/runningAction.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "执行操作", position = 0)
    public RunningActionPO runningAction(@RequestParam(value = "scriptId", required = true) @ApiParam(value = "scriptId", required = true) String scriptId,
                                         @RequestParam(value = "step", required = true) @ApiParam(value = "step", required = true) String step,
                                         @RequestParam(value = "elementtype", required = false) @ApiParam(value = "elementtype", required = false) String elementtype,
                                         @RequestParam(value = "elementvalue", required = false) @ApiParam(value = "elementvalue", required = false) String elementvalue,
                                         @RequestParam(value = "actiontype", required = false) @ApiParam(value = "actiontype", required = false) String actiontype,
                                         @RequestParam(value = "actionvalue", required = false) @ApiParam(value = "actionvalue", required = false) String actionvalue,
                                         @RequestParam(value = "blocktype", required = false) @ApiParam(value = "blocktype", required = false) String blocktype,
                                         @RequestParam(value = "blockvalue", required = false) @ApiParam(value = "blockvalue", required = false) String blockvalue,
                                         @RequestParam(value = "block", required = false) @ApiParam(value = "block", required = false) String block,
                                         @RequestParam(value = "deviceId", required = false) @ApiParam(value = "deviceId", required = false) String deviceId,
                                         @RequestParam(value = "serverName", required = true) @ApiParam(value = "serverName", required = true) String serverName,
                                         HttpServletRequest req, HttpServletResponse res) {
        RunningActionPO runningActionPO = new RunningActionPO();
        String dealClass = "LaunchAction";
        //根据scriptId 得到 dataTypeId
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoById(scriptId);
        if (scriptInfoBO == null) {
            throw new WebException(MySystemCode.GET_SCRIPT_INFO_NULL);
        }
        int dataTypeId = scriptInfoBO.getDatatypeId();

        //封装参数
        ActionBo actionBo = new ActionBo();
        actionBo.setDeviceId(deviceId + "");
        actionBo.setStep(step);
        actionBo.setActionType(actiontype);
        actionBo.setActionValue(actionvalue);
        actionBo.setElementType(elementtype);
        actionBo.setElementValue(elementvalue);
        actionBo.setBlock(block);
        actionBo.setBlockType(blocktype);
        actionBo.setBlockValue(blockvalue);
        String param = JSONObject.toJSONString(actionBo);

        //封装
        ScriptTask actionTask = new ScriptTask();
        actionTask.setDataTypeId(dataTypeId + "");
        actionTask.setDealClass(dealClass);
        actionTask.setParam(param);
        actionTask.setDeviceId(deviceId);
        String paramTask = JSON.toJSONString(actionTask);
        //调用kafaka send方法
        baseKafkaProducer.send(serverName, paramTask);
        //返回参数
        runningActionPO.setScriptId(Integer.parseInt(scriptId));
        return runningActionPO;
    }

    @RequestMapping(value = "/startApplication.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "启动应用", position = 0)
    public RunningActionPO startApplication(@RequestParam(value = "deviceId", required = false) @ApiParam(value = "deviceId", required = false) String deviceId,
                                            @RequestParam(value = "scriptId", required = true) @ApiParam(value = "scriptId", required = true) String scriptId,
                                            @RequestParam(value = "serverName", required = true) @ApiParam(value = "serverName", required = true) String serverName,
                                            HttpServletRequest req, HttpServletResponse res) {
        String dealClass = "LaunchApp";
        //根据scriptId 得到 dataTypeId
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoById(scriptId);
        int dataTypeId = scriptInfoBO.getDatatypeId();

        //封装
        ScriptTask launchTask = new ScriptTask();
        launchTask.setDataTypeId(dataTypeId + "");
        launchTask.setDealClass(dealClass);
        launchTask.setDeviceId(deviceId);
        launchTask.setServerName(serverName);
        String paramTask = JSON.toJSONString(launchTask);
        //调用kafaka send方法
        baseKafkaProducer.send(serverName, paramTask);
        //返回参数
        RunningActionPO runningActionPO = new RunningActionPO();
        runningActionPO.setScriptId(Integer.parseInt(scriptId));
        return runningActionPO;
    }

    @RequestMapping(value = "/uploadingScreenInfo.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存屏幕信息,源码", position = 0)
    public UploadSreenInPO uploadingScreenInfo(
            @RequestParam(value = "dataTypeId", required = true) @ApiParam(value = "dataTypeId", required = true) String dataTypeId,
            @RequestParam(value = "deviceId", required = false) @ApiParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "height", required = false) @ApiParam(value = "height", required = false) String height,
            @RequestParam(value = "width", required = false) @ApiParam(value = "wight", required = false) String width,
            @RequestParam(value = "src", required = false) @ApiParam(value = "src", required = false) String src,
            HttpServletRequest req, HttpServletResponse res) {

        UploadSreenInPO uploadSreenInPO = new UploadSreenInPO();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(dataTypeId);
        System.out.println("保存屏幕信息,源码===============>>>>>" + dataTypeId);
        if (scriptInfoBO != null) {
            String name = scriptInfoBO.getName();
            int scriptId = scriptInfoBO.getId();
            if (src != null) {
                String baseUrl = WebUtil.getBaseFileByEnv();
                byte[] srcByte = src.getBytes();
                System.out.println("有文件: 222   " + baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/src.txt");
                // baseSaoHDFS.deleteFile(baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId +"/1",true);
                baseSaoHDFS.uploadFile(srcByte, baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/src.txt");
            }
//            if (height != null && width != null && !height.isEmpty() && !width.isEmpty()) {
//                int deviceId = scriptInfoBO.getDeviceId();
//                DevicesInf devicesInf = new DevicesInf();
//                devicesInf.setHeight(Integer.parseInt(height));
//                devicesInf.setWidth(Integer.parseInt(width));
//                devicesInf.setId(deviceId);
//                scriptService.updateDevicesInfo(devicesInf);
//            }
        } else {
            throw new WebException(MySystemCode.GET_SCRTPT_INFO_NULL);
        }
        return uploadSreenInPO;
    }

    @RequestMapping(value = "/uploadingImg.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "上传图片", position = 0)
    public UploadSreenInPO uploadingImg(
            @RequestParam(value = "dataTypeId", required = true) @ApiParam(value = "dataTypeId", required = true) String dataTypeId,
            @RequestParam(value = "deviceId", required = true) @ApiParam(value = "deviceId", required = true) String deviceId,
            @RequestParam(value = "haveFile", required = false) @ApiParam(value = "haveFile", required = false) String haveFile,
            @RequestParam(value = "file", required = true) MultipartFile file,
            HttpServletRequest req, HttpServletResponse res) {
        UploadSreenInPO uploadSreenInPO = new UploadSreenInPO();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(dataTypeId);
        System.out.println("上传图片===============>>>>>" + dataTypeId);
        String name = scriptInfoBO.getName();
        int scriptId = scriptInfoBO.getId();
        String baseUrl = WebUtil.getBaseFileByEnv();
        String fileName = "screen_" + System.currentTimeMillis() + ".png";
        String path = baseUrl + "/screen_img" + "/" + scriptId + "/" + deviceId;
        if (haveFile != null && haveFile.equals("0")) {
            if (baseSaoHDFS.existsFile(path)) {
                List<String> screenImgPathList = baseSaoHDFS.listFiles(path);
                if (screenImgPathList.size() > 0) {
                    baseSaoHDFS.renameFile(screenImgPathList.get(0), path + "/" + fileName);
                }
            }
        } else {
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
                if (baseSaoHDFS.existsFile(path)) {
                    List<String> screenImgPathList = baseSaoHDFS.listFiles(path);
                    if (screenImgPathList.size() > 0) {
                        baseSaoHDFS.deleteFile(screenImgPathList.get(0), true);
                    }
                }
                baseSaoHDFS.uploadFile(fileBytes, path + "/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uploadSreenInPO;
    }

    @RequestMapping(value = "/saveCrawlSrc.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "点击抓取按钮", position = 0)
    public ClickCrawlPO saveCrawlSrc(
            @RequestParam(value = "scriptId", required = true) @ApiParam(value = "scriptId", required = true) String scriptId,
            @RequestParam(value = "deviceId", required = true) @ApiParam(value = "deviceId", required = true) String deviceId,
            @RequestParam(value = "step", required = true) @ApiParam(value = "step", required = true) String step,
            HttpServletRequest req, HttpServletResponse res) {
        ClickCrawlPO clickCrawlPO = new ClickCrawlPO();
        ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoById(scriptId);
        List<String> pathList = new ArrayList<>();
        if (scriptInfoBO != null) {
            String name = scriptInfoBO.getName();
            String baseUrl = WebUtil.getBaseFileByEnv();
            System.out.println("有文件: " + baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/src.txt");
            if (baseSaoHDFS.existsFile(baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/src.txt")) {
                System.out.println("有文件: ");
                byte[] srcByte = baseSaoHDFS.downloadFile(baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/src.txt");
                String fileName = scriptId + "_" + step + "_" + (new Date().getTime());
                baseSaoHDFS.uploadFile(srcByte, baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/" + step + "/" + fileName + ".txt");

                // 获得该步骤下的所有源码文件
                List<String> list = baseSaoHDFS.listFiles(baseUrl + "/screen_src" + "/" + scriptId + "/" + deviceId + "/" + step + "/");

                JSONArray objects = (JSONArray) JSONArray.toJSON(list);
                String newPath = String.valueOf(objects);

                clickCrawlPO.setPath(newPath);
                clickCrawlPO.setCount(list.size());
            }
        } else {
            System.out.print("1");
            throw new WebException(MySystemCode.GET_SCRTPT_INFO_NULL);
        }


        return clickCrawlPO;
    }

    @RequestMapping(value = "/getEnableDevices.json", method = RequestMethod.GET)
    @ResponseBody
    @Scope(value = "single")
    @ApiOperation(value = "获取可用设备", position = 0)
    public DevicesEnablePO getEnableDevices(@RequestParam(value = "funcation", required = false) @ApiParam(value = "funcation", required = false) String funcation,
                                            HttpServletRequest req, HttpServletResponse res) {
        DevicesEnablePO devicesEnablePO = new DevicesEnablePO();
        String deviceId = null;
        String baseListPath = WebUtil.getMcssDeviceList();
        String baseUsedPath = WebUtil.getMcssDeviceUsed();

        List<String> zkServerList = baseSaoZookeeper.getChildrenKeys(baseListPath);
        outer:
        for (String zkServerName : zkServerList) {
            DevicesInf devicesInf = new DevicesInf();
            devicesInf.setRegistServer(zkServerName);
            devicesInf.setFuncation(funcation);
            //获取对应本地运行服务器下的设备
            List<DevicesInf> devicesInfList = taskService.getDevicesByServerList(devicesInf);
            for (DevicesInf deviceInfo : devicesInfList) {
                if (!baseSaoZookeeper.isExisted(baseUsedPath + "/" + zkServerName + "_" + deviceInfo.getId())) {
                    try {
                        baseSaoZookeeper.create(baseUsedPath + "/" + zkServerName + "_" + deviceInfo.getId(), deviceInfo.getId() + "");
                        deviceId = deviceInfo.getId() + "";
                        devicesEnablePO.setId(deviceId);
                        devicesEnablePO.setServerName(zkServerName);
                        break outer;
                    } catch (Exception e) {
                        //创建失败
                        System.out.print("设备已在使用  >>>> 接着寻找下一个设备");
                    }
                } else {
                    System.out.print("设备已在使用  >>>> 接着寻找下一个设备");
                }
            }
        }

        if (deviceId == null) {
            throw new WebException(MySystemCode.GET_ENABLE_DEVICE_NULL);
        }

        return devicesEnablePO;
    }

    @RequestMapping(value = "/releaseDevices.json", method = RequestMethod.GET)
    @ResponseBody
    @Scope(value = "single")
    @ApiOperation(value = "释放设备", position = 0)
    public DeleteDevicePO releaseDevices(
            @RequestParam(value = "deviceId", required = true) @ApiParam(value = "deviceId", required = true) String deviceId,
            @RequestParam(value = "serverName", required = true) @ApiParam(value = "serverName", required = true) String serverName,
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam(value = "datasourceTypeId", required = true) String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        //1.删除控制台目录下图片  2.释放本地设备
        String baseUrl = WebUtil.getBaseFileByEnv();
        DeleteDevicePO deleteDevicePO = new DeleteDevicePO();
        String dealClass = "ReleaseDevices";
        //封装
        ScriptTask launchTask = new ScriptTask();
        launchTask.setDealClass(dealClass);
        launchTask.setDeviceId(deviceId);
        launchTask.setServerName(serverName);
        String paramTask = JSON.toJSONString(launchTask);
        //调用kafaka send方法
        baseKafkaProducer.send("device_" + serverName, paramTask);
        if (datasourceTypeId != null) {
            ScriptInfoBO scriptInfoBO = scriptService.getScriptInfoByDatatypeId(datasourceTypeId);  // 根据 DataSourceTypeId 来查询名字
            String scriptName = scriptInfoBO.getName();
            int scriptId = scriptInfoBO.getId();
            String path = baseUrl + "/screen_img" + "/" + scriptId + "/" + deviceId + "/";
            if (baseSaoHDFS.existsFile(path)) {
                List<String> screenImgPathList = baseSaoHDFS.listFiles(path);
                if (screenImgPathList.size() > 0) {
                    for (String screenImgPath : screenImgPathList) {
                        for (int i = 0; i < 8; i++) {
                            if (baseSaoHDFS.deleteFile(screenImgPath, false)) {
                                System.out.println("删除对应图片=================》成功");
                                break;
                            }
                            if (i == 7) {
                                System.out.println("删除对应图片=================》失败");
                            }
                        }
                    }
                }
            }
        }
        return deleteDevicePO;
    }


    @RequestMapping(value = "/test.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "测试类", position = 0)
    public void getTest(
            HttpServletRequest req, HttpServletResponse res) {
        Map<String, Object> crawlMap = new HashMap<>();
        List<Map<String, Object>> crawlMapList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<Map<String, Object>> crawlMapListSub = new ArrayList<>();
            if (i == 0) {
                for (int i2 = 0; i2 < 1; i2++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", "1992-01-23");
                    crawlMapListSub.add(map);
                }
            } else {
                for (int i2 = 0; i2 < 2; i2++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("content", i2 + "content");
                    map.put("title", i2 + "title");
                    crawlMapListSub.add(map);
                }
            }
            crawlMapList.addAll(crawlMapListSub);
        }
        crawlMapList.size();
    }

    private List<Map<String, Object>> getCrawlData(List<CrawlRegulationBO> crawlRegulationBOList, CrawlRegulationListBO crawlRegulationListBO, int i, List<SubTaskParam> subTaskParams, Map<String, Object> crawlMap) throws Exception {
        List<Map<String, Object>> crawlMapList = new ArrayList<>();
        String content = "<div class=\"bui-left index-content\"><div class=\"bui-box slide\"><ul class=\"slide-list bui-left\"><li ga_event=\"focus_list_click\" class=\"slide-item\"><a href=\"/group/6549120922471629320/\" target=\"_blank\"><img src=\"//p3.pstatp.com/origin/7b9b001b756971f9f1bf\" alt=\"\"> <p class=\"title\">“走崖壁爬藤梯” 易地扶贫 崖壁求学路即将成为历史</p></a></li><li ga_event=\"focus_list_click\" class=\"slide-item slide-item-active\"><a href=\"/group/6549292160296944132/\" target=\"_blank\"><img src=\"//p3.pstatp.com/origin/7b9a001b80ab506ccc3f\" alt=\"\"> <p class=\"title\">“黑科技”助力“旅游警察” 备战“五一”小长假</p></a></li><li ga_event=\"focus_list_click\" class=\"slide-item\"><a href=\"/group/6548632996084187656/\" target=\"_blank\"><img src=\"//p3.pstatp.com/origin/7e52000c78d383854ad4\" alt=\"\"> <p class=\"title\">杜海涛沈梦辰潮范十足同框现身，一路热聊停不下来狂撒狗粮</p></a></li><li ga_event=\"focus_list_click\" class=\"slide-item\"><a href=\"/group/6548306779921449486/\" target=\"_blank\"><img src=\"//p9.pstatp.com/origin/7e520009851e86e52740\" alt=\"\"> <p class=\"title\">辽宁体育局举行庆功表彰大会 众将士耐心为球迷签名</p></a></li><li ga_event=\"focus_list_click\" class=\"slide-item\"><a href=\"/group/6548626766175404548/\" target=\"_blank\"><img src=\"//p1.pstatp.com/origin/7b99001b98b755c68ace\" alt=\"\"> <p class=\"title\">第75集团军某空突旅：空地协同锤炼空突劲旅</p></a></li><li ga_event=\"focus_list_click\" class=\"slide-item\"><a href=\"/group/6548653126717538823/\" target=\"_blank\"><img src=\"//p3.pstatp.com/origin/7b9a0018907186bdc734\" alt=\"\"> <p class=\"title\">不塑料的真姐妹！大威与哈迪德姐妹助阵小威个人电影首映</p></a></li></ul> <ul class=\"slide-tab bui-right\"><li class=\"slide-tab-item\">\n" +
                "    要闻\n" +
                "  </li><li class=\"slide-tab-item slide-tab-item-active\">\n" +
                "    社会\n" +
                "  </li><li class=\"slide-tab-item\">\n" +
                "    娱乐\n" +
                "  </li><li class=\"slide-tab-item\">\n" +
                "    体育\n" +
                "  </li><li class=\"slide-tab-item\">\n" +
                "    军事\n" +
                "  </li><li class=\"slide-tab-item\">\n" +
                "    明星\n" +
                "  </li></ul></div> <div class=\"feed-infinite-wrapper\"><div class=\"loading ball-pulse\" style=\"display: none;\"><div></div> <div></div> <div></div> <span>推荐中⋅⋅⋅</span></div> <div><div class=\"msg-alert msg-alert-hidden\"><span>为您推荐了4篇文章</span> <!----></div> <div class=\"msgAlert-place\" style=\"display: none;\"><div ga_event=\"refresh_float_click\" class=\"msg-alert\"><span>您有未读新闻，点击查看</span> <i class=\"bui-icon icon-close_small\" style=\"font-size: 15px; color: rgb(255, 255, 255);\"></i></div></div></div> <ul infinite-scroll-disabled=\"loading\" infinite-scroll-immediate-check=\"containerCheck\" infinite-scroll-immediate-check-count=\"containerCheckCount\" infinite-scroll-distance=\"80\"><li class=\"\"><div ga_event=\"article_item_click\" class=\"bui-box single-mode\"><div ga_event=\"article_img_click\" class=\"bui-left single-mode-lbox\"><a href=\"/group/6549279046314754563/\" target=\"_blank\" class=\"img-wrap\"><img class=\"lazy-load-img\" src=\"//p1.pstatp.com/list/190x124/80a300038f738be44238\" lazy=\"loaded\"> <!----></a></div> <div class=\"single-mode-rbox\"><div class=\"single-mode-rbox-inner\"><div ga_event=\"article_title_click\" class=\"title-box\"><a href=\"/group/6549279046314754563/\" target=\"_blank\" class=\"link\">独家｜“家乡外交”后又一创新：中国驻印大使披露习莫会幕后</a></div> <div class=\"bui-box footer-bar\"><div class=\"bui-left footer-bar-left\"><!----> <a href=\"/c/user/51045089537/\" target=\"_blank\" ga_event=\"article_avatar_click\" class=\"footer-bar-action media-avatar\"><img src=\"//p1.pstatp.com/large/d290013b7aaaabcfff2\" lazy=\"loaded\"></a> <a href=\"/c/user/51045089537/\" target=\"_blank\" ga_event=\"article_name_click\" class=\"footer-bar-action source\">&nbsp;澎湃新闻&nbsp;⋅</a> <a href=\"/group/6549279046314754563//#comment_area\" target=\"_blank\" ga_event=\"article_comment_click\" class=\"footer-bar-action source\">&nbsp;1评论&nbsp;⋅</a> <span class=\"footer-bar-action\">&nbsp;7分钟前</span> <!----> <!----> <!----></div> <div class=\"bui-right\"><div ga_event=\"dislike_click\" class=\"action-dislike\" dislikeurl=\"/api/dislike/\"><i class=\"bui-icon icon-close_small\" style=\"font-size: 16px; color: rgb(221, 221, 221);\"></i>\n" +
                "  不感兴趣\n" +
                "</div></div></div></div></div></div></li><li class=\"\"><div ga_event=\"article_item_click\" class=\"bui-box single-mode\"><div ga_event=\"article_img_click\" class=\"bui-left single-mode-lbox\"><a href=\"/group/6549275469756236295/\" target=\"_blank\" class=\"img-wrap\"><img class=\"lazy-load-img\" src=\"//p1.pstatp.com/list/190x124/7e3b000ae775fa12fa43\" lazy=\"loaded\"> <!----></a></div> <div class=\"single-mode-rbox\"><div class=\"single-mode-rbox-inner\"><div ga_event=\"article_title_click\" class=\"title-box\"><a href=\"/group/6549275469756236295/\" target=\"_blank\" class=\"link\">陕西米脂通报4.27伤害案：死亡学生人数升至9人</a></div> <div class=\"bui-box footer-bar\"><div class=\"bui-left footer-bar-left\"><a href=\"news_society\" target=\"_blank\" ga_event=\"article_tag_click\" class=\"footer-bar-action tag tag-style-society\">社会</a> <a href=\"/search/?keyword=人民网\" ga_event=\"article_avatar_click\" class=\"footer-bar-action media-avatar avatar-style-4\">人</a> <a href=\"/search/?keyword=人民网\" target=\"_blank\" ga_event=\"article_name_click\" class=\"footer-bar-action source\">&nbsp;人民网&nbsp;⋅</a> <span class=\"footer-bar-action\">&nbsp;15分钟前</span> <!----> <!----> <!----></div> <div class=\"bui-right\"><div ga_event=\"dislike_click\" class=\"action-dislike\" dislikeurl=\"/api/dislike/\"><i class=\"bui-icon icon-close_small\" style=\"font-size: 16px; color: rgb(221, 221, 221);\"></i>\n" +
                "  不感兴趣\n" +
                "</div></div></div></div></div></div></li><li ad_id=\"1598777695807539\" ad_extra=\"{&quot;ad_price&quot;:&quot;WuPeF__yrcNa494X__Ktw0SCVdIKPSioeHBg-g&quot;,&quot;convert_component_suspend&quot;:0,&quot;convert_id&quot;:0,&quot;external_action&quot;:0,&quot;req_id&quot;:&quot;20180428103606010012061015964F04&quot;,&quot;rit&quot;:1}\" class=\"J_ad\" ad_show=\"0\"><div ga_event=\"ad_item_click\" class=\"bui-box single-mode\"><div ga_event=\"ad_img_click\" class=\"bui-left single-mode-lbox\"><a href=\"http://www.gdxxb.com/97930\" target=\"_blank\" class=\"img-wrap\"><img class=\"lazy-load-img\" src=\"//sf3-ttcdn-tos.pstatp.com/img/mosaic-legacy/78fb0027bf6f062073d1~640x0.image\" lazy=\"loaded\"> <!----></a></div> <div class=\"single-mode-rbox\"><div class=\"single-mode-rbox-inner\"><div ga_event=\"ad_title_click\" class=\"title-box\"><a href=\"http://www.gdxxb.com/97930\" target=\"_blank\" class=\"link\">装备永久保值，适合散人长期耐玩，重温2003的激情</a></div> <div class=\"bui-box footer-bar\"><div class=\"bui-left footer-bar-left\"><!----> <a href=\"/search/?keyword=散人传说\" ga_event=\"ad_avatar_click\" class=\"footer-bar-action media-avatar avatar-style-0\">散</a> <a href=\"/search/?keyword=散人传说\" target=\"_blank\" ga_event=\"ad_name_click\" class=\"footer-bar-action source\">&nbsp;散人传说&nbsp;⋅</a> <span class=\"footer-bar-action\">&nbsp;30分钟前</span> <!----> <!----> <a target=\"_blank\" href=\"https://ad.toutiao.com/promotion/?source2=pcfeedadtag\" class=\"footer-bar-action ad\">广告</a></div> <div class=\"bui-right\"><div ga_event=\"dislike_click\" class=\"action-dislike\" dislikeurl=\"/api/dislike/\"><i class=\"bui-icon icon-close_small\" style=\"font-size: 16px; color: rgb(221, 221, 221);\"></i>\n" +
                "  不感兴趣\n" +
                "</div></div></div></div></div></div></li><li class=\"\"><div ga_event=\"wenda_item_click\" class=\"bui-box single-mode\"><div ga_event=\"wenda_img_click\" class=\"bui-left single-mode-lbox\"><a href=\"/group/6531987069533683972/\" target=\"_blank\" class=\"img-wrap\"><img class=\"lazy-load-img\" src=\"//p3.pstatp.com/list/190x124/382f0018b76a65301880\" lazy=\"loaded\"> <!----></a></div> <div class=\"single-mode-rbox\"><div class=\"single-mode-rbox-inner\"><div ga_event=\"wenda_title_click\" class=\"title-box\"><a href=\"/group/6531987069533683972/\" target=\"_blank\" class=\"link\">肝脏不好，身体有四个地方会变黑，是指哪四个地方？</a></div> <div class=\"bui-box footer-bar\"><div class=\"bui-left footer-bar-left\"><a href=\"news_health\" target=\"_blank\" ga_event=\"article_tag_click\" class=\"footer-bar-action tag tag-style-other\">健康</a> <a href=\"/search/?keyword=悟空问答\" ga_event=\"wenda_avatar_click\" class=\"footer-bar-action media-avatar avatar-style-3\">悟</a> <a href=\"/search/?keyword=悟空问答\" target=\"_blank\" ga_event=\"wenda_name_click\" class=\"footer-bar-action source\">&nbsp;悟空问答&nbsp;⋅</a> <span class=\"footer-bar-action\">&nbsp;37分钟前</span> <!----> <!----> <!----></div> <div class=\"bui-right\"><div ga_event=\"dislike_click\" class=\"action-dislike\" dislikeurl=\"/api/dislike/\"><i class=\"bui-icon icon-close_small\" style=\"font-size: 16px; color: rgb(221, 221, 221);\"></i>\n" +
                "  不感兴趣\n" +
                "</div></div></div></div></div></div></li><li class=\"\"><div ga_event=\"refresh_item_click\" class=\"refresh-mode\"><span>37分钟前看到这里</span>\n" +
                "  &nbsp;点击刷新&nbsp;<i class=\"bui-icon icon-refresh\" style=\"font-size: 12px; color: rgb(42, 144, 215);\"></i></div></li></ul> <div class=\"loading ball-pulse\" style=\"display: none;\"><div></div> <div></div> <div></div> <span>加载中⋅⋅⋅</span></div></div></div>";
        List<String> contentList = new ArrayList<>();
        //执行抓取分页脚本
        if (crawlRegulationListBO != null && crawlRegulationListBO.getStep() == i) {
            //前置处理
            CrawlAction crawlAction = new CrawlAction();
            String beforeprocessorArray = crawlRegulationListBO.getBeforeprocessorArray();
            content = crawlAction.beforeProcessorAction(beforeprocessorArray, content);
            //抓取规则
            String crawlArray = crawlRegulationListBO.getCrawlArray();
            List<String> crawlStringList = crawlAction.listCrawl(crawlArray, content);
            for (String crawlString : crawlStringList) {
                //后置处理
                String afterProcessorArray = crawlRegulationListBO.getAfterprocessorArray();
                crawlString = crawlAction.afterProcessorAction(afterProcessorArray, crawlString);
                contentList.add(crawlString);
            }
        } else {
            contentList.add(content);
        }

        for (String contents : contentList) {
            //执行抓取内容脚本
            List<CrawlRegulationBO> crawlRegulationBOStepList = new ArrayList<>();
            for (CrawlRegulationBO crawlRegulationBO : crawlRegulationBOList) {
                if (crawlRegulationBO.getStep().equals(i + "")) {
                    crawlRegulationBOStepList.add(crawlRegulationBO);
                }
            }
            //如果该步骤有抓取任务,获取页面,执行抓取脚本
            if (crawlRegulationBOStepList.size() > 0) {
                for (CrawlRegulationBO crawlRegulationBO : crawlRegulationBOStepList) {
                    CrawlAction crawlAction = new CrawlAction();
                    String crawlString = crawlAction.crawl(crawlRegulationBO, contents, subTaskParams, null);
                    Object crawlItem = crawlString;
                    if (crawlRegulationBO.getType().equalsIgnoreCase("int")) {
                        if (crawlString == null) {
                            crawlItem = 0;
                        } else {
                            try {
                                crawlItem = Integer.parseInt(crawlString);
                            } catch (Exception e) {
                                crawlItem = 0;
                            }
                        }
                    } else if (crawlRegulationBO.getType().equalsIgnoreCase("datetime")) {
                        if (crawlString == null) {
                            crawlItem = System.currentTimeMillis();
                        } else {
                            crawlItem = DateUtil.parseDate(crawlString);
                        }
                    }
                    crawlMap.put(crawlRegulationBO.getItem(), crawlItem);
                }
                crawlMapList.add(crawlMap);
            }
        }
        return crawlMapList;
    }
}
