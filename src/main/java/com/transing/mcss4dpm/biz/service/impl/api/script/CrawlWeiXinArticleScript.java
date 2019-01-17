package com.transing.mcss4dpm.biz.service.impl.api.script;

import com.transing.mcss4dpm.biz.service.impl.api.TaskInputParam.WeixinArticleTask;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AndroidDriverStatus;
import com.transing.mcss4dpm.integration.bo.CrawlWeiXinArticleBO;
import com.transing.mcss4dpm.integration.bo.WeixinBrandBO;
import com.transing.mcss4dpm.integration.bo.WeixinCommentBO;
import com.transing.mcss4dpm.util.XpathUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class CrawlWeiXinArticleScript {
    private AndroidDriverStatus driverStatus;
    private ExecutorService executor;
    private String chromeDriverId;
    private AndroidDriver driver;
    private String pid;

    public CrawlWeiXinArticleScript(AndroidDriverStatus driverStatus) {
        this.driverStatus = driverStatus;
        driver = driverStatus.getAndroidDriver();
        pid = driverStatus.getServicePid();
    }

    /**
     * 简单描述：原生页面操作
     */
    public CrawlWeiXinArticleBO operateProcess(WeixinArticleTask taskInput) throws InterruptedException, IOException {
        int tryLoopNu = 0;
        String taskUrl = taskInput.getUrl();
        System.out.println("XXXX: URL : " + taskUrl);
        if (taskUrl.isEmpty()) {
            return null;
        }

        byte[] bytes=driver.getScreenshotAs(OutputType.BYTES);
        driver.findElementById("com.tencent.mm:id/a27").sendKeys(taskUrl);//在文本框中输入url
        driver.findElementById("com.tencent.mm:id/a2c").click();//点击发送
        //延迟0.3秒,为了等待消息发送显示到对话框中
        Thread.currentThread().sleep(300);
        while (tryLoopNu < 10) {
            System.out.println("XXXX: tryLoopNu : " + tryLoopNu);
            List<WebElement> elements = driver.findElementsById("com.tencent.mm:id/ib");//获取对话内容列表
            if (elements.size() > 1) {
                elements.get(elements.size() - 1).click();//点击最后一条
                boolean isSucce = operateX5Web();
                if (isSucce) {
                    //切换webview成功
                    String url = driver.getCurrentUrl();
                    CrawlWeiXinArticleBO uploadSougouBO = insertToWeixinBrand(url, driver.getPageSource());
                    uploadSougouBO.setFormerUrl(taskUrl);
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
                    driver.swipe(60, 100, 60, 100, 10);
                    return uploadSougouBO;
                } else {
                    System.out.println("XXXX: webview try again");
                    tryLoopNu++;
                }
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
                driver.context("NATIVE_APP");
                driver.swipe(60, 100, 60, 100, 10);
                return false;
            }else {
                chromeDriverId = "";
                driver.swipe(60, 100, 60, 100, 10);
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

    private CrawlWeiXinArticleBO insertToWeixinBrand(String currentUrl, String httpContent) {
        WeixinBrandBO weixinBrand = new WeixinBrandBO();
        weixinBrand.setUrl(currentUrl);
        Map<String, Object> map = XpathUtil.getWeixinBrand2ByHtml(httpContent, weixinBrand);
        WeixinBrandBO weixinBrandBO = (WeixinBrandBO) map.get("weixinBrandBO");
        // 更新
        List<WeixinCommentBO> weixinCommentBOList = (List<WeixinCommentBO>) map.get("weixinCommentBOList");

        CrawlWeiXinArticleBO crawlWeiXinArticleBO = getUplpadSougouBo(weixinBrandBO, weixinCommentBOList, currentUrl);
        return crawlWeiXinArticleBO;
    }

    private CrawlWeiXinArticleBO getUplpadSougouBo(WeixinBrandBO weixinBrandBO, List<WeixinCommentBO> weixinCommentBOList, String currentUrl) {
        CrawlWeiXinArticleBO crawlWeiXinArticleBO = new CrawlWeiXinArticleBO();
        String pageViews = weixinBrandBO.getPageViews();
        String praiseNum = weixinBrandBO.getPraiseNum();
        String title = weixinBrandBO.getTitle();
        String author = weixinBrandBO.getAuthor();
        String source = weixinBrandBO.getSource();
        String weixinId = weixinBrandBO.getWeixinId();
        String content = weixinBrandBO.getContent();
        String publishTime =weixinBrandBO.getPublishTime();
        String imgUrl=weixinBrandBO.getImgUrl();
        if (pageViews == null) {
            pageViews = "0";
        }
        if (pageViews.contains("+")) {
            pageViews = "100000";
        }
        if (praiseNum != null) {
            if (praiseNum.equals("赞")) {
                praiseNum = "0";
            }
        } else {
            praiseNum = "0";
        }

        crawlWeiXinArticleBO.setUrl(currentUrl);
        crawlWeiXinArticleBO.setReplytimes(praiseNum);
        crawlWeiXinArticleBO.setViewtimes(pageViews);
        crawlWeiXinArticleBO.setCommentlist(weixinCommentBOList);
        crawlWeiXinArticleBO.setTitle(title);
        crawlWeiXinArticleBO.setAuthor(author);
        crawlWeiXinArticleBO.setSource(source);
        crawlWeiXinArticleBO.setWeixinId(weixinId);
        crawlWeiXinArticleBO.setContent(content);
        crawlWeiXinArticleBO.setPublishTime(publishTime);
        crawlWeiXinArticleBO.setImgUrl(imgUrl);
        return crawlWeiXinArticleBO;
    }
}
