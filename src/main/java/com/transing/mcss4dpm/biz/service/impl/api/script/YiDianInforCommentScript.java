package com.transing.mcss4dpm.biz.service.impl.api.script;

import com.jeeframework.util.encrypt.MD5Util;
import com.transing.mcss4dpm.biz.service.impl.api.DriverManager2;
import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.YiDianInforTask;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AppiumDriverManager;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AppiumSettingBo;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXInTencentBO;
import com.transing.mcss4dpm.util.DateUtil;
import com.transing.mcss4dpm.util.ParseUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/4/13
 */
public class YiDianInforCommentScript {
    private AppiumDriverManager appiumDriverManager;
    private DriverManager2 driverManager;
    private ExecutorService executor;
    private String chromeDriverId;
    private AndroidDriver driver;
    private String pid;
    private String datasourceTypeId;
    private String parent = "";

    public YiDianInforCommentScript(AppiumDriverManager appiumDriverManager, DriverManager2 driverManager, String datasourceTypeId) {
        this.appiumDriverManager = appiumDriverManager;
        this.driverManager = driverManager;
        this.datasourceTypeId = datasourceTypeId;
        driver = appiumDriverManager.getAndroidDriver();
        pid = appiumDriverManager.getServicePid();
    }


    /**
     * 简单描述：原生页面操作
     */
    public List<CrawlWeiXInTencentBO> operateProcess(YiDianInforTask taskInput) throws InterruptedException, IOException {
        List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOList = new ArrayList<>();
        int tryLoopNu = 0;
        String keyword = taskInput.getKeyword();
        if (keyword != null) {
            if (keyword != null) {
                keyword = keyword.replaceAll("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
                Pattern pattern = Pattern.compile("[\\d]");
                Matcher matcher = pattern.matcher(keyword);
                keyword = matcher.replaceAll("").trim();
                if (keyword.length() >= 10) {
                    keyword = keyword.substring(0, 6) + " " + keyword.substring(6, 10);
                }
            }
        }
        parent = taskInput.getUrl();
        System.out.println("XXXX: KEYWORD : " + keyword);
        System.out.println("XXXX: URL : " + parent);
        if (keyword != null && keyword.isEmpty()) {
            return null;
        }
        //根据数据源类型id获取启动参数
        AppiumSettingBo appiumSettingBo = driverManager.getApplicationSettingBo(datasourceTypeId);
        if(appiumSettingBo==null){
            System.out.println("没有被配置的数据源类型>>>>>>>>>>  ");
            return null;
        }
        //启动services
        pid = driverManager.startAppiumService(appiumDriverManager.getPort(), appiumDriverManager.getBp(), appiumDriverManager.getCp(), appiumDriverManager.getDeviceInfo().getDevicesName(), String.valueOf(appiumSettingBo.getNewCommandTimeout()));
        appiumDriverManager.setServicePid(pid);
        System.out.println(appiumDriverManager.getDeviceInfo().getDevicesName() + "的 servicePid 是  >>>>>>>>>>   : " + pid);
        appiumDriverManager.setStatus("1");
        //启动client
        DesiredCapabilities desiredCapabilities = driverManager.appiumSetting(appiumSettingBo);
        if (appiumSettingBo.getNewCommandTimeout() != null && appiumSettingBo.getNewCommandTimeout() > 0) {
            appiumDriverManager.setDelay(appiumSettingBo.getNewCommandTimeout() * 1000);
        } else {
            appiumDriverManager.setDelay(300 * 1000);
        }
        driver = driverManager.launchAppium(desiredCapabilities, appiumDriverManager, 10);
        //把该设备调整为启动运行状态
        appiumDriverManager.setAndroidDriver(driver);
        appiumDriverManager.setBindApp(appiumSettingBo.getName());
        appiumDriverManager.setDatasourceTypeId(datasourceTypeId);
        appiumDriverManager.setStatus("2");

        try {
            driver.findElementById("com.hipu.yidian:id/txtSearch").click();
            driver.findElementById("com.hipu.yidian:id/edtKeyword").sendKeys(keyword);
            driver.findElementById("com.hipu.yidian:id/btnSearch").click();
            driver.findElementById("com.hipu.yidian:id/news_title").click();
        } catch (Exception e) {
            return crawlWeiXInTencentBOList;
        }
        Thread.sleep(2000);
        driver.findElementById("com.hipu.yidian:id/frame_comments_number").click();
        Thread.sleep(2000);
        int maxWhile = 0;
        while (true) {
            if (maxWhile > 100) {
                break;
            }
            String content = driver.getPageSource();
            try {

                List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOSubList = insertToWeixinBrand(keyword, content);
                crawlWeiXInTencentBOList.addAll(crawlWeiXInTencentBOSubList);
                List<String> isComplete = ParseUtil.parseValue(content, "//*[@resource-id='com.hipu.yidian:id/messageTxt']/@text", "xpath", null);
                if (isComplete != null && isComplete.size() > 0) {
                    if (isComplete.get(0).equalsIgnoreCase("已显示所有评论")) {
                        break;
                    }
                }
                driver.swipe(1000, 2000, 1000, 500, 1000);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            } finally {
                maxWhile++;
            }
        }
        return crawlWeiXInTencentBOList;
    }

    private List<CrawlWeiXInTencentBO> insertToWeixinBrand(String keyword, String httpContent) {
        List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOList = new ArrayList<>();
        try {
            List<String> commentList = ParseUtil.parseValue(httpContent, "//*[@resource-id='com.hipu.yidian:id/normal_comment']", "xpath", null);
            if (commentList != null) {
                for (String comment : commentList) {
                    boolean isCompletion = false;
                    CrawlWeiXInTencentBO crawlWeiXInTencentBO = new CrawlWeiXInTencentBO();
                    List<String> ltimesList = ParseUtil.parseValue(comment, "//*[@resource-id='com.hipu.yidian:id/likeCount']/@text", "xpath", null);
                    if (ltimesList != null && ltimesList.size() > 0) {
                        try {
                            crawlWeiXInTencentBO.setLtimes(Integer.parseInt(ltimesList.get(0)));
                            System.out.println("点赞数 >>>>>>>" + ltimesList.get(0));
                        } catch (Exception e) {
                            crawlWeiXInTencentBO.setLtimes(0);
                        }
                    } else {
                        crawlWeiXInTencentBO.setLtimes(0);
                    }
                    List<String> authorList = ParseUtil.parseValue(comment, "//*[@resource-id='com.hipu.yidian:id/name']/@text", "xpath", null);
                    if (authorList != null && authorList.size() > 0) {
                        crawlWeiXInTencentBO.setAuthor(authorList.get(0));
                        System.out.println("作者 >>>>>>>" + authorList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setAuthor("");
                    }
                    List<String> contentList = ParseUtil.parseValue(comment, "//*[@resource-id='com.hipu.yidian:id/comment']/@text", "xpath", null);
                    if (contentList != null && contentList.size() > 0) {
                        crawlWeiXInTencentBO.setContent(contentList.get(0));
                        System.out.println("内容 >>>>>>>" + contentList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setContent("");
                    }
                    List<String> datetimeList = ParseUtil.parseValue(comment, "//*[@resource-id='com.hipu.yidian:id/time']/@text", "xpath", null);
                    if (datetimeList != null && datetimeList.size() > 0) {
                        Date date = DateUtil.parseDate(datetimeList.get(0));
                        crawlWeiXInTencentBO.setDatetime(date);
                        System.out.println("时间 >>>>>>>" + datetimeList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setDatetime(new Date());
                    }
                    if (authorList != null && authorList.size() > 0 && contentList != null && contentList.size() > 0) {
                        isCompletion = true;
                    }
                    if (isCompletion) {
                        crawlWeiXInTencentBO.setParent(parent);
                        crawlWeiXInTencentBO.setFrom("nul");
                        String uniqueValue = MD5Util.encrypt(authorList.get(0) + contentList.get(0));
                        crawlWeiXInTencentBO.setUniqueValue(uniqueValue);
                        System.out.println("uniqueValue >>>>>>>" + uniqueValue);
                        crawlWeiXInTencentBOList.add(crawlWeiXInTencentBO);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crawlWeiXInTencentBOList;
    }
}
