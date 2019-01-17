package com.transing.mcss4dpm.integration.impl.ibatis;

import com.jeeframework.logicframework.integration.dao.DAOException;
import com.jeeframework.logicframework.integration.dao.ibatis.BaseDaoiBATIS;
import com.transing.mcss4dpm.integration.BossUserDataService;
import com.transing.mcss4dpm.integration.bo.BossUser;
import com.transing.mcss4dpm.web.filter.GetBossUsersFilter;
import com.transing.mcss4dpm.web.filter.UpdateBossUserFilter;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问对象
 *
 * @author lanceyan
 * @version 1.0
 */
@Scope("prototype")
@Repository("bossUserDataService")
public class BossUserDAOIbatis extends BaseDaoiBATIS implements BossUserDataService {
    @Override
    public int addBossUser(BossUser bossUser) {
        try {
            return sqlSessionTemplate.insert("bossBossUserMapper.addBossUser", bossUser);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }

    @Override
    public BossUser getBossUserByUid(long uid) {
        try {
            return sqlSessionTemplate.selectOne("bossBossUserMapper.getBossUserByUid", uid);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }

    @Override
    public int updateBossUser(UpdateBossUserFilter updateBossUserFilter) {
        try {
            return sqlSessionTemplate.update("bossBossUserMapper.updateBossUser", updateBossUserFilter);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }

    @Override
    public int deleteBossUser(long uid) {
        try {
            return sqlSessionTemplate.delete("bossBossUserMapper.deleteBossUser", uid);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }

    @Override
    public BossUser getBossUserByPasswd(String userName, String password) {
        try {
            BossUser bossUserParam = new BossUser();
            bossUserParam.setUserName(userName);
            bossUserParam.setPasswd(password);
            return sqlSessionTemplate.selectOne("bossBossUserMapper.getBossUserByPasswd", bossUserParam);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }

    /**
     * 简单描述：根据userFilter返回用户对象列表
     * <p/>
     *
     * @
     */
    @Override
    public List<BossUser> getBossUsers(GetBossUsersFilter getBossUsersFilter) {
        try {
            return sqlSessionTemplate.selectList("bossBossUserMapper.getBossUsers", getBossUsersFilter);
        } catch (DataAccessException e) {
            throw new DAOException(e);

        }
    }


}