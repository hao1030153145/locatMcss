package com.transing.mcss4dpm.biz.service.impl.api.script;

import com.transing.mcss4dpm.biz.service.impl.api.DriverManager2;
import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.WeiXinTencentTask;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AppiumDriverManager;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AppiumSettingBo;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXInTencentBO;
import com.transing.mcss4dpm.util.DateUtil;
import com.transing.mcss4dpm.util.ParseUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/4/11
 */
public class CrawlWeiXinTencentCommentScript {
    private AppiumDriverManager appiumDriverManager;
    private DriverManager2 driverManager;
    private ExecutorService executor;
    private String chromeDriverId;
    private AndroidDriver driver;
    private String pid;
    private String datasourceTypeId;
    private String parent = "";

    public CrawlWeiXinTencentCommentScript(AppiumDriverManager appiumDriverManager, DriverManager2 driverManager, String datasourceTypeId) {
        this.appiumDriverManager = appiumDriverManager;
        this.driverManager = driverManager;
        this.datasourceTypeId = datasourceTypeId;
        driver = appiumDriverManager.getAndroidDriver();
        pid = appiumDriverManager.getServicePid();
    }

    /**
     * 简单描述：原生页面操作
     */
    public List<CrawlWeiXInTencentBO> operateProcess(WeiXinTencentTask taskInput) throws InterruptedException, IOException {
        int tryLoopNu = 0;
        String taskUrl = taskInput.getUrl();
        System.out.println("XXXX: URL : " + taskUrl);
        if (taskUrl.isEmpty()) {
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

        parent = taskUrl;
        System.out.println("XXXX: 开始执行脚本");
        driver.findElementByXPath("//*[@text='刘玮琪']").click();
        driver.findElementById("com.tencent.mm:id/aac").sendKeys(taskUrl);//在文本框中输入url
        driver.findElementById("com.tencent.mm:id/aai").click();//点击发送
        driver.swipe(1000, 2000, 1000, 2000, 10);
        Thread.currentThread().sleep(8000);
        driver.swipe(1000, 1300, 1000, 1300, 10);
        //延迟0.3秒,为了等待消息发送显示到对话框中
        Thread.currentThread().sleep(2000);
        for (int i = 0; i < 20; i++) {
            System.out.println("循环低 : " + i + "次");
            for (int i2 = 0; i2 < 5; i2++) {
                driver.swipe(700, 2000, 700, 300, 700);
                Thread.currentThread().sleep(1000);
            }
            driver.swipe(700, 2200, 700, 2200, 10);
            Thread.currentThread().sleep(1000);
        }
        while (tryLoopNu < 20) {
            boolean isSucce = operateX5Web();
            if (isSucce) {
                //切换webview成功
                String url = driver.getCurrentUrl();
                //抓取
                System.out.println("到抓取了哈 >>>>>>>");
                List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOList = insertToWeixinBrand(url, driver.getPageSource());
                Thread.currentThread().sleep(2 * 1000);
                driver.context("NATIVE_APP");
                Thread.currentThread().sleep(2 * 1000);
                //关闭chromeDriver
                try {
                    Process process = Runtime.getRuntime().exec("kill -s 9 " + chromeDriverId);
                    process.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return crawlWeiXInTencentBOList;
            } else {
                System.out.println("XXXX: webview try again");
                tryLoopNu++;
            }
        }
        return null;
    }

    /**
     * 简单描述：x5浏览器页面操作
     */
    private boolean operateX5Web() throws InterruptedException, IOException {
        try {
            //切换webView
            chromeDriverId = changeWebViewContext();
            return true;
        } catch (Exception e) {
            //切换webview失败,删除chromeDriver
            e.printStackTrace();
            System.out.println("XXXX: clean chrome");
            Thread.currentThread().sleep(2 * 1000);
            Process process2 = Runtime.getRuntime().exec("pstree " + pid + " -p");
            process2.waitFor();
            Scanner in3 = new Scanner(process2.getInputStream());
            while (in3.hasNext()) {
                String processInf = in3.nextLine();
                if (processInf.contains("chromedriver_64")) {
                    String id = processInf.trim().split("chromedriver_64")[1];
                    id = id.split("-\\+-")[0];
                    id = id.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& ;*（）——+|{}【】‘；：”“’。，、？|-]", "");
                    chromeDriverId = id;
                    System.out.println("XXXX: clean chromeId" + chromeDriverId);
                }
            }
            Thread.currentThread().sleep(3 * 1000);
            if (chromeDriverId != null) {
                if (!chromeDriverId.equals("")) {
                    Process process = Runtime.getRuntime().exec("kill -s 9 " + chromeDriverId);
                    process.waitFor();
                }
                chromeDriverId = "";
                //切换失败回退到聊天页面
                return false;
            } else {
                chromeDriverId = "";
                return false;
            }
        }
    }

    private String changeWebViewContext() throws InterruptedException, IOException, ExecutionException, TimeoutException {
        String chromeDriverId = "";
        executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(() -> {
            driver.context("WEBVIEW_com.tencent.mm:tools");
            return 1;
        });
        if (future.get(6, TimeUnit.SECONDS) == 1) {
            Process process = Runtime.getRuntime().exec("pstree " + pid + " -p");
            process.waitFor();
            Scanner in3 = new Scanner(process.getInputStream());
            while (in3.hasNext()) {
                String processInf = in3.nextLine();
                if (processInf.contains("chromedriver_64")) {
                    String id = processInf.trim().split("chromedriver_64")[1];
                    id = id.split("-\\+-")[0];
                    id = id.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& ;*（）——+|{}【】‘；：”“’。，、？|-]", "");
                    chromeDriverId = id;
                }
            }
        }
        return chromeDriverId;
    }

