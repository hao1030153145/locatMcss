package com.transing.mcss4dpm.biz.service.impl.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.transing.mcss4dpm.biz.service.impl.api.bo.AndroidDriverStatus;
import com.transing.mcss4dpm.biz.service.impl.api.bo.ChromeOption;
import com.transing.mcss4dpm.util.CallRemoteServiceUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单描述：设备控制类,用于设备的初始化,启动,删除,
 * <p>
 *
 * @
 */
public class DriverManager {
    private static Lock lock=new ReentrantLock();
    public static List<AndroidDriverStatus> driverList = new ArrayList<>(); //可用设备
    private static DriverManager instance = null;

    //静态工厂方法
    public static DriverManager getInstance() {
        if (instance == null) {
            instance = new DriverManager();
        }
        return instance;
    }

    private DriverManager() {
        initDriver();
    }

    private void initDriver() {
        System.out.println("XXXX: location_mcss : driverManager initDriver");
        int port = 4726;
        int bp = 4721;
        //调用远程mcss接口获取设备
        List<String> devicesNameList = getAndroidDevices();
        executeShell("adb start-server");

        //启动同等设备数量的appium-server
        for (String devicesName : devicesNameList) {
            executeShell("adb connect " + devicesName);
            int loop = 0; //循环出错次数
            while (loop < 3) {
                loop++;
                String servicePid = startAppiumService(port, bp, devicesName);
                System.out.println("XXXX: location_mcss : driverManager servicePid: " + servicePid);
                if (servicePid.equals("")) {
                    //启动appium失败,删除node,避免实际上已经启动了appium,但是删除失败,重新启动appium端口占用的错误
                    System.out.println("XXXX: pid is empty , kill appium-server");
                    stopAppium(servicePid,devicesName);
                } else {
                    //启动appium成功
                    System.out.println("XXXX: location_mcss : driverManager node_server sucess");
                    AndroidDriverStatus androidDriverStatus = new AndroidDriverStatus();
                    androidDriverStatus.setPort(port);
                    androidDriverStatus.setBp(bp);
                    androidDriverStatus.setDeviceName(devicesName);
                    androidDriverStatus.setServicePid(servicePid);
                    driverList.add(androidDriverStatus);
                    port = port + 1;
                    bp = bp + 1;
                    break;
                }
            }
        }

        //创建微信文章driver
        ChromeOption chromeOption = new ChromeOption();
        chromeOption.setAndroidPackage("com.tencent.mm");
        chromeOption.setAndroidActivity(".plugin.webview.ui.tools.WebViewUI");
        chromeOption.setAndroidProcess("com.tencent.mm:tools");
        getEnableSettingDriverManager("com.tencent.mm", ".ui.LauncherUI", chromeOption);
    }

