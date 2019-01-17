/**
 * @project: mcss4dpm
 * @Title: BossUserController.java
 * @Package: com.transing.mcss4dpm.web.controller
 * <p>
 * Copyright (c) 2014-2017 Jeeframework Limited, Inc.
 * All rights reserved.
 */
package com.transing.mcss4dpm.web.controller;

import com.jeeframework.util.cookie.CookieHelper;
import com.jeeframework.util.encrypt.BASE64Util;
import com.jeeframework.util.encrypt.MD5Util;
import com.jeeframework.util.validate.Validate;
import com.jeeframework.webframework.exception.SystemCode;
import com.jeeframework.webframework.exception.WebException;
import com.mangofactory.swagger.annotations.ApiIgnore;
import com.transing.mcss4dpm.biz.service.BossUserService;
import com.transing.mcss4dpm.biz.service.UserService;
import com.transing.mcss4dpm.constant.Constants;
import com.transing.mcss4dpm.integration.bo.BossUser;
import com.transing.mcss4dpm.web.filter.GetBossUsersFilter;
import com.transing.mcss4dpm.web.filter.UpdateBossUserFilter;
import com.transing.mcss4dpm.web.po.AddBossUserPO;
import com.transing.mcss4dpm.web.po.UpdateBossUserPO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("bossUserController")
@Api(value = "后台用户登录注册，资料设置", description = "后台用户相关的访问接口", position = 1)
@ApiIgnore //忽略这个api生成文档
public class BossUserController {
    private static final String LOGIN_URL = "user/login";

    @Resource
    private BossUserService bossUserService;

    @Resource
    private UserService userService;

    @RequestMapping(value = "/login.html", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @ApiOperation(value = "后台用户登录接口", position = 0)
    public ModelAndView login(HttpServletRequest req, HttpServletResponse res) {

        if ("get".equalsIgnoreCase(req.getMethod())) {
            ModelAndView mv = new ModelAndView(LOGIN_URL);
            mv.addObject("test", "hello");
            //freemarker页面可直接使用${test}获取变量
            return mv;
        }

        String userName = req.getParameter("userName");
        String passwd = req.getParameter("password");

        try {
            if (Validate.isEmpty(userName)) {
                throw new WebException(SystemCode.BIZ_LOGIN_NAME_EXCEPTION);
            }
            if (Validate.isEmpty(passwd)) {
                throw new WebException(SystemCode.BIZ_LOGIN_PASSWORD_EXCEPTION);
            }

            userName = userName.trim();

            BossUser bossUser = bossUserService.getBossUserByPasswd(userName, passwd);

            if (bossUser == null) {
                throw new WebException(SystemCode.BIZ_LOGIN_PASSNOTRIGHT_EXCEPTION);
            }

            String remember = req.getParameter("remember");
            if (!Validate.isEmpty(remember)) {
                StringBuilder cookieLogin = new StringBuilder();
                cookieLogin.append(bossUser.getUid());

                long validTime = System.currentTimeMillis() + (Constants.COOKIE_MAX_AGE * 1000);
                // MD5加密用户详细信息
                String cookieValueWithMd5 = MD5Util.encrypt(bossUser.getUid() + ":" + bossUser.getPasswd() + ":" + validTime + ":" + Constants.LOGIN_KEY);
                // 将要被保存的完整的Cookie值
                String cookieValue = bossUser.getUid() + ":" + validTime + ":" + cookieValueWithMd5;
                // 再一次对Cookie的值进行BASE64编码
                String cookieValueBase64 = BASE64Util.encode(cookieValue.getBytes());
                // 是自动登录则设置cookie
                CookieHelper.setCookie(res, Constants.LOGIN_COOKIE_SIGN, cookieValueBase64, null, "/", Constants.COOKIE_ONE_YEAR_AGE); // 设置了自动登录，cookie在客户端保存2年
            }
            req.getSession().setMaxInactiveInterval(2 * 3600); // Session保存两小时
            req.getSession().setAttribute(Constants.WITH_SESSION_USER, bossUser);


            req.setAttribute("user", bossUser);

            return new ModelAndView("redirect:/dashboard.html");
        } catch (WebException e) {
            req.setAttribute("error", e);
            return new ModelAndView(LOGIN_URL);
        }


    }

    @RequestMapping(value = "/dashboard.html", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "后台用户综合访问界面", position = 1)
    public ModelAndView dashboard(HttpServletRequest req, HttpServletResponse res) {

        Map<String, Object> retMap = new HashMap<>();

        long userCount = userService.getUsersCount(null);

        retMap.put("userCount", userCount);

        req.setAttribute("result", retMap);


        return new ModelAndView("user/dashboard");
    }

    @RequestMapping(value = "/logout.html", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "后台用户退出", position = 2)
    public ModelAndView logout(HttpServletRequest req, HttpServletResponse res) {

        CookieHelper.setCookie(res, Constants.LOGIN_COOKIE_SIGN, "", null, "/", 0);

        req.getSession().removeAttribute(Constants.WITH_SESSION_USER);

        req.getSession().invalidate();

        return new ModelAndView(LOGIN_URL);
    }

    @RequestMapping(value = "/addBossUser.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "后台添加用户接口", position = 3)
    public AddBossUserPO addBossUser(HttpServletRequest req, HttpServletResponse res) {

        BossUser bossUser = new BossUser();

        bossUser.setUserName("111");
        bossUser.setNickName("1111");
        bossUser.setDescription("111");
        bossUser.setPasswd("111");
        bossUser.setAvatar("111");
        bossUser.setType(1);
        bossUser.setEmail("1111");

        bossUserService.addBossUser(bossUser);

        AddBossUserPO addBossUserPO = new AddBossUserPO();
        addBossUserPO.setUid(bossUser.getUid());

        return addBossUserPO;
    }

    @RequestMapping(value = "/updateBossUser.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "后台更新用户接口", position = 4)
    public UpdateBossUserPO updateBossUser(HttpServletRequest req, HttpServletResponse res) {

        UpdateBossUserFilter updateBossUserFilter = new UpdateBossUserFilter();

        updateBossUserFilter.setDescription("111");
        updateBossUserFilter.setAvatar("111");
        updateBossUserFilter.setType(1);
        updateBossUserFilter.setEmail("1111");
        updateBossUserFilter.setUid(1L);

        int effectRows = bossUserService.updateBossUser(updateBossUserFilter);

        UpdateBossUserPO updateBossUserPO = new UpdateBossUserPO();
        updateBossUserPO.setUpdateRows(effectRows);

        return updateBossUserPO;
    }

    @RequestMapping(value = "/deleteBossUser.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "后台删除用户接口", position = 5)
    public UpdateBossUserPO deleteBossUser(HttpServletRequest req, HttpServletResponse res) {
        int effectRows = bossUserService.deleteBossUser(1L);
        UpdateBossUserPO updateBossUserPO = new UpdateBossUserPO();
        updateBossUserPO.setUpdateRows(effectRows);

        return updateBossUserPO;
    }

    @RequestMapping(value = "/getBossUsers.json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "后台查询用户列表接口", position = 6)
    public List<BossUser> getBossUsers(HttpServletRequest req, HttpServletResponse res) {
        GetBossUsersFilter getBossUsersFilter = new GetBossUsersFilter();
        getBossUsersFilter.setPageSize(10L);
        getBossUsersFilter.setStartRow(0L);

        return bossUserService.getBossUsers(getBossUsersFilter);

    }

}
