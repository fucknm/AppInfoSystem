package cn.appsys.dao.devuser;

import org.apache.ibatis.annotations.Param;

import cn.appsys.pojo.DevUser;

public interface DevUserMapper {
	/**
	 * 登录
	 * @param devCode
	 * @return
	 */
	public DevUser Login(@Param("devCode")String devCode,@Param("devPassword")String devPassword);
}