    private List<CrawlWeiXInTencentBO> insertToWeixinBrand(String currentUrl, String httpContent) {
        List<CrawlWeiXInTencentBO> crawlWeiXInTencentBOList = new ArrayList<>();
        try {
            List<String> commentList = ParseUtil.parseValue(httpContent, "//*[@id='discuss_list']/li", "xpath", null);
            if (commentList != null) {
                System.out.println("评论条数 >>>>>>>" + commentList.size());
                for (String comment : commentList) {
                    CrawlWeiXInTencentBO crawlWeiXInTencentBO = new CrawlWeiXInTencentBO();
                    List<String> iconList = ParseUtil.parseValue(comment, "//*[@class='header']/@src", "xpath", null);
                    if (iconList != null && iconList.size() > 0) {
                        crawlWeiXInTencentBO.setIcon(iconList.get(0));
                        System.out.println("头像 >>>>>>>" + iconList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setIcon("");
                    }
                    List<String> authorList = ParseUtil.parseValue(comment, "//*[@class='text_primary user_name']/text()", "xpath", null);
                    if (authorList != null && authorList.size() > 0) {
                        crawlWeiXInTencentBO.setAuthor(authorList.get(0));
                        System.out.println("作者 >>>>>>>" + authorList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setAuthor("");
                    }
                    List<String> contentList = ParseUtil.parseValue(comment, "//*[@class='text_default content ']/text()", "xpath", null);
                    if (contentList != null && contentList.size() > 0) {
                        crawlWeiXInTencentBO.setContent(contentList.get(0));
                        System.out.println("内容 >>>>>>>" + contentList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setContent("");
                    }
                    List<String> datetimeList = ParseUtil.parseValue(comment, "//*[@class='date']/text()", "xpath", null);
                    if (datetimeList != null && datetimeList.size() > 0) {
                        Date date = DateUtil.parseDate(datetimeList.get(0));
                        crawlWeiXInTencentBO.setDatetime(date);
                        System.out.println("时间 >>>>>>>" + datetimeList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setDatetime(new Date());
                    }
                    List<String> urlList = ParseUtil.parseValue(comment, "//*/@data-comment-id", "xpath", null);
                    if (urlList != null && urlList.size() > 0) {
                        crawlWeiXInTencentBO.setUrl(urlList.get(0));
                        System.out.println("URL >>>>>>>" + urlList.get(0));
                    } else {
                        crawlWeiXInTencentBO.setUrl("");
                    }
                    crawlWeiXInTencentBO.setParent(parent);
                    crawlWeiXInTencentBO.setFrom("nul");
                    crawlWeiXInTencentBOList.add(crawlWeiXInTencentBO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crawlWeiXInTencentBOList;
    }


}
