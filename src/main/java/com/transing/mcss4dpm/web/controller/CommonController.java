package com.transing.mcss4dpm.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.transing.mcss4dpm.biz.service.CommonService;
import com.transing.mcss4dpm.biz.service.CrawlRegulationService;
import com.transing.mcss4dpm.integration.bo.CrawlRegulationTestBO;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import com.transing.mcss4dpm.util.WebUtil;
import com.transing.mcss4dpm.web.po.DataPO;
import com.transing.mcss4dpm.web.po.StorageTypeFieldPO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/13
 */
@Controller("commonController")
@Api(value = "通用接口", description = "通用接口", position = 2)
@RequestMapping(path = "/common")
public class CommonController {
    @Resource
    private CommonService commonService;
    @Resource
    private CrawlRegulationService crawlRegulationService;

    @RequestMapping(value = "/getStorageTypeFieldByDatasourceTypeId.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "", position = 0)
    public List<StorageTypeFieldPO> getStorageTypeFieldByDatasourceTypeId(
            @RequestParam(value = "datasourceTypeId", required = true) @ApiParam String datasourceTypeId,
            HttpServletRequest req, HttpServletResponse res) {
        List<StorageTypeFieldPO> idList = new ArrayList<>();
        //根据数据源类型查看存储表名
        String baseServer = WebUtil.getBaseDataServerByEnv();
        String getDataUrl = "/common/getDataSourceTypeAndTableName.json" + "?datasourceTypeId=" + datasourceTypeId;
        Map<String, String> dataTableNameMap = new HashMap<String, String>();
        Object dataSourceTableObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataTableNameMap);
        if (null != dataSourceTableObject) {
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) dataSourceTableObject;
            if (!jsonObject.containsKey("storageTypeTable")) {
                return idList;
            }
            String storageTypeTable = jsonObject.getString("storageTypeTable");
//            String storageTypeName = jsonObject.getString("storageTypeName");
            if (!storageTypeTable.equals("")) {
                getDataUrl = "/common/getStorageTypeFieldList.json" + "?datasourceTypeId=" + datasourceTypeId + "&storageTypeId=" + storageTypeTable;

                Map<String, String> dataFieldMap = new HashMap<String, String>();
                Object fieldObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataFieldMap);
                if (null != fieldObject) {
                    Field[] fields;
                    net.sf.json.JSONArray fieldJsonArray;
                    fieldJsonArray = (net.sf.json.JSONArray) fieldObject;
                    List<CrawlRegulationTestBO> crawlRegulationTestBOList = crawlRegulationService.selectRegulationTotalBydataTypeId(Integer.valueOf(datasourceTypeId));
                    for (int i = 0; i < fieldJsonArray.size(); i++) {
                        net.sf.json.JSONObject json = (net.sf.json.JSONObject) fieldJsonArray.get(i);
                        String fieldEnName = json.getString("fieldEnName");
                        if (i == 11) {
                            i = 11;
                        }
                        for (CrawlRegulationTestBO crawlRegulationTestBO : crawlRegulationTestBOList) {
                            if (crawlRegulationTestBO.getItem().equalsIgnoreCase(fieldEnName)) {
                                StorageTypeFieldPO storageTypeFieldPO = new StorageTypeFieldPO();
                                storageTypeFieldPO.setStorageFieldId(json.getString("id"));
                                idList.add(storageTypeFieldPO);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return idList;
    }

    @RequestMapping(value = "/addDevice.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "添加设备", position = 0)
    public DataPO addDevices(
            @RequestParam(value = "deviceName", required = true) @ApiParam String deviceName,
            HttpServletRequest req, HttpServletResponse res) {
        commonService.addDevices(deviceName);
        return new DataPO();
    }

    @RequestMapping(value = "/removeDevice.json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "删除设备", position = 0)
    public DataPO removeDevices(
            @RequestParam(value = "deviceName", required = true) @ApiParam String deviceName,
            HttpServletRequest req, HttpServletResponse res) {
        commonService.removeDevices(deviceName);
        return new DataPO();
    }
}