    private String startAppiumService(int port, int bp, String devicesName) {
        lock.lock();
        List<String> nodeProcessIdBe = new ArrayList<>();
        List<String> nodeProcessIdAf = new ArrayList<>();
        String pid = "";
        try {
            System.out.println("XXXX: location_mcss : access shell : ps -aux");
            Process processBe = Runtime.getRuntime().exec("ps -aux");
            processBe.waitFor();
            System.out.println("XXXX: location_mcss : access shell : ps -aux success");
            Scanner in = new Scanner(processBe.getInputStream());
            nodeProcessIdBe.addAll(getNodePidList(in));
        } catch (IOException | InterruptedException ioe) {
            System.out.println("XXXX: location_mcss : access erro shell : ps -aux :" + ioe.toString());
            ioe.printStackTrace();
        }

//        String str = "/home/weiqi/tim_downlaod/node_modules/.bin/appium -p " + port + " -bp " + bp + " --device-name " + devicesName + " -U " + devicesName;
        String str = "appium -p " + port + " -bp " + bp + " --device-name " + devicesName + " -U " + devicesName;
        try {
            System.out.println("XXXX: appium start command " + str);
            Runtime.getRuntime().exec(str);
            System.out.println("XXXX: appium start command 2");
            try {
                System.out.println("XXXX: appium start command sleep");
                Thread.sleep(6 * 1000);
            } catch (InterruptedException e) {
                System.out.println("XXXX: appium start command sleep error:" + e.toString());
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            System.out.println("XXXX: appium start error: " + ioe.toString());
            ioe.printStackTrace();
        }
        //遍历系统进程,获取启动的node.exe进程号
        try {
            Process processAf = Runtime.getRuntime().exec("ps -aux");
            processAf.waitFor();
            Scanner in = new Scanner(processAf.getInputStream());
            nodeProcessIdAf.addAll(getNodePidList(in));
            int nodeProcessIdAfLength = nodeProcessIdAf.size();
            for (int i = nodeProcessIdAfLength - 1; i >= 0; i--) {
                String string = nodeProcessIdAf.get(i);
                if (nodeProcessIdBe.contains(string)) {
                    nodeProcessIdAf.remove(i);
                }
            }

            if (nodeProcessIdAf.size() > 0) {
                pid = nodeProcessIdAf.get(0);
            }
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        } finally {
            lock.unlock();
        }
        return pid;
    }

    private List<String> getNodePidList(Scanner in) {
        List<String> pidList = new ArrayList<>();
        while (in.hasNext()) {
            String processInf = in.nextLine();
            if (processInf.contains("node")) {
                String[] strings = processInf.split("\\s+");
                pidList.add(strings[1]);
            }
        }
        return pidList;
    }

    /*
     * 获取可用的driver
     */
    public AndroidDriverStatus getEnableDriver() {
        for (AndroidDriverStatus androidDriverStatus : driverList) {
            if (androidDriverStatus.getStatus().equalsIgnoreCase("0")) {
                androidDriverStatus.setStatus("1");
                return androidDriverStatus;
            }
        }
        return null;
    }

    /*
     * 释放设备
     */
    public boolean releaseDriver(String deviceName) {
        for (AndroidDriverStatus androidDriverStatus : driverList) {
            if (androidDriverStatus.getDeviceName().equalsIgnoreCase(deviceName)) {
                androidDriverStatus.setStatus("0");
                return true;
            }
        }
        return false;
    }

    /*
     * 点击到聊天页面
     */
    public boolean enterChatView(AndroidDriver driver) {
        //向第一个顶置好友发送数据,便于运营人员随时检测抓取的url
        //检测是否弹出更新按钮
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            return false;
        }
        try {
            if (driver.findElementsById("com.tencent.mm:id/adq").isEmpty()) {
                driver.findElementById("com.tencent.mm:id/a_x").click();
                driver.findElementById("com.tencent.mm:id/a_y").click();
                List<WebElement> homelements = driver.findElementsById("com.tencent.mm:id/adq");
                if (homelements.size() > 1) {
                    homelements.get(0).click();
                }
            } else {
                List<WebElement> homelements = driver.findElementsById("com.tencent.mm:id/adq");
                if (homelements.size() > 1) {
                    homelements.get(0).click();
                }
            }
        } catch (Exception e) {
            //偶尔微信会弹出更新提示,走这个流程
            driver.findElementById("com.tencent.mm:id/a_x").click();
            driver.findElementById("com.tencent.mm:id/a_y").click();
            List<WebElement> homelements = driver.findElementsById("com.tencent.mm:id/adq");
            if (homelements.size() > 1) {
                homelements.get(0).click();
            }
        }
        return true;
    }

