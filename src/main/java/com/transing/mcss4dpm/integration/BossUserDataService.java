package com.transing.mcss4dpm.integration;

import com.jeeframework.logicframework.integration.DataService;
import com.transing.mcss4dpm.integration.bo.BossUser;
import com.transing.mcss4dpm.web.filter.GetBossUsersFilter;
import com.transing.mcss4dpm.web.filter.UpdateBossUserFilter;

import java.util.List;

/**
 * 后台用户数据操作接口
 *
 * @author lanceyan
 * @version 1.0
 * @see
 */
public interface BossUserDataService extends DataService {

    BossUser getBossUserByUid(long uid);

    int addBossUser(BossUser bossUser);

    int updateBossUser(UpdateBossUserFilter updateBossUserFilter);

    int deleteBossUser(long uid);

    /**
     * 简单描述：根据用户名、密码返回用户对象
     * <p>
     *
     * @param userName
     * @param password
     * @
     */
    BossUser getBossUserByPasswd(String userName, String password);

    /**
     * 简单描述：根据userFilter返回用户对象列表
     * <p>
     *
     * @param getBossUsersFilter
     * @
     */
    List<BossUser> getBossUsers(GetBossUsersFilter getBossUsersFilter);


}