    /*
    * kill掉所有的appiumService和由它启动的adb
    */
    public boolean killAppiumServiceAndRelateAdb(String devicesName) {
        System.out.println("XXXX: killNode");
        List<String> nodePidList = new ArrayList<>();
        //取出当前系统里所有的node
        ShellProcess shellProcess = executeShell("ps -aux");
        if (shellProcess.isSuccessful()) {
            Scanner scanner = getShellResultContent(shellProcess.getProcess());
            while (scanner.hasNext()) {
                String processInf = scanner.nextLine();
                if (processInf.contains("node")) {
                    if (processInf.contains(devicesName)) {
                        String[] strings = processInf.split("\\s+");
                        nodePidList.add(strings[1]);
                    }
                }
            }

            for (String nodePid : nodePidList) {
                String nodeSubadbId = "";
                ShellProcess shellprocesstres = executeShell("pstree " + nodePid + " -p");
                if (shellprocesstres.isSuccessful()) {
                    Scanner inNodePid = getShellResultContent(shellProcess.getProcess());
                    while (inNodePid.hasNext()) {
                        String processInf = inNodePid.nextLine();
                        if (processInf.contains("adb")) {
                            String id = processInf.trim().split("adb")[1];
                            id = id.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& ;*（）——+|{}【】‘；：”“’。，、？|-]", "");
                            nodeSubadbId = id;
                            break;
                        }
                    }
                    if (!nodeSubadbId.equals("")) {
                        executeShell("kill -s 9 " + nodeSubadbId);
                    }
                    executeShell("kill -s 9 " + nodePid);
                }
            }
        }
        return true;
    }

    /*
    * kill掉相应的appiumService和由它启动的adb
    */
    public boolean stopAppium(String pid,String deviceName){
        String adbId = null;
        //记录下指定pid node下的adb
        ShellProcess shellProcess = executeShell("pstree " + pid + " -p");
        if (shellProcess.isSuccessful()) {
            Scanner in = getShellResultContent(shellProcess.getProcess());
            if (in.hasNext()) {
                while (in.hasNext()) {
                    String processInf = in.nextLine();
                    if (processInf.contains("adb")) {
                        String id = processInf.trim().split("adb")[1];
                        id = id.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& ;*（）——+|{}【】‘；：”“’。，、？|-]", "");
                        adbId = id;
                        break;
                    }
                }
                //Kill掉node
                executeShell("kill -s 9 " + pid);
                //确认是否删除
                Scanner in2 = getShellResultContent(executeShell("pstree " + pid + " -p").getProcess());
                if (in2.hasNext()) {
                    //如果根据pid没有删除成功,强制删除一次
                    System.out.println("XXXX: stopAppium3" + pid);
                    killAppiumServiceAndRelateAdb(deviceName);
                }
                //kill掉删除不掉的adb
                if (adbId != null) {
                    ShellProcess process3 = executeShell("kill -s 9 " + adbId);
                }
            }
        }
        return true;
    }

    public boolean stopAppium(AndroidDriverStatus androidDriverStatus) {
        androidDriverStatus.setStatus("10");
        String pid = androidDriverStatus.getServicePid();
        String deviceName = androidDriverStatus.getDeviceName();
        return stopAppium(pid,deviceName);
    }

    public void restartDriver(String devicesName) {
        for (AndroidDriverStatus androidDriverStatus : driverList) {
            if (androidDriverStatus.getDeviceName().equalsIgnoreCase(devicesName)) {
                androidDriverStatus.setStatus("10");
                restartAppium(androidDriverStatus);
                break;
            }
        }
    }

    //TODO 后续需重构
    public void restartAppium(AndroidDriverStatus androidDriverStatus) {
        int port = androidDriverStatus.getPort();
        int bp = androidDriverStatus.getBp();
        String devicesName = androidDriverStatus.getDeviceName();

        int loop = 0; //循环出错次数
        while (loop < 3) {
            loop++;
            String servicePid = startAppiumService(port, bp, devicesName);
            if (servicePid.equals("")) {
                //启动appium失败,删除node,避免实际上已经启动了appium,但是删除失败,重新启动appium端口占用的错误
                System.out.println("XXXX: pid is empty , kill appium-server");
                killAppiumServiceAndRelateAdb(devicesName);
            } else {
                //启动appium成功
                androidDriverStatus.setServicePid(servicePid);
                break;
            }
        }

        ChromeOption chromeOption = new ChromeOption();
        chromeOption.setAndroidPackage("com.tencent.mm");
        chromeOption.setAndroidActivity(".plugin.webview.ui.tools.WebViewUI");
        chromeOption.setAndroidProcess("com.tencent.mm:tools");
        AndroidDriver driver = creatDriver("com.tencent.mm", ".ui.LauncherUI", devicesName, chromeOption, bp, port);
        if (driver != null) {
            //创建成功
            androidDriverStatus.setStatus("0");
            androidDriverStatus.setAndroidDriver(driver);
            if (enterChatView(driver)) {
                //进入到聊天页面成功
            }
        } else {
            //创建失败
            androidDriverStatus.setStatus("9");
        }
    }

    private List<String> getAndroidDevices() {
        List<String> devicesNameList = new ArrayList<>();

        String baseServer = System.getProperty("mcss_url");
        String serviceName = System.getProperty("mcss_service_name");
        if (serviceName.startsWith("/")){
            serviceName = serviceName.substring(1);
        }
        String getDataUrl = "/getDevicesList.json?registServer=" + serviceName;
        Map<String, String> dataMap = new HashMap<String, String>();
        Object firstObject = CallRemoteServiceUtil.callRemoteService(this.getClass().getName(), baseServer + getDataUrl, "get", dataMap);
        if (null == firstObject) {
            System.out.println("XXXX: location_mcss : driverManager devices null");
        } else {
            System.out.println("XXXX: location_mcss : driverManager callremote object: " + firstObject.toString());
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) firstObject;
            String devicesArray = jsonObject.getString("data");
            JSONArray jsonDataStorageFieldArray = JSON.parseArray(devicesArray);
            if (jsonDataStorageFieldArray.size() > 0) {
                for (int i = 0; i < jsonDataStorageFieldArray.size(); i++) {
                    com.alibaba.fastjson.JSONObject job = jsonDataStorageFieldArray.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                    devicesNameList.add(job.getString("devicesName"));
                    System.out.println("XXXX: location_mcss : driverManager devices:" + job.getString("devicesName"));
                }
            }
        }
        return devicesNameList;
    }

    /**
     * 简单描述：创建setting driver
     *
     * @param appPackage   : com.android.settings设置 / com.saurik.substrate hook环境
     * @param appActivity: .Settings设置  / .SetupActivity hook环境
     * @return 是否成功
     */
    public void getEnableSettingDriverManager(String appPackage, String appActivity, ChromeOption chromeOption) {
        System.out.println("XXXX: location_mcss : begin create client_1: " + driverList.size());
        for (AndroidDriverStatus androidDriverDao : driverList) {
            System.out.println("XXXX: location_mcss : begin create client_1: " + androidDriverDao.getStatus());
            System.out.println("XXXX: location_mcss : begin create client");
            AndroidDriver driver = creatDriver(appPackage, appActivity, androidDriverDao.getDeviceName(), chromeOption, androidDriverDao.getBp(), androidDriverDao.getPort());
            if (driver != null) {
                //appium driver创建成功
                System.out.println("XXXX: location_mcss : driverManager start weixin success");
                androidDriverDao.setStatus("0");
                androidDriverDao.setBindApp(appPackage);
                androidDriverDao.setAndroidDriver(driver);
                if (enterChatView(driver)) {
                    //进入到聊天页面成功,等待
                    Runnable runnable = new DriverWaitRunable(androidDriverDao);
                    Thread thread = new Thread(runnable);
                    thread.start();
                    androidDriverDao.setThread(thread);
                }
            } else {
                androidDriverDao.setStatus("9");
            }
            break;
        }
    }


    /**
     * 简单描述：创建 driver
     *
     * @param appPackage   app包名
     * @param appActivity  app启动类
     * @param udid         设备名称
     * @param chromeOption chrome设置
     * @param bp
     * @param port
     * @return 创建成功返回driver
     */
    private AndroidDriver creatDriver(String appPackage, String appActivity, String udid, ChromeOption chromeOption, int bp, int port) {
        DesiredCapabilities capability = new DesiredCapabilities();
        capability.setCapability("app", "");
        capability.setCapability("appPackage", appPackage);
        capability.setCapability("appActivity", appActivity);
        capability.setCapability("devicesName", udid);
        capability.setCapability("udid", udid);
        capability.setCapability("fullReset", "false");
        capability.setCapability("noReset", "true");
        capability.setCapability("bootstrapPort", bp + "");
        capability.setCapability("unicodeKeyboard", "true");
        capability.setCapability("resetKeyboard", "true");

        if (chromeOption != null) {
            //关键是加上这段
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("androidPackage", chromeOption.getAndroidPackage());
            options.setExperimentalOption("androidUseRunningApp", true);
            options.setExperimentalOption("androidActivity", chromeOption.getAndroidActivity());
            options.setExperimentalOption("androidProcess", chromeOption.getAndroidProcess());
            capability.setCapability(ChromeOptions.CAPABILITY, options);
        }
        try {
            AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:" + port + "/wd/hub"),
                    capability);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            return driver;
        } catch (MalformedURLException e) {
            System.out.println("XXXX: erorr initAppium" + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 简单描述：获取执行shell后打印结果
     */
    private Scanner getShellResultContent(Process process) {
        return new Scanner(process.getInputStream());
    }

    public ShellProcess executeShell(String shell) {
        return executeShell(shell, true);
    }

    /**
     * 简单描述：执行shell命令
     *
     * @return Process(shell命令执行后的返回)+status
     */
    public ShellProcess executeShell(String shell, boolean isWait) {
        ShellProcess shellProcess = new ShellProcess();
        try {
            Process process = Runtime.getRuntime().exec(shell);
            if (isWait) {
                process.waitFor();
                shellProcess.setProcess(process);
                shellProcess.setSuccessful(true);
            } else {
                Thread.sleep(6 * 1000);
                shellProcess.setProcess(process);
                shellProcess.setSuccessful(true);
            }
        } catch (IOException | InterruptedException e) {
            shellProcess.setProcess(null);
            shellProcess.setSuccessful(false);
            e.printStackTrace();
        }
        return shellProcess;
    }
}
